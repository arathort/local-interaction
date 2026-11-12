package com.arathort.localinteraction.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arathort.localinteraction.domain.model.User
import com.arathort.localinteraction.domain.model.UserRole
import com.arathort.localinteraction.domain.repository.AuthRepository
import com.arathort.localinteraction.domain.usecase.GetUnreadNotificationsCountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainNavigationViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val getUnreadNotificationsCountUseCase: GetUnreadNotificationsCountUseCase
) : ViewModel() {

    val currentUser: StateFlow<User> = authRepository.currentUser
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = User(id = "user-guest", role = UserRole.GUEST)
        )

    val unreadNotificationsCount: StateFlow<Int> = getUnreadNotificationsCountUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                if (user.role != UserRole.GUEST) {
                    getUnreadNotificationsCountUseCase.refresh()
                }
            }
        }
    }
}
