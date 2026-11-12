package com.arathort.localinteraction.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arathort.localinteraction.R
import com.arathort.localinteraction.domain.model.UserRole
import com.arathort.localinteraction.domain.usecase.ApproveEventModerationUseCase
import com.arathort.localinteraction.domain.usecase.BlockUserUseCase
import com.arathort.localinteraction.domain.usecase.CreateCategoryUseCase
import com.arathort.localinteraction.domain.usecase.DeleteCategoryUseCase
import com.arathort.localinteraction.domain.usecase.DeleteComplaintContentUseCase
import com.arathort.localinteraction.domain.usecase.GetAdminCategoriesUseCase
import com.arathort.localinteraction.domain.usecase.GetAdminComplaintsUseCase
import com.arathort.localinteraction.domain.usecase.GetAdminUsersUseCase
import com.arathort.localinteraction.domain.usecase.GetPendingModerationEventsUseCase
import com.arathort.localinteraction.domain.usecase.ObserveCurrentUserUseCase
import com.arathort.localinteraction.domain.usecase.RejectComplaintUseCase
import com.arathort.localinteraction.domain.usecase.RejectEventModerationUseCase
import com.arathort.localinteraction.domain.usecase.UnblockUserUseCase
import com.arathort.localinteraction.domain.usecase.UpdateCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminPanelViewModel @Inject constructor(
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val getPendingModerationEventsUseCase: GetPendingModerationEventsUseCase,
    private val approveEventModerationUseCase: ApproveEventModerationUseCase,
    private val rejectEventModerationUseCase: RejectEventModerationUseCase,
    private val getAdminCategoriesUseCase: GetAdminCategoriesUseCase,
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    private val getAdminComplaintsUseCase: GetAdminComplaintsUseCase,
    private val rejectComplaintUseCase: RejectComplaintUseCase,
    private val deleteComplaintContentUseCase: DeleteComplaintContentUseCase,
    private val getAdminUsersUseCase: GetAdminUsersUseCase,
    private val blockUserUseCase: BlockUserUseCase,
    private val unblockUserUseCase: UnblockUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminPanelState())
    val uiState: StateFlow<AdminPanelState> = _uiState.asStateFlow()

    private val effectChannel = Channel<AdminPanelEffect>(Channel.BUFFERED)
    val effect = effectChannel.receiveAsFlow()

    init {
        checkAdminAccessAndInitialize()
    }

    fun onEvent(event: AdminPanelEvent) {
        when (event) {
            is AdminPanelEvent.SelectTab -> handleSelectTab(event.tab)
            AdminPanelEvent.Refresh -> refreshCurrentTab()
            is AdminPanelEvent.ToggleEventDetails -> handleToggleEventDetails(event.eventId)
            is AdminPanelEvent.ApproveEvent -> handleApproveEvent(event.eventId)
            is AdminPanelEvent.OpenRejectEventDialog -> _uiState.update { it.copy(eventToReject = event.event) }
            is AdminPanelEvent.ConfirmRejectEvent -> handleConfirmRejectEvent(event.reason)
            AdminPanelEvent.DismissRejectEventDialog -> _uiState.update { it.copy(eventToReject = null) }
            AdminPanelEvent.OpenCreateCategoryDialog -> _uiState.update { it.copy(categoryDialogMode = CategoryDialogMode.Create) }
            is AdminPanelEvent.OpenEditCategoryDialog -> _uiState.update { it.copy(categoryDialogMode = CategoryDialogMode.Edit(event.category)) }
            is AdminPanelEvent.ConfirmSaveCategory -> handleConfirmSaveCategory(event.name)
            is AdminPanelEvent.OpenDeleteCategoryDialog -> _uiState.update { it.copy(categoryToDelete = event.category) }
            AdminPanelEvent.ConfirmDeleteCategory -> handleConfirmDeleteCategory()
            AdminPanelEvent.DismissCategoryDialog -> _uiState.update { it.copy(categoryDialogMode = null, categoryToDelete = null) }
            is AdminPanelEvent.SelectComplaintsUsersSubTab -> handleSelectComplaintsUsersSubTab(event.subTab)
            is AdminPanelEvent.FilterComplaints -> handleFilterComplaints(event.filter)
            is AdminPanelEvent.OpenDeleteComplaintContentDialog -> _uiState.update { it.copy(complaintToDeleteContent = event.complaint) }
            AdminPanelEvent.ConfirmDeleteComplaintContent -> handleConfirmDeleteComplaintContent()
            AdminPanelEvent.DismissDeleteComplaintContentDialog -> _uiState.update { it.copy(complaintToDeleteContent = null) }
            is AdminPanelEvent.OpenRejectComplaintDialog -> _uiState.update { it.copy(complaintToReject = event.complaint) }
            is AdminPanelEvent.ConfirmRejectComplaint -> handleConfirmRejectComplaint(event.note)
            AdminPanelEvent.DismissRejectComplaintDialog -> _uiState.update { it.copy(complaintToReject = null) }
            is AdminPanelEvent.SearchUsers -> _uiState.update { it.copy(userSearchQuery = event.query) }
            is AdminPanelEvent.FilterUsersByRole -> _uiState.update { it.copy(userRoleFilter = event.role) }
            is AdminPanelEvent.FilterUsersByStatus -> _uiState.update { it.copy(userStatusFilter = event.status) }
            is AdminPanelEvent.OpenBlockUserDialog -> _uiState.update { it.copy(userToBlock = event.user, blockComplaintId = event.complaintId) }
            is AdminPanelEvent.ConfirmBlockUser -> handleConfirmBlockUser(event.reason)
            is AdminPanelEvent.OpenUnblockUserDialog -> _uiState.update { it.copy(userToUnblock = event.user) }
            AdminPanelEvent.ConfirmUnblockUser -> handleConfirmUnblockUser()
            AdminPanelEvent.DismissUserDialog -> _uiState.update { it.copy(userToBlock = null, userToUnblock = null, blockComplaintId = null) }
            is AdminPanelEvent.ViewUserProfile -> viewModelScope.launch { effectChannel.send(AdminPanelEffect.NavigateToProfile(event.userId)) }
            AdminPanelEvent.NavigateBackClicked -> viewModelScope.launch { effectChannel.send(AdminPanelEffect.NavigateBack) }
        }
    }

    private fun checkAdminAccessAndInitialize() {
        viewModelScope.launch {
            val currentUser = observeCurrentUserUseCase().first()
            if (currentUser.role != UserRole.ADMIN) {
                effectChannel.send(AdminPanelEffect.AccessDenied)
                effectChannel.send(AdminPanelEffect.NavigateBack)
                return@launch
            }
            _uiState.update { it.copy(isAuthorizedAdmin = true) }
            loadModerationEvents()
        }
    }

    private fun handleSelectTab(tab: AdminTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        refreshCurrentTab()
    }

    private fun refreshCurrentTab() {
        when (_uiState.value.selectedTab) {
            AdminTab.MODERATION -> loadModerationEvents()
            AdminTab.CATEGORIES -> loadCategories()
            AdminTab.COMPLAINTS_AND_USERS -> {
                when (_uiState.value.complaintsUsersSubTab) {
                    ComplaintsUsersSubTab.COMPLAINTS -> loadComplaints(_uiState.value.complaintFilter)
                    ComplaintsUsersSubTab.USERS -> loadUsers()
                }
            }
        }
    }

    private fun loadModerationEvents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            getPendingModerationEventsUseCase()
                .onSuccess { events ->
                    _uiState.update { it.copy(isLoading = false, pendingEvents = events) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    private fun handleToggleEventDetails(eventId: String) {
        _uiState.update { current ->
            val newExpanded = if (current.expandedEventId == eventId) null else eventId
            current.copy(expandedEventId = newExpanded)
        }
    }

    private fun handleApproveEvent(eventId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            approveEventModerationUseCase(eventId)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            isActionLoading = false,
                            pendingEvents = state.pendingEvents.filter { it.id != eventId }
                        )
                    }
                    effectChannel.send(AdminPanelEffect.ShowMessageRes(R.string.admin_msg_event_approved))
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isActionLoading = false) }
                    effectChannel.send(AdminPanelEffect.ShowMessage(error.message ?: ""))
                }
        }
    }

    private fun handleConfirmRejectEvent(reason: String) {
        if (reason.isBlank()) {
            viewModelScope.launch {
                effectChannel.send(AdminPanelEffect.ShowMessageRes(R.string.admin_error_rejection_reason_empty))
            }
            return
        }
        val event = _uiState.value.eventToReject ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            rejectEventModerationUseCase(event.id, reason)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            isActionLoading = false,
                            eventToReject = null,
                            pendingEvents = state.pendingEvents.filter { it.id != event.id }
                        )
                    }
                    effectChannel.send(AdminPanelEffect.ShowMessageRes(R.string.admin_msg_event_rejected))
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isActionLoading = false) }
                    effectChannel.send(AdminPanelEffect.ShowMessage(error.message ?: ""))
                }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            getAdminCategoriesUseCase()
                .onSuccess { categories ->
                    _uiState.update { it.copy(isLoading = false, categories = categories) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    private fun handleConfirmSaveCategory(name: String) {
        val trimmed = name.trim()
        if (trimmed.length < 2 || trimmed.length > 80) {
            viewModelScope.launch {
                effectChannel.send(AdminPanelEffect.ShowMessageRes(R.string.admin_error_category_name_length))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            val mode = _uiState.value.categoryDialogMode
            if (mode is CategoryDialogMode.Edit) {
                updateCategoryUseCase(mode.category.id, trimmed)
                    .onSuccess {
                        _uiState.update { it.copy(isActionLoading = false, categoryDialogMode = null) }
                        effectChannel.send(AdminPanelEffect.ShowMessageRes(R.string.admin_msg_category_updated))
                        loadCategories()
                    }
                    .onFailure { error ->
                        _uiState.update { it.copy(isActionLoading = false) }
                        effectChannel.send(AdminPanelEffect.ShowMessage(error.message ?: ""))
                    }
            } else {
                createCategoryUseCase(trimmed)
                    .onSuccess {
                        _uiState.update { it.copy(isActionLoading = false, categoryDialogMode = null) }
                        effectChannel.send(AdminPanelEffect.ShowMessageRes(R.string.admin_msg_category_created))
                        loadCategories()
                    }
                    .onFailure { error ->
                        _uiState.update { it.copy(isActionLoading = false) }
                        effectChannel.send(AdminPanelEffect.ShowMessage(error.message ?: ""))
                    }
            }
        }
    }

    private fun handleConfirmDeleteCategory() {
        val category = _uiState.value.categoryToDelete ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            deleteCategoryUseCase(category.id)
                .onSuccess { action ->
                    _uiState.update { it.copy(isActionLoading = false, categoryToDelete = null) }
                    val messageRes = if (action.equals("archived", ignoreCase = true)) {
                        R.string.admin_msg_category_archived
                    } else {
                        R.string.admin_msg_category_deleted
                    }
                    effectChannel.send(AdminPanelEffect.ShowMessageRes(messageRes))
                    loadCategories()
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isActionLoading = false) }
                    effectChannel.send(AdminPanelEffect.ShowMessage(error.message ?: ""))
                }
        }
    }

    private fun handleSelectComplaintsUsersSubTab(subTab: ComplaintsUsersSubTab) {
        _uiState.update { it.copy(complaintsUsersSubTab = subTab) }
        when (subTab) {
            ComplaintsUsersSubTab.COMPLAINTS -> loadComplaints(_uiState.value.complaintFilter)
            ComplaintsUsersSubTab.USERS -> loadUsers()
        }
    }

    private fun handleFilterComplaints(filter: ComplaintFilter) {
        _uiState.update { it.copy(complaintFilter = filter) }
        loadComplaints(filter)
    }

    private fun loadComplaints(filter: ComplaintFilter) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            getAdminComplaintsUseCase(filter.apiValue)
                .onSuccess { list ->
                    _uiState.update { it.copy(isLoading = false, complaints = list) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    private fun handleConfirmDeleteComplaintContent() {
        val complaint = _uiState.value.complaintToDeleteContent ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            deleteComplaintContentUseCase(complaint.id)
                .onSuccess {
                    _uiState.update { it.copy(isActionLoading = false, complaintToDeleteContent = null) }
                    effectChannel.send(AdminPanelEffect.ShowMessageRes(R.string.admin_msg_content_deleted))
                    loadComplaints(_uiState.value.complaintFilter)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isActionLoading = false) }
                    effectChannel.send(AdminPanelEffect.ShowMessage(error.message ?: ""))
                }
        }
    }

    private fun handleConfirmRejectComplaint(note: String?) {
        val complaint = _uiState.value.complaintToReject ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            rejectComplaintUseCase(complaint.id, note)
                .onSuccess {
                    _uiState.update { it.copy(isActionLoading = false, complaintToReject = null) }
                    effectChannel.send(AdminPanelEffect.ShowMessageRes(R.string.admin_msg_complaint_rejected))
                    loadComplaints(_uiState.value.complaintFilter)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isActionLoading = false) }
                    effectChannel.send(AdminPanelEffect.ShowMessage(error.message ?: ""))
                }
        }
    }

    private fun loadUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            getAdminUsersUseCase()
                .onSuccess { list ->
                    _uiState.update { it.copy(isLoading = false, users = list) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    private fun handleConfirmBlockUser(reason: String) {
        val trimmed = reason.trim()
        if (trimmed.length < 5) {
            viewModelScope.launch {
                effectChannel.send(AdminPanelEffect.ShowMessageRes(R.string.admin_error_block_reason_length))
            }
            return
        }
        val user = _uiState.value.userToBlock ?: return
        val complaintId = _uiState.value.blockComplaintId

        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            blockUserUseCase(user.id, trimmed, complaintId)
                .onSuccess {
                    _uiState.update { it.copy(isActionLoading = false, userToBlock = null, blockComplaintId = null) }
                    effectChannel.send(AdminPanelEffect.ShowMessageRes(R.string.admin_msg_user_blocked))
                    loadUsers()
                    if (_uiState.value.complaintsUsersSubTab == ComplaintsUsersSubTab.COMPLAINTS) {
                        loadComplaints(_uiState.value.complaintFilter)
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isActionLoading = false) }
                    effectChannel.send(AdminPanelEffect.ShowMessage(error.message ?: ""))
                }
        }
    }

    private fun handleConfirmUnblockUser() {
        val user = _uiState.value.userToUnblock ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            unblockUserUseCase(user.id)
                .onSuccess {
                    _uiState.update { it.copy(isActionLoading = false, userToUnblock = null) }
                    effectChannel.send(AdminPanelEffect.ShowMessageRes(R.string.admin_msg_user_unblocked))
                    loadUsers()
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isActionLoading = false) }
                    effectChannel.send(AdminPanelEffect.ShowMessage(error.message ?: ""))
                }
        }
    }
}
