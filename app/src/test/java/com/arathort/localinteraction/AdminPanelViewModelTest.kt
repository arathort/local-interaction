package com.arathort.localinteraction

import com.arathort.localinteraction.R
import com.arathort.localinteraction.domain.model.Category
import com.arathort.localinteraction.domain.model.Complaint
import com.arathort.localinteraction.domain.model.Event
import com.arathort.localinteraction.domain.model.GeoPoint
import com.arathort.localinteraction.domain.model.User
import com.arathort.localinteraction.domain.model.UserRole
import com.arathort.localinteraction.domain.model.UserStatus
import com.arathort.localinteraction.domain.repository.AdminRepository
import com.arathort.localinteraction.domain.repository.AuthRepository
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
import com.arathort.localinteraction.presentation.admin.AdminPanelEffect
import com.arathort.localinteraction.presentation.admin.AdminPanelEvent
import com.arathort.localinteraction.presentation.admin.AdminPanelViewModel
import com.arathort.localinteraction.presentation.admin.AdminTab
import com.arathort.localinteraction.presentation.admin.CategoryDialogMode
import com.arathort.localinteraction.presentation.admin.ComplaintFilter
import com.arathort.localinteraction.presentation.admin.ComplaintsUsersSubTab
import com.arathort.localinteraction.presentation.admin.UserRoleFilter
import com.arathort.localinteraction.presentation.admin.UserStatusFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AdminPanelViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val adminUser = User(
        id = "admin-1",
        email = "user3@example.com",
        fullName = "Адміністратор",
        role = UserRole.ADMIN
    )

    private val regularUser = User(
        id = "user-1",
        email = "volunteer@example.com",
        fullName = "Волонтер",
        role = UserRole.VOLUNTEER
    )

    private val currentUserFlow = MutableStateFlow(adminUser)

    private val sampleEvents = mutableListOf<Event>()
    private val sampleCategories = mutableListOf<Category>()
    private val sampleComplaints = mutableListOf<Complaint>()
    private val sampleUsers = mutableListOf<User>()

    private lateinit var fakeAdminRepository: AdminRepository
    private lateinit var fakeAuthRepository: AuthRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        currentUserFlow.value = adminUser

        sampleEvents.clear()
        sampleEvents.addAll(
            listOf(
                Event(
                    id = "event-1",
                    title = "Прибирання пляжу",
                    eventDate = "20.11.2026 10:00",
                    location = GeoPoint(address = "Оболонь", latitude = 50.5, longitude = 30.5),
                    volunteerQuota = 15
                ),
                Event(
                    id = "event-2",
                    title = "Допомога притулку",
                    eventDate = "22.11.2026 12:00",
                    location = GeoPoint(address = "Поділ", latitude = 50.47, longitude = 30.51),
                    volunteerQuota = 5
                )
            )
        )

        sampleCategories.clear()
        sampleCategories.addAll(
            listOf(
                Category(id = "cat-1", name = "Екологія", boundEventsCount = 3),
                Category(id = "cat-2", name = "Тварини", boundEventsCount = 1)
            )
        )

        sampleComplaints.clear()
        sampleComplaints.addAll(
            listOf(
                Complaint(
                    id = "comp-1",
                    reporterId = "user-10",
                    reporterName = "Іван",
                    violatorId = "user-20",
                    violatorName = "Олег",
                    reason = "Спам у коментарях",
                    targetSummary = "Коментар",
                    targetType = "Comment",
                    status = "Pending"
                )
            )
        )

        sampleUsers.clear()
        sampleUsers.addAll(
            listOf(
                User(
                    id = "user-1",
                    email = "vol1@example.com",
                    fullName = "Оксана",
                    role = UserRole.VOLUNTEER,
                    status = UserStatus.ACTIVE
                ),
                User(
                    id = "user-2",
                    email = "org1@example.com",
                    fullName = "Тарас",
                    role = UserRole.ORGANIZER,
                    status = UserStatus.BLOCKED,
                    blockReason = "Порушення правил"
                )
            )
        )

        fakeAuthRepository = object : AuthRepository {
            override val currentUser: Flow<User> get() = currentUserFlow
            override suspend fun login(email: String, password: String): Result<User> = Result.success(adminUser)
            override suspend fun register(
                fullName: String,
                email: String,
                password: String,
                role: UserRole,
                acceptedPrivacyPolicy: Boolean
            ): Result<User> = Result.success(adminUser)
            override suspend fun logout(): Result<Unit> = Result.success(Unit)
            override suspend fun restoreSession(): Result<User?> = Result.success(currentUserFlow.value)
        }

        fakeAdminRepository = object : AdminRepository {
            override fun observePendingModerationEvents(): Flow<List<Event>> = flowOf(sampleEvents)
            override suspend fun getPendingModerationEvents(): Result<List<Event>> = Result.success(sampleEvents.toList())
            override suspend fun approveEventModeration(eventId: String): Result<Event> {
                val found = sampleEvents.find { it.id == eventId } ?: return Result.failure(NoSuchElementException())
                sampleEvents.remove(found)
                return Result.success(found)
            }
            override suspend fun rejectEventModeration(eventId: String, reason: String): Result<Event> {
                val found = sampleEvents.find { it.id == eventId } ?: return Result.failure(NoSuchElementException())
                sampleEvents.remove(found)
                return Result.success(found)
            }
            override fun observeAdminCategories(): Flow<List<Category>> = flowOf(sampleCategories)
            override suspend fun getAdminCategories(): Result<List<Category>> = Result.success(sampleCategories.toList())
            override suspend fun createCategory(name: String): Result<Category> {
                val newCat = Category(id = "cat-${sampleCategories.size + 1}", name = name)
                sampleCategories.add(newCat)
                return Result.success(newCat)
            }
            override suspend fun updateCategory(categoryId: String, name: String): Result<Category> {
                val index = sampleCategories.indexOfFirst { it.id == categoryId }
                if (index == -1) return Result.failure(NoSuchElementException())
                val updated = sampleCategories[index].copy(name = name)
                sampleCategories[index] = updated
                return Result.success(updated)
            }
            override suspend fun archiveCategory(categoryId: String): Result<Category> {
                val index = sampleCategories.indexOfFirst { it.id == categoryId }
                if (index == -1) return Result.failure(NoSuchElementException())
                val archived = sampleCategories[index].copy(isArchived = true)
                sampleCategories[index] = archived
                return Result.success(archived)
            }
            override suspend fun deleteCategory(categoryId: String): Result<String> {
                sampleCategories.removeAll { it.id == categoryId }
                return Result.success("deleted")
            }
            override fun observeActiveComplaints(): Flow<List<Complaint>> = flowOf(sampleComplaints)
            override suspend fun getAdminComplaints(statusFilter: String?): Result<List<Complaint>> =
                Result.success(sampleComplaints.toList())
            override suspend fun rejectComplaint(complaintId: String, adminNote: String?): Result<Complaint> {
                val comp = sampleComplaints.find { it.id == complaintId } ?: return Result.failure(NoSuchElementException())
                val rejected = comp.copy(status = "Rejected", adminNote = adminNote)
                val index = sampleComplaints.indexOf(comp)
                sampleComplaints[index] = rejected
                return Result.success(rejected)
            }
            override suspend fun deleteComplaintContent(complaintId: String): Result<Complaint> {
                val comp = sampleComplaints.find { it.id == complaintId } ?: return Result.failure(NoSuchElementException())
                val resolved = comp.copy(status = "Resolved", isResolved = true)
                val index = sampleComplaints.indexOf(comp)
                sampleComplaints[index] = resolved
                return Result.success(resolved)
            }
            override suspend fun getAdminUsers(role: String?, status: String?): Result<List<User>> =
                Result.success(sampleUsers.toList())
            override suspend fun blockUser(userId: String, reason: String, complaintId: String?): Result<User> {
                val user = sampleUsers.find { it.id == userId } ?: return Result.failure(NoSuchElementException())
                val blocked = user.copy(status = UserStatus.BLOCKED, blockReason = reason)
                val index = sampleUsers.indexOf(user)
                sampleUsers[index] = blocked
                return Result.success(blocked)
            }
            override suspend fun unblockUser(userId: String): Result<User> {
                val user = sampleUsers.find { it.id == userId } ?: return Result.failure(NoSuchElementException())
                val unblocked = user.copy(status = UserStatus.ACTIVE, blockReason = null)
                val index = sampleUsers.indexOf(user)
                sampleUsers[index] = unblocked
                return Result.success(unblocked)
            }
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): AdminPanelViewModel = AdminPanelViewModel(
        observeCurrentUserUseCase = ObserveCurrentUserUseCase(fakeAuthRepository),
        getPendingModerationEventsUseCase = GetPendingModerationEventsUseCase(fakeAdminRepository),
        approveEventModerationUseCase = ApproveEventModerationUseCase(fakeAdminRepository),
        rejectEventModerationUseCase = RejectEventModerationUseCase(fakeAdminRepository),
        getAdminCategoriesUseCase = GetAdminCategoriesUseCase(fakeAdminRepository),
        createCategoryUseCase = CreateCategoryUseCase(fakeAdminRepository),
        updateCategoryUseCase = UpdateCategoryUseCase(fakeAdminRepository),
        deleteCategoryUseCase = DeleteCategoryUseCase(fakeAdminRepository),
        getAdminComplaintsUseCase = GetAdminComplaintsUseCase(fakeAdminRepository),
        rejectComplaintUseCase = RejectComplaintUseCase(fakeAdminRepository),
        deleteComplaintContentUseCase = DeleteComplaintContentUseCase(fakeAdminRepository),
        getAdminUsersUseCase = GetAdminUsersUseCase(fakeAdminRepository),
        blockUserUseCase = BlockUserUseCase(fakeAdminRepository),
        unblockUserUseCase = UnblockUserUseCase(fakeAdminRepository)
    )

    @Test
    fun nonAdminUser_accessDeniedAndNavigatesBack() = runTest {
        currentUserFlow.value = regularUser
        val viewModel = createViewModel()

        val effects = mutableListOf<AdminPanelEffect>()
        val effectJob = backgroundScope.launch(testDispatcher) {
            viewModel.effect.collect { effects.add(it) }
        }
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isAuthorizedAdmin)
        assertTrue(effects.any { it is AdminPanelEffect.AccessDenied })
        assertTrue(effects.any { it is AdminPanelEffect.NavigateBack })

        effectJob.cancel()
    }

    @Test
    fun adminUser_accessGrantedAndLoadsModerationEvents() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isAuthorizedAdmin)
        assertEquals(2, viewModel.uiState.value.pendingEvents.size)
        assertEquals("event-1", viewModel.uiState.value.pendingEvents[0].id)
    }

    @Test
    fun approveEvent_removesEventAndEmitsMessage() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val effects = mutableListOf<AdminPanelEffect>()
        val effectJob = backgroundScope.launch(testDispatcher) {
            viewModel.effect.collect { effects.add(it) }
        }

        viewModel.onEvent(AdminPanelEvent.ApproveEvent("event-1"))
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.pendingEvents.size)
        assertEquals("event-2", viewModel.uiState.value.pendingEvents[0].id)
        assertTrue(effects.any { it is AdminPanelEffect.ShowMessageRes && it.stringResId == R.string.admin_msg_event_approved })

        effectJob.cancel()
    }

    @Test
    fun rejectEvent_withBlankReason_showsValidationError() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val effects = mutableListOf<AdminPanelEffect>()
        val effectJob = backgroundScope.launch(testDispatcher) {
            viewModel.effect.collect { effects.add(it) }
        }

        viewModel.onEvent(AdminPanelEvent.OpenRejectEventDialog(sampleEvents[0]))
        viewModel.onEvent(AdminPanelEvent.ConfirmRejectEvent("   "))
        advanceUntilIdle()

        assertTrue(effects.any { it is AdminPanelEffect.ShowMessageRes && it.stringResId == R.string.admin_error_rejection_reason_empty })
        assertEquals(2, viewModel.uiState.value.pendingEvents.size)

        effectJob.cancel()
    }

    @Test
    fun rejectEvent_withValidReason_removesEventAndEmitsSuccess() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val effects = mutableListOf<AdminPanelEffect>()
        val effectJob = backgroundScope.launch(testDispatcher) {
            viewModel.effect.collect { effects.add(it) }
        }

        viewModel.onEvent(AdminPanelEvent.OpenRejectEventDialog(sampleEvents[0]))
        viewModel.onEvent(AdminPanelEvent.ConfirmRejectEvent("Порушення правил спільноти"))
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.pendingEvents.size)
        assertNull(viewModel.uiState.value.eventToReject)
        assertTrue(effects.any { it is AdminPanelEffect.ShowMessageRes && it.stringResId == R.string.admin_msg_event_rejected })

        effectJob.cancel()
    }

    @Test
    fun selectCategoriesTab_loadsCategories() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(AdminPanelEvent.SelectTab(AdminTab.CATEGORIES))
        advanceUntilIdle()

        assertEquals(AdminTab.CATEGORIES, viewModel.uiState.value.selectedTab)
        assertEquals(2, viewModel.uiState.value.categories.size)
    }

    @Test
    fun createCategory_withValidName_createsAndRefreshes() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(AdminPanelEvent.SelectTab(AdminTab.CATEGORIES))
        advanceUntilIdle()

        viewModel.onEvent(AdminPanelEvent.OpenCreateCategoryDialog)
        assertEquals(CategoryDialogMode.Create, viewModel.uiState.value.categoryDialogMode)

        viewModel.onEvent(AdminPanelEvent.ConfirmSaveCategory("Культура"))
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.categoryDialogMode)
        assertEquals(3, viewModel.uiState.value.categories.size)
        assertEquals("Культура", viewModel.uiState.value.categories[2].name)
    }

    @Test
    fun createCategory_withTooShortName_showsError() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val effects = mutableListOf<AdminPanelEffect>()
        val effectJob = backgroundScope.launch(testDispatcher) {
            viewModel.effect.collect { effects.add(it) }
        }

        viewModel.onEvent(AdminPanelEvent.SelectTab(AdminTab.CATEGORIES))
        advanceUntilIdle()

        viewModel.onEvent(AdminPanelEvent.ConfirmSaveCategory("X"))
        advanceUntilIdle()

        assertTrue(effects.any { it is AdminPanelEffect.ShowMessageRes && it.stringResId == R.string.admin_error_category_name_length })
        assertEquals(2, viewModel.uiState.value.categories.size)

        effectJob.cancel()
    }

    @Test
    fun deleteCategory_deletesAndRefreshes() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(AdminPanelEvent.SelectTab(AdminTab.CATEGORIES))
        advanceUntilIdle()

        viewModel.onEvent(AdminPanelEvent.OpenDeleteCategoryDialog(sampleCategories[0]))
        viewModel.onEvent(AdminPanelEvent.ConfirmDeleteCategory)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.categoryToDelete)
        assertEquals(1, viewModel.uiState.value.categories.size)
    }

    @Test
    fun selectComplaintsUsersSubTab_andDeleteComplaintContent() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(AdminPanelEvent.SelectTab(AdminTab.COMPLAINTS_AND_USERS))
        advanceUntilIdle()

        viewModel.onEvent(AdminPanelEvent.SelectComplaintsUsersSubTab(ComplaintsUsersSubTab.COMPLAINTS))
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.complaints.size)

        viewModel.onEvent(AdminPanelEvent.OpenDeleteComplaintContentDialog(sampleComplaints[0]))
        viewModel.onEvent(AdminPanelEvent.ConfirmDeleteComplaintContent)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.complaintToDeleteContent)
        assertTrue(sampleComplaints[0].isResolved)
    }

    @Test
    fun rejectComplaint_updatesComplaintStatus() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(AdminPanelEvent.SelectTab(AdminTab.COMPLAINTS_AND_USERS))
        advanceUntilIdle()

        viewModel.onEvent(AdminPanelEvent.SelectComplaintsUsersSubTab(ComplaintsUsersSubTab.COMPLAINTS))
        advanceUntilIdle()

        viewModel.onEvent(AdminPanelEvent.OpenRejectComplaintDialog(sampleComplaints[0]))
        viewModel.onEvent(AdminPanelEvent.ConfirmRejectComplaint("Не підтверджено"))
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.complaintToReject)
        assertEquals("Rejected", sampleComplaints[0].status)
        assertEquals("Не підтверджено", sampleComplaints[0].adminNote)
    }

    @Test
    fun selectUsersSubTab_searchAndFilters() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(AdminPanelEvent.SelectTab(AdminTab.COMPLAINTS_AND_USERS))
        advanceUntilIdle()

        viewModel.onEvent(AdminPanelEvent.SelectComplaintsUsersSubTab(ComplaintsUsersSubTab.USERS))
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.users.size)
        assertEquals(2, viewModel.uiState.value.filteredUsers.size)

        viewModel.onEvent(AdminPanelEvent.SearchUsers("Оксана"))
        assertEquals(1, viewModel.uiState.value.filteredUsers.size)
        assertEquals("user-1", viewModel.uiState.value.filteredUsers[0].id)

        viewModel.onEvent(AdminPanelEvent.SearchUsers(""))
        viewModel.onEvent(AdminPanelEvent.FilterUsersByRole(UserRoleFilter.ORGANIZER))
        assertEquals(1, viewModel.uiState.value.filteredUsers.size)
        assertEquals("user-2", viewModel.uiState.value.filteredUsers[0].id)

        viewModel.onEvent(AdminPanelEvent.FilterUsersByRole(UserRoleFilter.ALL))
        viewModel.onEvent(AdminPanelEvent.FilterUsersByStatus(UserStatusFilter.BLOCKED))
        assertEquals(1, viewModel.uiState.value.filteredUsers.size)
        assertEquals("user-2", viewModel.uiState.value.filteredUsers[0].id)
    }

    @Test
    fun blockUser_withShortReason_showsValidationError() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val effects = mutableListOf<AdminPanelEffect>()
        val effectJob = backgroundScope.launch(testDispatcher) {
            viewModel.effect.collect { effects.add(it) }
        }

        viewModel.onEvent(AdminPanelEvent.SelectTab(AdminTab.COMPLAINTS_AND_USERS))
        advanceUntilIdle()

        viewModel.onEvent(AdminPanelEvent.SelectComplaintsUsersSubTab(ComplaintsUsersSubTab.USERS))
        advanceUntilIdle()

        viewModel.onEvent(AdminPanelEvent.OpenBlockUserDialog(sampleUsers[0]))
        viewModel.onEvent(AdminPanelEvent.ConfirmBlockUser("bad"))
        advanceUntilIdle()

        assertTrue(effects.any { it is AdminPanelEffect.ShowMessageRes && it.stringResId == R.string.admin_error_block_reason_length })
        assertEquals(UserStatus.ACTIVE, sampleUsers[0].status)

        effectJob.cancel()
    }

    @Test
    fun blockUser_withValidReason_blocksAndRefreshes() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(AdminPanelEvent.SelectTab(AdminTab.COMPLAINTS_AND_USERS))
        advanceUntilIdle()

        viewModel.onEvent(AdminPanelEvent.SelectComplaintsUsersSubTab(ComplaintsUsersSubTab.USERS))
        advanceUntilIdle()

        viewModel.onEvent(AdminPanelEvent.OpenBlockUserDialog(sampleUsers[0]))
        viewModel.onEvent(AdminPanelEvent.ConfirmBlockUser("Систематичний спам"))
        advanceUntilIdle()

        assertEquals(UserStatus.BLOCKED, sampleUsers[0].status)
        assertEquals("Систематичний спам", sampleUsers[0].blockReason)
    }

    @Test
    fun unblockUser_unblocksAndRefreshes() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(AdminPanelEvent.SelectTab(AdminTab.COMPLAINTS_AND_USERS))
        advanceUntilIdle()

        viewModel.onEvent(AdminPanelEvent.SelectComplaintsUsersSubTab(ComplaintsUsersSubTab.USERS))
        advanceUntilIdle()

        viewModel.onEvent(AdminPanelEvent.OpenUnblockUserDialog(sampleUsers[1]))
        viewModel.onEvent(AdminPanelEvent.ConfirmUnblockUser)
        advanceUntilIdle()

        assertEquals(UserStatus.ACTIVE, sampleUsers[1].status)
        assertNull(sampleUsers[1].blockReason)
    }
}
