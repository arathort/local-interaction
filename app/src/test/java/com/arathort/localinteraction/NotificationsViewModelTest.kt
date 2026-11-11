package com.arathort.localinteraction

import com.arathort.localinteraction.domain.model.AppNotification
import com.arathort.localinteraction.domain.model.NotificationFeed
import com.arathort.localinteraction.domain.model.NotificationType
import com.arathort.localinteraction.domain.model.User
import com.arathort.localinteraction.domain.model.UserRole
import com.arathort.localinteraction.domain.repository.AuthRepository
import com.arathort.localinteraction.domain.repository.NotificationRepository
import com.arathort.localinteraction.domain.usecase.GetNotificationsUseCase
import com.arathort.localinteraction.domain.usecase.MarkAllNotificationsAsReadUseCase
import com.arathort.localinteraction.domain.usecase.MarkNotificationAsReadUseCase
import com.arathort.localinteraction.presentation.notifications.NotificationsEffect
import com.arathort.localinteraction.presentation.notifications.NotificationsEvent
import com.arathort.localinteraction.presentation.notifications.NotificationsFilter
import com.arathort.localinteraction.presentation.notifications.NotificationsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var fakeNotificationRepository: FakeNotificationRepository
    private lateinit var viewModel: NotificationsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeAuthRepository = FakeAuthRepository()
        fakeNotificationRepository = FakeNotificationRepository()
        viewModel = NotificationsViewModel(
            authRepository = fakeAuthRepository,
            getNotificationsUseCase = GetNotificationsUseCase(fakeNotificationRepository),
            markNotificationAsReadUseCase = MarkNotificationAsReadUseCase(fakeNotificationRepository),
            markAllNotificationsAsReadUseCase = MarkAllNotificationsAsReadUseCase(fakeNotificationRepository)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state for authenticated user loads notifications`() = runTest {
        val user = User(id = "user-1", role = UserRole.VOLUNTEER)
        fakeAuthRepository.emitUser(user)

        val state = viewModel.uiState.value
        assertEquals(user, state.currentUser)
        assertEquals(2, state.notifications.size)
        assertEquals(1, state.unreadCount)
        assertFalse(state.isLoading)
    }

    @Test
    fun `guest user state does not fetch notifications`() = runTest {
        fakeAuthRepository.emitUser(User(id = "guest", role = UserRole.GUEST))

        val state = viewModel.uiState.value
        assertEquals(UserRole.GUEST, state.currentUser.role)
        assertTrue(state.notifications.isEmpty())
        assertEquals(0, state.unreadCount)
    }

    @Test
    fun `filter changes to UNREAD_ONLY updates state and calls repository`() = runTest {
        fakeAuthRepository.emitUser(User(id = "user-1", role = UserRole.VOLUNTEER))

        viewModel.onEvent(NotificationsEvent.FilterChanged(NotificationsFilter.UNREAD_ONLY))

        val state = viewModel.uiState.value
        assertEquals(NotificationsFilter.UNREAD_ONLY, state.filter)
        assertEquals(1, state.notifications.size)
        assertFalse(state.notifications.first().isRead)
    }

    @Test
    fun `mark notification as read updates local item and decrements unread count`() = runTest {
        fakeAuthRepository.emitUser(User(id = "user-1", role = UserRole.VOLUNTEER))

        viewModel.onEvent(NotificationsEvent.MarkAsReadClicked("notif-1"))

        val state = viewModel.uiState.value
        val updated = state.notifications.first { it.id == "notif-1" }
        assertTrue(updated.isRead)
        assertEquals(0, state.unreadCount)
    }

    @Test
    fun `mark all notifications as read marks all items read and clears unread count`() = runTest {
        fakeAuthRepository.emitUser(User(id = "user-1", role = UserRole.VOLUNTEER))

        viewModel.onEvent(NotificationsEvent.MarkAllAsReadClicked)

        val state = viewModel.uiState.value
        assertTrue(state.notifications.all { it.isRead })
        assertEquals(0, state.unreadCount)
    }

    @Test
    fun `notification click with relatedEntityId triggers NavigateToEventDetails`() = runTest {
        fakeAuthRepository.emitUser(User(id = "user-1", role = UserRole.VOLUNTEER))
        val notification = fakeNotificationRepository.notifications.first()

        viewModel.onEvent(NotificationsEvent.NotificationClicked(notification))

        val effect = viewModel.effect.first()
        assertTrue(effect is NotificationsEffect.NavigateToEventDetails)
        assertEquals("event-123", (effect as NotificationsEffect.NavigateToEventDetails).eventId)
    }

    @Test
    fun `LoginClicked triggers NavigateToAuth effect`() = runTest {
        viewModel.onEvent(NotificationsEvent.LoginClicked)

        val effect = viewModel.effect.first()
        assertEquals(NotificationsEffect.NavigateToAuth, effect)
    }

    private class FakeAuthRepository : AuthRepository {
        private val _currentUser = MutableStateFlow(User(id = "guest", role = UserRole.GUEST))
        override val currentUser: Flow<User> = _currentUser.asStateFlow()

        fun emitUser(user: User) {
            _currentUser.value = user
        }

        override suspend fun login(email: String, password: String): Result<User> =
            Result.success(User(id = "user-1", role = UserRole.VOLUNTEER))

        override suspend fun register(
            fullName: String,
            email: String,
            password: String,
            role: UserRole,
            acceptedPrivacyPolicy: Boolean
        ): Result<User> = Result.success(User(id = "user-1", role = role))

        override suspend fun logout(): Result<Unit> = Result.success(Unit)

        override suspend fun restoreSession(): Result<User?> = Result.success(null)
    }

    private class FakeNotificationRepository : NotificationRepository {
        val notifications = mutableListOf(
            AppNotification(
                id = "notif-1",
                userId = "user-1",
                type = NotificationType.APPLICATION_STATUS,
                title = "Заявку схвалено",
                message = "Вашу заявку схвалено",
                relatedEntityId = "event-123",
                isRead = false,
                createdAt = "2026-11-11T10:00:00"
            ),
            AppNotification(
                id = "notif-2",
                userId = "user-1",
                type = NotificationType.NEW_COMMENT,
                title = "Новий коментар",
                message = "Організатор залишив коментар",
                relatedEntityId = "event-123",
                isRead = true,
                createdAt = "2026-11-10T12:00:00"
            )
        )

        private val _unreadCount = MutableStateFlow(1)
        override val unreadCount: Flow<Int> = _unreadCount.asStateFlow()

        override suspend fun getNotifications(
            unreadOnly: Boolean,
            skip: Int,
            limit: Int
        ): Result<NotificationFeed> {
            val list = if (unreadOnly) notifications.filter { !it.isRead } else notifications
            val count = notifications.count { !it.isRead }
            _unreadCount.value = count
            return Result.success(NotificationFeed(unreadCount = count, items = list))
        }

        override suspend fun markNotificationAsRead(notificationId: String): Result<AppNotification> {
            val index = notifications.indexOfFirst { it.id == notificationId }
            if (index >= 0) {
                val updated = notifications[index].copy(isRead = true)
                notifications[index] = updated
                _unreadCount.value = maxOf(0, _unreadCount.value - 1)
                return Result.success(updated)
            }
            return Result.failure(NoSuchElementException())
        }

        override suspend fun markAllNotificationsAsRead(): Result<Unit> {
            for (i in notifications.indices) {
                notifications[i] = notifications[i].copy(isRead = true)
            }
            _unreadCount.value = 0
            return Result.success(Unit)
        }

        override suspend fun refreshUnreadCount(): Result<Int> {
            val count = notifications.count { !it.isRead }
            _unreadCount.value = count
            return Result.success(count)
        }
    }
}
