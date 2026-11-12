package com.arathort.localinteraction

import com.arathort.localinteraction.domain.model.Complaint
import com.arathort.localinteraction.domain.model.ComplaintTargetType
import com.arathort.localinteraction.domain.repository.ComplaintRepository
import com.arathort.localinteraction.domain.usecase.SubmitComplaintUseCase
import com.arathort.localinteraction.presentation.report.ReportEffect
import com.arathort.localinteraction.presentation.report.ReportEvent
import com.arathort.localinteraction.presentation.report.ReportViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class ReportViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private var submitResult: Result<Complaint> = Result.success(
        Complaint(
            id = "complaint-1",
            reporterId = "user-1",
            reporterName = "Волонтер",
            violatorId = "target-1",
            violatorName = "Порушник",
            reason = "Шахрайство",
            targetType = "Event",
            targetId = "event-1",
            violationCategory = "Шахрайство",
            createdAt = "2026-11-11T10:00:00Z"
        )
    )

    private var lastTargetType: ComplaintTargetType? = null
    private var lastTargetId: String? = null
    private var lastViolationCategory: String? = null
    private var lastDescription: String? = null

    private val fakeComplaintRepository = object : ComplaintRepository {
        override suspend fun createComplaint(
            targetType: ComplaintTargetType,
            targetId: String,
            violationCategory: String,
            description: String
        ): Result<Complaint> {
            lastTargetType = targetType
            lastTargetId = targetId
            lastViolationCategory = violationCategory
            lastDescription = description
            return submitResult
        }
    }

    private fun createViewModel(
        targetType: String = "EVENT",
        targetId: String = "event-1",
        targetTitle: String = "Прибирання парку"
    ): ReportViewModel = ReportViewModel(
        targetTypeString = targetType,
        targetId = targetId,
        targetTitle = targetTitle,
        submitComplaintUseCase = SubmitComplaintUseCase(fakeComplaintRepository)
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        lastTargetType = null
        lastTargetId = null
        lastViolationCategory = null
        lastDescription = null
        submitResult = Result.success(
            Complaint(
                id = "complaint-1",
                reporterId = "user-1",
                reporterName = "Волонтер",
                violatorId = "target-1",
                violatorName = "Порушник",
                reason = "Шахрайство",
                targetType = "Event",
                targetId = "event-1",
                violationCategory = "Шахрайство",
                createdAt = "2026-11-11T10:00:00Z"
            )
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state reflects parameters`() {
        val viewModel = createViewModel(targetType = "USER", targetId = "user-2", targetTitle = "Іван")
        val state = viewModel.uiState.value

        assertEquals(ComplaintTargetType.USER, state.targetType)
        assertEquals("user-2", state.targetId)
        assertEquals("Іван", state.targetTitle)
        assertEquals("", state.selectedCategory)
        assertEquals("", state.description)
        assertFalse(state.isSubmitting)
        assertNull(state.categoryErrorResId)
        assertNull(state.descriptionErrorResId)
    }

    @Test
    fun `CategorySelected updates selectedCategory and clears error`() {
        val viewModel = createViewModel()
        viewModel.onEvent(ReportEvent.SubmitClicked)
        assertNotNull(viewModel.uiState.value.categoryErrorResId)

        viewModel.onEvent(ReportEvent.CategorySelected("Спам або реклама"))
        assertEquals("Спам або реклама", viewModel.uiState.value.selectedCategory)
        assertNull(viewModel.uiState.value.categoryErrorResId)
    }

    @Test
    fun `DescriptionChanged updates description and clears error`() {
        val viewModel = createViewModel()
        viewModel.onEvent(ReportEvent.SubmitClicked)
        assertNotNull(viewModel.uiState.value.descriptionErrorResId)

        viewModel.onEvent(ReportEvent.DescriptionChanged("Детальний опис скарги на подію"))
        assertEquals("Детальний опис скарги на подію", viewModel.uiState.value.description)
        assertNull(viewModel.uiState.value.descriptionErrorResId)
    }

    @Test
    fun `BackClicked sends NavigateBack effect`() = runTest {
        val viewModel = createViewModel()
        val effects = mutableListOf<ReportEffect>()
        val effectJob = backgroundScope.launch(testDispatcher) {
            viewModel.effect.collect { effects.add(it) }
        }

        viewModel.onEvent(ReportEvent.BackClicked)
        advanceUntilIdle()

        assertTrue(effects.contains(ReportEffect.NavigateBack))
        effectJob.cancel()
    }

    @Test
    fun `SubmitClicked with invalid inputs sets validation errors`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(ReportEvent.SubmitClicked)

        val state = viewModel.uiState.value
        assertEquals(R.string.error_complaint_category_required, state.categoryErrorResId)
        assertEquals(R.string.error_complaint_description_too_short, state.descriptionErrorResId)
        assertFalse(state.isSubmitting)
        assertNull(lastTargetId)
    }

    @Test
    fun `SubmitClicked with valid inputs submits complaint and sends NavigateBack`() = runTest {
        val viewModel = createViewModel(targetType = "COMMENT", targetId = "comment-5", targetTitle = "Коментар")
        val effects = mutableListOf<ReportEffect>()
        val effectJob = backgroundScope.launch(testDispatcher) {
            viewModel.effect.collect { effects.add(it) }
        }

        viewModel.onEvent(ReportEvent.CategorySelected("Образлива поведінка"))
        viewModel.onEvent(ReportEvent.DescriptionChanged("Цей коментар містить неприпустимі образи"))
        viewModel.onEvent(ReportEvent.SubmitClicked)
        advanceUntilIdle()

        assertEquals(ComplaintTargetType.COMMENT, lastTargetType)
        assertEquals("comment-5", lastTargetId)
        assertEquals("Образлива поведінка", lastViolationCategory)
        assertEquals("Цей коментар містить неприпустимі образи", lastDescription)

        assertFalse(viewModel.uiState.value.isSubmitting)
        assertTrue(effects.contains(ReportEffect.ShowSnackbar(R.string.complaint_submitted_success)))
        assertTrue(effects.contains(ReportEffect.NavigateBack))
        effectJob.cancel()
    }

    @Test
    fun `SubmitClicked on failure shows error snackbar`() = runTest {
        submitResult = Result.failure(Exception("Сервер скарг недоступний"))
        val viewModel = createViewModel()
        val effects = mutableListOf<ReportEffect>()
        val effectJob = backgroundScope.launch(testDispatcher) {
            viewModel.effect.collect { effects.add(it) }
        }

        viewModel.onEvent(ReportEvent.CategorySelected("Шахрайство"))
        viewModel.onEvent(ReportEvent.DescriptionChanged("Опис порушення понад 10 символів"))
        viewModel.onEvent(ReportEvent.SubmitClicked)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSubmitting)
        assertTrue(effects.contains(ReportEffect.ShowSnackbarText("Сервер скарг недоступний")))
        assertFalse(effects.contains(ReportEffect.NavigateBack))
        effectJob.cancel()
    }
}
