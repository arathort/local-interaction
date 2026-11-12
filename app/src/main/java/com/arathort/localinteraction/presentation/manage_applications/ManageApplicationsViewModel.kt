package com.arathort.localinteraction.presentation.manage_applications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arathort.localinteraction.R
import com.arathort.localinteraction.domain.model.UserRole
import com.arathort.localinteraction.domain.usecase.ApproveApplicationUseCase
import com.arathort.localinteraction.domain.usecase.GetEventApplicationsManageUseCase
import com.arathort.localinteraction.domain.usecase.ObserveCurrentUserUseCase
import com.arathort.localinteraction.domain.usecase.RejectApplicationUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ManageApplicationsViewModel.Factory::class)
class ManageApplicationsViewModel @AssistedInject constructor(
    @Assisted private val eventId: String,
    private val getEventApplicationsManageUseCase: GetEventApplicationsManageUseCase,
    private val approveApplicationUseCase: ApproveApplicationUseCase,
    private val rejectApplicationUseCase: RejectApplicationUseCase,
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(eventId: String): ManageApplicationsViewModel
    }

    private val _uiState = MutableStateFlow(ManageApplicationsState(eventId = eventId, isLoading = true))
    val uiState: StateFlow<ManageApplicationsState> = _uiState.asStateFlow()

    private val _effect = Channel<ManageApplicationsEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        observeCurrentUser()
        loadApplications()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            observeCurrentUserUseCase().collect { user ->
                val isOrganizer = user.role == UserRole.ORGANIZER || user.role == UserRole.ADMIN
                _uiState.update { it.copy(isOrganizer = isOrganizer) }
                if (!isOrganizer) {
                    _effect.send(ManageApplicationsEffect.ShowSnackbar(R.string.error_only_organizer_can_manage_applications))
                    _effect.send(ManageApplicationsEffect.NavigateBack)
                }
            }
        }
    }

    fun onEvent(event: ManageApplicationsEvent) {
        when (event) {
            ManageApplicationsEvent.Refresh -> loadApplications(isRefresh = true)
            is ManageApplicationsEvent.FilterSelected -> {
                _uiState.update { it.copy(selectedFilter = event.filter) }
            }
            is ManageApplicationsEvent.VolunteerClicked -> {
                viewModelScope.launch {
                    _effect.send(ManageApplicationsEffect.NavigateToVolunteerProfile(event.volunteerId))
                }
            }
            is ManageApplicationsEvent.ApproveClicked -> {
                val state = _uiState.value
                if (state.volunteerQuota > 0 && state.approvedVolunteersCount >= state.volunteerQuota) {
                    _uiState.update { it.copy(pendingApprovalApplicationId = event.applicationId) }
                } else {
                    executeApproval(event.applicationId)
                }
            }
            ManageApplicationsEvent.ConfirmOverQuotaApproval -> {
                val applicationId = _uiState.value.pendingApprovalApplicationId ?: return
                _uiState.update { it.copy(pendingApprovalApplicationId = null) }
                executeApproval(applicationId)
            }
            ManageApplicationsEvent.DismissOverQuotaDialog -> {
                _uiState.update { it.copy(pendingApprovalApplicationId = null) }
            }
            is ManageApplicationsEvent.RejectClicked -> {
                _uiState.update {
                    it.copy(
                        rejectingApplicationId = event.applicationId,
                        rejectionReasonText = "",
                        rejectionReasonErrorResId = null
                    )
                }
            }
            is ManageApplicationsEvent.RejectionReasonChanged -> {
                _uiState.update {
                    it.copy(
                        rejectionReasonText = event.reason,
                        rejectionReasonErrorResId = null
                    )
                }
            }
            ManageApplicationsEvent.ConfirmRejection -> {
                val applicationId = _uiState.value.rejectingApplicationId ?: return
                val reason = _uiState.value.rejectionReasonText.trim()
                if (reason.isEmpty()) {
                    _uiState.update { it.copy(rejectionReasonErrorResId = R.string.error_rejection_reason_required) }
                    return
                }
                executeRejection(applicationId, reason)
            }
            ManageApplicationsEvent.DismissRejectDialog -> {
                _uiState.update {
                    it.copy(
                        rejectingApplicationId = null,
                        rejectionReasonText = "",
                        rejectionReasonErrorResId = null
                    )
                }
            }
            ManageApplicationsEvent.BackClicked -> {
                viewModelScope.launch {
                    _effect.send(ManageApplicationsEffect.NavigateBack)
                }
            }
        }
    }

    private fun executeApproval(applicationId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingAction = true) }
            val result = approveApplicationUseCase(applicationId)
            result.onSuccess {
                _effect.send(ManageApplicationsEffect.ShowSnackbar(R.string.manage_applications_approve_success))
                loadApplications(isRefresh = true)
            }.onFailure {
                _effect.send(ManageApplicationsEffect.ShowSnackbar(R.string.error_unknown))
                _uiState.update { it.copy(isProcessingAction = false) }
            }
        }
    }

    private fun executeRejection(applicationId: String, reason: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingAction = true, rejectingApplicationId = null) }
            val result = rejectApplicationUseCase(applicationId, reason)
            result.onSuccess {
                _effect.send(ManageApplicationsEffect.ShowSnackbar(R.string.manage_applications_reject_success))
                loadApplications(isRefresh = true)
            }.onFailure {
                _effect.send(ManageApplicationsEffect.ShowSnackbar(R.string.error_unknown))
                _uiState.update { it.copy(isProcessingAction = false) }
            }
        }
    }

    private fun loadApplications(isRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                if (isRefresh) it.copy(isRefreshing = true) else it.copy(isLoading = true)
            }
            val result = getEventApplicationsManageUseCase.fetch(eventId)
            result.onSuccess { manage ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isProcessingAction = false,
                        eventTitle = manage.eventTitle,
                        eventStatus = manage.eventStatus,
                        volunteerQuota = manage.volunteerQuota,
                        approvedVolunteersCount = manage.approvedVolunteersCount,
                        applications = manage.applications,
                        errorMessageResId = null
                    )
                }
            }.onFailure {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isProcessingAction = false,
                        errorMessageResId = R.string.error_network
                    )
                }
            }
        }
    }
}
