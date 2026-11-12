package com.arathort.localinteraction

import com.arathort.localinteraction.domain.model.Review
import com.arathort.localinteraction.domain.model.ReviewTargetInfo
import com.arathort.localinteraction.domain.model.UserRole
import com.arathort.localinteraction.domain.repository.ReviewRepository
import com.arathort.localinteraction.domain.usecase.GetReviewFormInfoUseCase
import com.arathort.localinteraction.domain.usecase.SubmitReviewUseCase
import com.arathort.localinteraction.presentation.review.WriteReviewEffect
import com.arathort.localinteraction.presentation.review.WriteReviewEvent
import com.arathort.localinteraction.presentation.review.WriteReviewViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
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
class WriteReviewViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private var formInfoResult: Result<ReviewTargetInfo> = Result.success(
        ReviewTargetInfo(
            eventId = "event-1",
            eventTitle = "Прибирання парку",
            revieweeId = "user-org",
            revieweeName = "Олександр Коваленко",
            revieweeRole = UserRole.ORGANIZER,
            alreadyReviewed = false,
            canReview = true
        )
    )

    private var submitReviewResult: Result<Review> = Result.success(
        Review(
            id = "review-1",
            eventId = "event-1",
            authorId = "user-vol",
            authorName = "Волонтер Марія",
            rating = 5,
            comment = "Все було чудово!",
            createdAt = "2026-11-09T10:00:00Z",
            targetUserId = "user-org",
            eventTitle = "Прибирання парку",
            newAverageRating = 4.8
        )
    )

    private var lastSubmittedTargetRole: UserRole? = null
    private var lastSubmittedRating: Int? = null
    private var lastSubmittedComment: String? = null

    private lateinit var fakeReviewRepository: ReviewRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        lastSubmittedTargetRole = null
        lastSubmittedRating = null
        lastSubmittedComment = null

        formInfoResult = Result.success(
            ReviewTargetInfo(
                eventId = "event-1",
                eventTitle = "Прибирання парку",
                revieweeId = "user-org",
                revieweeName = "Олександр Коваленко",
                revieweeRole = UserRole.ORGANIZER,
                alreadyReviewed = false,
                canReview = true
            )
        )

        submitReviewResult = Result.success(
            Review(
                id = "review-1",
                eventId = "event-1",
                authorId = "user-vol",
                authorName = "Волонтер Марія",
                rating = 5,
                comment = "Все було чудово!",
                createdAt = "2026-11-09T10:00:00Z",
                targetUserId = "user-org",
                eventTitle = "Прибирання парку",
                newAverageRating = 4.8
            )
        )

        fakeReviewRepository = object : ReviewRepository {
            override fun observeReviewsForUser(userId: String): Flow<List<Review>> = emptyFlow()

            override suspend fun getReviewFormInfo(eventId: String, targetUserId: String): Result<ReviewTargetInfo> =
                formInfoResult

            override suspend fun submitReview(
                eventId: String,
                targetUserId: String,
                rating: Int,
                comment: String,
                targetRole: UserRole
            ): Result<Review> {
                lastSubmittedRating = rating
                lastSubmittedComment = comment
                lastSubmittedTargetRole = targetRole
                return submitReviewResult
            }

            override suspend fun getEventReviews(eventId: String): Result<List<Review>> =
                Result.success(emptyList())
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        eventId: String = "event-1",
        eventTitle: String = "Прибирання парку",
        targetUserId: String = "user-org",
        targetUserName: String = "Олександр Коваленко"
    ): WriteReviewViewModel = WriteReviewViewModel(
        eventId = eventId,
        initialEventTitle = eventTitle,
        targetUserId = targetUserId,
        initialTargetUserName = targetUserName,
        getReviewFormInfoUseCase = GetReviewFormInfoUseCase(fakeReviewRepository),
        submitReviewUseCase = SubmitReviewUseCase(fakeReviewRepository)
    )

    @Test
    fun initialization_loadsFormInfoAndUpdatesState() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Прибирання парку", state.eventTitle)
        assertEquals("Олександр Коваленко", state.targetUserName)
        assertEquals(UserRole.ORGANIZER, state.targetRole)
        assertFalse(state.alreadyReviewed)
        assertTrue(state.canReview)
    }

    @Test
    fun whenAlreadyReviewed_submittingIsBlocked() = runTest {
        formInfoResult = Result.success(
            ReviewTargetInfo(
                eventId = "event-1",
                eventTitle = "Прибирання парку",
                revieweeId = "user-org",
                revieweeName = "Олександр Коваленко",
                revieweeRole = UserRole.ORGANIZER,
                alreadyReviewed = true,
                canReview = true
            )
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        val effects = mutableListOf<WriteReviewEffect>()
        val effectJob = backgroundScope.launch(testDispatcher) {
            viewModel.effect.collect { effects.add(it) }
        }

        val state = viewModel.uiState.value
        assertTrue(state.alreadyReviewed)
        assertFalse(state.isSubmitEnabled)

        viewModel.onEvent(WriteReviewEvent.RatingChanged(5))
        viewModel.onEvent(WriteReviewEvent.CommentChanged("Чудова співпраця"))
        viewModel.onEvent(WriteReviewEvent.SubmitClicked)
        advanceUntilIdle()

        assertNull(lastSubmittedRating)
        assertTrue(effects.any { it is WriteReviewEffect.ShowSnackbar && it.stringResId == R.string.error_review_already_submitted })

        effectJob.cancel()
    }

    @Test
    fun whenCannotReview_submittingIsBlocked() = runTest {
        formInfoResult = Result.success(
            ReviewTargetInfo(
                eventId = "event-1",
                eventTitle = "Прибирання парку",
                revieweeId = "user-org",
                revieweeName = "Олександр Коваленко",
                revieweeRole = UserRole.ORGANIZER,
                alreadyReviewed = false,
                canReview = false
            )
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        val effects = mutableListOf<WriteReviewEffect>()
        val effectJob = backgroundScope.launch(testDispatcher) {
            viewModel.effect.collect { effects.add(it) }
        }

        val state = viewModel.uiState.value
        assertFalse(state.canReview)
        assertFalse(state.isSubmitEnabled)

        viewModel.onEvent(WriteReviewEvent.RatingChanged(4))
        viewModel.onEvent(WriteReviewEvent.CommentChanged("Все добре"))
        viewModel.onEvent(WriteReviewEvent.SubmitClicked)
        advanceUntilIdle()

        assertNull(lastSubmittedRating)
        assertTrue(effects.any { it is WriteReviewEffect.ShowSnackbar && it.stringResId == R.string.error_review_event_status_invalid })

        effectJob.cancel()
    }

    @Test
    fun ratingChanged_updatesRatingAndClearsError() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(WriteReviewEvent.RatingChanged(4))

        assertEquals(4, viewModel.uiState.value.rating)
        assertNull(viewModel.uiState.value.ratingErrorRes)
    }

    @Test
    fun commentChanged_updatesCommentAndEnforces1000CharLimit() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val longComment = "a".repeat(1100)
        viewModel.onEvent(WriteReviewEvent.CommentChanged(longComment))

        assertEquals(1000, viewModel.uiState.value.comment.length)
        assertEquals(1000, viewModel.uiState.value.commentLength)
    }

    @Test
    fun submitClicked_withZeroRating_showsRatingError() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(WriteReviewEvent.CommentChanged("Чудова організація"))
        viewModel.onEvent(WriteReviewEvent.SubmitClicked)
        advanceUntilIdle()

        assertEquals(R.string.error_review_rating_invalid, viewModel.uiState.value.ratingErrorRes)
        assertNull(lastSubmittedRating)
    }

    @Test
    fun submitClicked_withShortComment_showsCommentError() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(WriteReviewEvent.RatingChanged(5))
        viewModel.onEvent(WriteReviewEvent.CommentChanged("A"))
        viewModel.onEvent(WriteReviewEvent.SubmitClicked)
        advanceUntilIdle()

        assertEquals(R.string.error_review_comment_required, viewModel.uiState.value.commentErrorRes)
        assertNull(lastSubmittedRating)
    }

    @Test
    fun submitClicked_forOrganizer_callsSubmitReviewWithOrganizerRoleAndNavigatesBack() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val effects = mutableListOf<WriteReviewEffect>()
        val effectJob = backgroundScope.launch(testDispatcher) {
            viewModel.effect.collect { effects.add(it) }
        }

        viewModel.onEvent(WriteReviewEvent.RatingChanged(5))
        viewModel.onEvent(WriteReviewEvent.CommentChanged("Неймовірна подія, дякую організаторам!"))
        assertTrue(viewModel.uiState.value.isSubmitEnabled)

        viewModel.onEvent(WriteReviewEvent.SubmitClicked)
        advanceUntilIdle()

        assertEquals(5, lastSubmittedRating)
        assertEquals("Неймовірна подія, дякую організаторам!", lastSubmittedComment)
        assertEquals(UserRole.ORGANIZER, lastSubmittedTargetRole)
        assertTrue(effects.any { it is WriteReviewEffect.ShowSnackbar && it.stringResId == R.string.review_success_with_new_rating })
        assertTrue(effects.any { it is WriteReviewEffect.NavigateBack })

        effectJob.cancel()
    }

    @Test
    fun submitClicked_forVolunteer_callsSubmitReviewWithVolunteerRoleAndNavigatesBack() = runTest {
        formInfoResult = Result.success(
            ReviewTargetInfo(
                eventId = "event-1",
                eventTitle = "Прибирання парку",
                revieweeId = "user-vol",
                revieweeName = "Волонтер Андрій",
                revieweeRole = UserRole.VOLUNTEER,
                alreadyReviewed = false,
                canReview = true
            )
        )

        val viewModel = createViewModel(targetUserId = "user-vol", targetUserName = "Волонтер Андрій")
        advanceUntilIdle()

        val effects = mutableListOf<WriteReviewEffect>()
        val effectJob = backgroundScope.launch(testDispatcher) {
            viewModel.effect.collect { effects.add(it) }
        }

        viewModel.onEvent(WriteReviewEvent.RatingChanged(5))
        viewModel.onEvent(WriteReviewEvent.CommentChanged("Відмінний волонтер, пунктуальний та відповідальний"))
        viewModel.onEvent(WriteReviewEvent.SubmitClicked)
        advanceUntilIdle()

        assertEquals(5, lastSubmittedRating)
        assertEquals(UserRole.VOLUNTEER, lastSubmittedTargetRole)
        assertTrue(effects.any { it is WriteReviewEffect.NavigateBack })

        effectJob.cancel()
    }

    @Test
    fun submitClicked_onFailure_updatesStateAndEmitsMessage() = runTest {
        submitReviewResult = Result.failure(RuntimeException("Network failure"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        val effects = mutableListOf<WriteReviewEffect>()
        val effectJob = backgroundScope.launch(testDispatcher) {
            viewModel.effect.collect { effects.add(it) }
        }

        viewModel.onEvent(WriteReviewEvent.RatingChanged(4))
        viewModel.onEvent(WriteReviewEvent.CommentChanged("Гарна робота команди"))
        viewModel.onEvent(WriteReviewEvent.SubmitClicked)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSubmitting)
        assertEquals("Network failure", viewModel.uiState.value.generalError)
        assertTrue(effects.any { it is WriteReviewEffect.ShowMessage && it.message == "Network failure" })

        effectJob.cancel()
    }

    @Test
    fun cancelClicked_emitsNavigateBack() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val effects = mutableListOf<WriteReviewEffect>()
        val effectJob = backgroundScope.launch(testDispatcher) {
            viewModel.effect.collect { effects.add(it) }
        }

        viewModel.onEvent(WriteReviewEvent.CancelClicked)
        advanceUntilIdle()

        assertTrue(effects.any { it is WriteReviewEffect.NavigateBack })

        effectJob.cancel()
    }
}
