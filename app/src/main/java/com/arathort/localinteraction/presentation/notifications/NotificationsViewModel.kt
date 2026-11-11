package com.arathort.localinteraction.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arathort.localinteraction.R
import com.arathort.localinteraction.domain.model.AppNotification
import com.arathort.localinteraction.domain.model.UserRole
import com.arathort.localinteraction.domain.repository.AuthRepository
import com.arathort.localinteraction.domain.usecase.GetNotificationsUseCase
import com.arathort.localinteraction.domain.usecase.MarkAllNotificationsAsReadUseCase
import com.arathort.localinteraction.domain.usecase.MarkNotificationAsReadUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val markNotificationAsReadUseCase: MarkNotificationAsReadUseCase,
    private val markAllNotificationsAsReadUseCase: MarkAllNotificationsAsReadUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsState())
    val uiState: StateFlow<NotificationsState> = _uiState.asStateFlow()

    private val _effect = Channel<NotificationsEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.update { it.copy(currentUser = user) }
                if (user.role == UserRole.GUEST) {
                    _uiState.update {
                        it.copy(
                            notifications = emptyList(),
                            unreadCount = 0,
                            isLoading = false
                        )
                    }
                } else {
                    loadNotifications(isRefresh = false)
                }
            }
        }
    }

    fun onEvent(event: NotificationsEvent) {
        when (event) {
            is NotificationsEvent.FilterChanged -> {
                if (_uiState.value.filter != event.filter) {
                    _uiState.update { it.copy(filter = event.filter) }
                    loadNotifications(isRefresh = false)
                }
            }
            NotificationsEvent.RefreshRequested -> loadNotifications(isRefresh = true)
            is NotificationsEvent.NotificationClicked -> handleNotificationClicked(event.notification)
            is NotificationsEvent.MarkAsReadClicked -> handleMarkAsRead(event.notificationId)
            NotificationsEvent.MarkAllAsReadClicked -> handleMarkAllAsRead()
            NotificationsEvent.LoginClicked -> {
                viewModelScope.launch {
                    _effect.send(NotificationsEffect.NavigateToAuth)
                }
            }
        }
    }

    private fun loadNotifications(isRefresh: Boolean) {
        val currentUser = _uiState.value.currentUser
        if (currentUser.role == UserRole.GUEST) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                if (isRefresh) it.copy(isRefreshing = true, errorMessage = null)
                else it.copy(isLoading = true, errorMessage = null)
            }

            val unreadOnly = _uiState.value.filter == NotificationsFilter.UNREAD_ONLY
            getNotificationsUseCase(unreadOnly = unreadOnly)
                .onSuccess { feed ->
                    _uiState.update {
                        it.copy(
                            notifications = feed.items,
                            unreadCount = feed.unreadCount,
                            isLoading = false,
                            isRefreshing = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = error.message
                        )
                    }
                    val msg = error.message.orEmpty()
                    if (msg.isNotBlank()) {
                        _effect.send(NotificationsEffect.ShowSnackbarText(msg))
                    } else {
                        _effect.send(NotificationsEffect.ShowSnackbar(R.string.error_unknown))
                    }
                }
        }
    }

    private fun handleNotificationClicked(notification: AppNotification) {
        if (!notification.isRead) {
            handleMarkAsRead(notification.id)
        }
        val entityId = notification.relatedEntityId
        if (!entityId.isNullOrBlank()) {
            viewModelScope.launch {
                _effect.send(NotificationsEffect.NavigateToEventDetails(entityId))
            }
        }
    }

    private fun handleMarkAsRead(notificationId: String) {
        viewModelScope.launch {
            markNotificationAsReadUseCase(notificationId)
                .onSuccess { updated ->
                    _uiState.update { state ->
                        val updatedList = state.notifications.map { item ->
                            if (item.id == updated.id) updated else item
                        }
                        val newUnread = maxOf(0, state.unreadCount - 1)
                        state.copy(
                            notifications = if (state.filter == NotificationsFilter.UNREAD_ONLY) {
                                updatedList.filter { !it.isRead }
                            } else {
                                updatedList
                            },
                            unreadCount = newUnread
                        )
                    }
                }
                .onFailure { error ->
                    val msg = error.message.orEmpty()
                    if (msg.isNotBlank()) {
                        _effect.send(NotificationsEffect.ShowSnackbarText(msg))
                    }
                }
        }
    }

    private fun handleMarkAllAsRead() {
        if (_uiState.value.isMarkingAllAsRead) return

        viewModelScope.launch {
            _uiState.update { it.copy(isMarkingAllAsRead = true) }
            markAllNotificationsAsReadUseCase()
                .onSuccess {
                    _uiState.update { state ->
                        val updatedList = state.notifications.map { it.copy(isRead = true) }
                        state.copy(
                            notifications = if (state.filter == NotificationsFilter.UNREAD_ONLY) {
                                emptyList()
                            } else {
                                updatedList
                            },
                            unreadCount = 0,
                            isMarkingAllAsRead = false
                        )
                    }
                    _effect.send(NotificationsEffect.ShowSnackbar(R.string.notifications_all_read_success))
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isMarkingAllAsRead = false) }
                    val msg = error.message.orEmpty()
                    if (msg.isNotBlank()) {
                        _effect.send(NotificationsEffect.ShowSnackbarText(msg))
                    } else {
                        _effect.send(NotificationsEffect.ShowSnackbar(R.string.error_unknown))
                    }
                }
        }
    }
}
