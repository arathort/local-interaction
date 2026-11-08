package com.arathort.localinteraction.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arathort.localinteraction.R
import com.arathort.localinteraction.domain.model.ApplicationStatus
import com.arathort.localinteraction.domain.model.UserRole
import com.arathort.localinteraction.domain.repository.ProfileRepository
import com.arathort.localinteraction.domain.usecase.GetMyProfileUseCase
import com.arathort.localinteraction.domain.usecase.GetUserProfileUseCase
import com.arathort.localinteraction.domain.usecase.LogoutUseCase
import com.arathort.localinteraction.domain.usecase.ObserveCurrentUserUseCase
import com.arathort.localinteraction.domain.usecase.UpdateProfileUseCase
import com.arathort.localinteraction.domain.usecase.WithdrawApplicationUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ProfileViewModel.Factory::class)
class ProfileViewModel @AssistedInject constructor(
    @Assisted private val targetUserId: String?,
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val withdrawApplicationUseCase: WithdrawApplicationUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(targetUserId: String?): ProfileViewModel
    }

    private val _uiState = MutableStateFlow(ProfileState(isLoading = true))
    val uiState: StateFlow<ProfileState> = _uiState.asStateFlow()

    private val effectChannel = Channel<ProfileEffect>(Channel.BUFFERED)
    val effect: Flow<ProfileEffect> = effectChannel.receiveAsFlow()

    init {
        loadProfile()
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.Refresh -> loadProfile(isRefresh = true)
            is ProfileEvent.SelectTab -> _uiState.update { it.copy(selectedTab = event.tab) }
            is ProfileEvent.EditProfileClicked -> showEditDialog()
            is ProfileEvent.EditFullNameChanged -> _uiState.update { it.copy(editFullName = event.value, errorMessageResId = null) }
            is ProfileEvent.EditPhoneChanged -> _uiState.update { it.copy(editPhone = event.value) }
            is ProfileEvent.EditBioChanged -> _uiState.update { it.copy(editBio = event.value) }
            is ProfileEvent.SaveProfileClicked -> saveProfile()
            is ProfileEvent.DismissEditProfileDialog -> _uiState.update { it.copy(isEditProfileDialogVisible = false, errorMessageResId = null) }
            is ProfileEvent.LogoutClicked -> _uiState.update { it.copy(isLogoutDialogVisible = true) }
            is ProfileEvent.ConfirmLogoutClicked -> logout()
            is ProfileEvent.DismissLogoutDialog -> _uiState.update { it.copy(isLogoutDialogVisible = false) }
            is ProfileEvent.WithdrawTaskClicked -> _uiState.update { it.copy(withdrawConfirmationApplicationId = event.applicationId) }
            is ProfileEvent.ConfirmWithdrawTaskClicked -> confirmWithdrawTask()
            is ProfileEvent.DismissWithdrawDialog -> _uiState.update { it.copy(withdrawConfirmationApplicationId = null) }
            is ProfileEvent.EventClicked -> sendEffect(ProfileEffect.NavigateToEventDetails(event.eventId))
            is ProfileEvent.LoginClicked -> sendEffect(ProfileEffect.NavigateToAuth)
            is ProfileEvent.BackClicked -> sendEffect(ProfileEffect.NavigateBack)
        }
    }

    private fun loadProfile(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true) }
            } else {
                _uiState.update { it.copy(isLoading = true) }
            }

            val loggedInUser = observeCurrentUserUseCase().first()
            val isTargetExternal = !targetUserId.isNullOrBlank() && targetUserId != loggedInUser.id

            if (isTargetExternal) {
                val result = getUserProfileUseCase(targetUserId!!)
                result.onSuccess { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            user = user,
                            isCurrentUser = false,
                            isGuest = false
                        )
                    }
                    if (user.role == UserRole.ORGANIZER) {
                        loadOrganizerEvents(user.id)
                    }
                }.onFailure {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessageResId = R.string.error_network
                        )
                    }
                }
            } else {
                if (loggedInUser.role == UserRole.GUEST) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            user = loggedInUser,
                            isCurrentUser = true,
                            isGuest = true
                        )
                    }
                } else {
                    val result = getMyProfileUseCase()
                    result.onSuccess { user ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                user = user,
                                isCurrentUser = true,
                                isGuest = false
                            )
                        }
                        if (user.role == UserRole.ORGANIZER) {
                            loadOrganizerEvents(user.id)
                        }
                    }.onFailure {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                user = loggedInUser,
                                isCurrentUser = true,
                                isGuest = false,
                                errorMessageResId = R.string.error_network
                            )
                        }
                    }
                }
            }
        }
    }

    private fun loadOrganizerEvents(organizerId: String) {
        viewModelScope.launch {
            profileRepository.observeOrganizerEvents(organizerId).collect { events ->
                _uiState.update { it.copy(organizerEvents = events) }
            }
        }
    }

    private fun showEditDialog() {
        val currentUser = _uiState.value.user ?: return
        _uiState.update {
            it.copy(
                isEditProfileDialogVisible = true,
                editFullName = currentUser.fullName.orEmpty(),
                editPhone = currentUser.phone.orEmpty(),
                editBio = currentUser.description.orEmpty(),
                errorMessageResId = null
            )
        }
    }

    private fun saveProfile() {
        val fullName = _uiState.value.editFullName.trim()
        if (fullName.length < 2) {
            _uiState.update { it.copy(errorMessageResId = R.string.profile_name_too_short_error) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingProfile = true) }
            val phone = _uiState.value.editPhone.trim()
            val bio = _uiState.value.editBio.trim()
            val result = updateProfileUseCase(fullName = fullName, phone = phone, bio = bio)
            result.onSuccess { updatedUser ->
                _uiState.update {
                    it.copy(
                        isSavingProfile = false,
                        isEditProfileDialogVisible = false,
                        user = updatedUser,
                        errorMessageResId = null
                    )
                }
                effectChannel.send(ProfileEffect.ShowSnackbar(R.string.profile_update_success))
            }.onFailure {
                _uiState.update {
                    it.copy(
                        isSavingProfile = false,
                        errorMessageResId = R.string.error_network
                    )
                }
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLogoutDialogVisible = false) }
            logoutUseCase()
            effectChannel.send(ProfileEffect.NavigateBack)
        }
    }

    private fun confirmWithdrawTask() {
        val applicationId = _uiState.value.withdrawConfirmationApplicationId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isWithdrawing = true) }
            val result = withdrawApplicationUseCase(applicationId)
            result.onSuccess {
                val currentTasks = _uiState.value.user?.myTasks.orEmpty()
                val updatedTasks = currentTasks.map { task ->
                    if (task.applicationId == applicationId) {
                        task.copy(
                            applicationStatus = ApplicationStatus.WITHDRAWN,
                            canWithdraw = false
                        )
                    } else {
                        task
                    }
                }
                _uiState.update { state ->
                    state.copy(
                        isWithdrawing = false,
                        withdrawConfirmationApplicationId = null,
                        user = state.user?.copy(myTasks = updatedTasks)
                    )
                }
                effectChannel.send(ProfileEffect.ShowSnackbar(R.string.event_details_application_withdrawn))
            }.onFailure {
                _uiState.update { state ->
                    state.copy(
                        isWithdrawing = false,
                        withdrawConfirmationApplicationId = null
                    )
                }
                effectChannel.send(ProfileEffect.ShowSnackbar(R.string.error_network))
            }
        }
    }

    private fun sendEffect(effect: ProfileEffect) {
        viewModelScope.launch {
            effectChannel.send(effect)
        }
    }
}
