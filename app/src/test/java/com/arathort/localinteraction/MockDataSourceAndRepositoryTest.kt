package com.arathort.localinteraction

import com.arathort.localinteraction.data.datasource.mock.MockLocalInteractionDataSource
import com.arathort.localinteraction.data.datasource.remote.JwtTokenManager
import com.arathort.localinteraction.data.repository.ApplicationRepositoryImpl
import com.arathort.localinteraction.data.repository.AuthRepositoryImpl
import com.arathort.localinteraction.data.repository.EventRepositoryImpl
import com.arathort.localinteraction.data.repository.ReviewRepositoryImpl
import com.arathort.localinteraction.domain.model.ApplicationStatus
import com.arathort.localinteraction.domain.model.EventStatus
import com.arathort.localinteraction.domain.model.GeoPoint
import com.arathort.localinteraction.domain.model.ModerationStatus
import com.arathort.localinteraction.domain.model.UserRole
import com.arathort.localinteraction.domain.model.UserStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MockDataSourceAndRepositoryTest {

    private lateinit var jwtTokenManager: JwtTokenManager
    private lateinit var dataSource: MockLocalInteractionDataSource
    private lateinit var authRepository: AuthRepositoryImpl
    private lateinit var eventRepository: EventRepositoryImpl
    private lateinit var applicationRepository: ApplicationRepositoryImpl
    private lateinit var reviewRepository: ReviewRepositoryImpl

    @Before
    fun setUp() {
        jwtTokenManager = JwtTokenManager()
        dataSource = MockLocalInteractionDataSource(jwtTokenManager).apply {
            simulatedDelayMs = 0L
        }
        authRepository = AuthRepositoryImpl(dataSource)
        eventRepository = EventRepositoryImpl(dataSource)
        applicationRepository = ApplicationRepositoryImpl(dataSource)
        reviewRepository = ReviewRepositoryImpl(dataSource)
    }

    @Test
    fun `observeOpenEvents returns only OPEN events and filters by query and category`() = runTest {
        val allOpenEvents = eventRepository.observeOpenEvents().first()
        assertEquals(3, allOpenEvents.size)
        assertTrue(allOpenEvents.all { it.status == EventStatus.OPEN })
        assertTrue(allOpenEvents.all { it.moderationStatus == ModerationStatus.APPROVED })
        assertEquals("Підтверджено: 8 з 25", allOpenEvents.first { it.id == "event-1" }.progressText)
        assertEquals("Тарас Мельник", allOpenEvents.first { it.id == "event-1" }.organizer.fullName)

        val filteredByCategory = eventRepository.observeOpenEvents(
            categoryIds = setOf("cat-eco")
        ).first()
        assertEquals(1, filteredByCategory.size)
        assertEquals("event-1", filteredByCategory.first().id)

        val filteredByKeyword = eventRepository.observeOpenEvents(
            searchQuery = "Сіріус"
        ).first()
        assertEquals(1, filteredByKeyword.size)
        assertEquals("event-3", filteredByKeyword.first().id)
    }

    @Test
    fun `register validates email password role and privacy consent`() = runTest {
        val invalidEmail = authRepository.register(
            fullName = "Іван Франко",
            email = "invalid-email",
            password = "password123",
            role = UserRole.VOLUNTEER,
            acceptedPrivacyPolicy = true
        )
        assertTrue(invalidEmail.isFailure)

        val shortPassword = authRepository.register(
            fullName = "Іван Франко",
            email = "ivan@ukr.net",
            password = "short",
            role = UserRole.VOLUNTEER,
            acceptedPrivacyPolicy = true
        )
        assertTrue(shortPassword.isFailure)

        val noConsent = authRepository.register(
            fullName = "Іван Франко",
            email = "ivan@ukr.net",
            password = "password123",
            role = UserRole.VOLUNTEER,
            acceptedPrivacyPolicy = false
        )
        assertTrue(noConsent.isFailure)

        val validRegistration = authRepository.register(
            fullName = "Іван Франко",
            email = "ivan@ukr.net",
            password = "password123",
            role = UserRole.ORGANIZER,
            acceptedPrivacyPolicy = true
        )
        assertTrue(validRegistration.isSuccess)
        val user = validRegistration.getOrThrow()
        assertEquals(UserRole.ORGANIZER, user.role)
        assertEquals(UserStatus.ACTIVE, user.status)
        assertNotNull(jwtTokenManager.accessToken)
    }

    @Test
    fun `createEvent enforces 50 char description and positive quota and starts in PENDING_MODERATION`() = runTest {
        authRepository.switchDemoRole(UserRole.ORGANIZER)

        val shortDescriptionResult = eventRepository.createEvent(
            title = "Нова толока",
            description = "Занадто короткий опис менше 50 символів.",
            categoryId = "cat-eco",
            dateTimeMillis = 1794000000000L,
            address = "м. Київ, вул. Хрещатик, 1",
            location = GeoPoint(50.4501, 30.5234),
            volunteerQuota = 10
        )
        assertTrue(shortDescriptionResult.isFailure)

        val validEventResult = eventRepository.createEvent(
            title = "Висадка дерев у Голосіївському парку разом із громадою",
            description = "Запрошуємо всіх мешканців району на висадку молодих дубів та кленів у Голосіївському парку цієї суботи.",
            categoryId = "cat-eco",
            dateTimeMillis = 1794000000000L,
            address = "м. Київ, Голосіївський проспект, 87",
            location = GeoPoint(50.3860, 30.5030),
            volunteerQuota = 20
        )
        assertTrue(validEventResult.isSuccess)
        val createdEvent = validEventResult.getOrThrow()
        assertEquals(EventStatus.PENDING_MODERATION, createdEvent.status)
        assertEquals(ModerationStatus.PENDING, createdEvent.moderationStatus)
        assertEquals("м. Київ, Голосіївський проспект, 87", createdEvent.location.address)
    }

    @Test
    fun `application approval exposes organizer contacts and withdrawal clears contacts`() = runTest {
        val submitted = applicationRepository.submitApplication(
            eventId = "event-2",
            motivationLetter = "Готова допомагати з пакуванням"
        ).getOrThrow()
        assertEquals(ApplicationStatus.PENDING, submitted.status)
        assertNull(submitted.organizerContacts)

        val approved = applicationRepository.approveApplication(submitted.id).getOrThrow()
        assertEquals(ApplicationStatus.APPROVED, approved.status)
        assertNotNull(approved.organizerContacts)
        assertEquals("taras.melnyk@gmail.com", approved.organizerContacts?.email)

        val withdrawn = applicationRepository.withdrawApplication(submitted.id).getOrThrow()
        assertEquals(ApplicationStatus.WITHDRAWN, withdrawn.status)
        assertNull(withdrawn.organizerContacts)
    }

    @Test
    fun `submitReview allows only one review per pair on IN_PROGRESS or COMPLETED event`() = runTest {
        authRepository.switchDemoRole(UserRole.VOLUNTEER)

        val openEventReview = reviewRepository.submitReview(
            eventId = "event-1",
            targetUserId = "user-organizer-1",
            rating = 5,
            comment = "Чудова організація!"
        )
        assertFalse(openEventReview.isSuccess)

        val firstValidReview = reviewRepository.submitReview(
            eventId = "event-4",
            targetUserId = "user-organizer-1",
            rating = 5,
            comment = "Чудова координація робіт на майданчику!"
        )
        assertTrue(firstValidReview.isSuccess)
        assertEquals("Олена Коваленко", firstValidReview.getOrThrow().authorName)

        val duplicateReview = reviewRepository.submitReview(
            eventId = "event-4",
            targetUserId = "user-organizer-1",
            rating = 4,
            comment = "Другий відгук"
        )
        assertTrue(duplicateReview.isFailure)
    }
}
