package com.arathort.localinteraction.data.datasource.mock

import com.arathort.localinteraction.data.datasource.LocalInteractionDataSource
import com.arathort.localinteraction.data.datasource.remote.JwtTokenManager
import com.arathort.localinteraction.data.dto.ApplicationDto
import com.arathort.localinteraction.data.dto.CategoryDto
import com.arathort.localinteraction.data.dto.CommentDto
import com.arathort.localinteraction.data.dto.ComplaintDto
import com.arathort.localinteraction.data.dto.EventDto
import com.arathort.localinteraction.data.dto.GeoLocationDto
import com.arathort.localinteraction.data.dto.MyTaskHistoryItemDto
import com.arathort.localinteraction.data.dto.OrganizerContactsInfoDto
import com.arathort.localinteraction.data.dto.OrganizerShortInfoDto
import com.arathort.localinteraction.data.dto.ReviewDto
import com.arathort.localinteraction.data.dto.UserDto
import com.arathort.localinteraction.domain.model.ApplicationStatus
import com.arathort.localinteraction.domain.model.EventStatus
import com.arathort.localinteraction.domain.model.GeoPoint
import com.arathort.localinteraction.domain.model.ModerationStatus
import com.arathort.localinteraction.domain.model.UserRole
import com.arathort.localinteraction.domain.model.UserStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockLocalInteractionDataSource @Inject constructor(
    private val jwtTokenManager: JwtTokenManager
) : LocalInteractionDataSource {

    var simulatedDelayMs: Long = 200L

    private val guestUser = UserDto(
        id = "user-guest",
        fullName = "Гість платформи",
        email = "",
        role = UserRole.GUEST.apiValue,
        status = UserStatus.ACTIVE.apiValue
    )

    private val initialReviews = listOf(
        ReviewDto(
            id = "review-1",
            eventId = "event-1",
            eventTitle = "Осіння висадка дерев на Подолі",
            authorId = "user-organizer-1",
            authorName = "Тарас Мельник",
            targetUserId = "user-volunteer-1",
            rating = 5,
            comment = "Надзвичайно відповідальна волонтерка! Дякуємо за активну участь.",
            createdAt = "2026-10-20T12:00:00Z"
        )
    )

    private val initialApplications = listOf(
        ApplicationDto(
            id = "app-1",
            eventId = "event-1",
            eventTitle = "Весняне прибирання парку «Наталка» та берегової лінії Дніпра",
            eventDate = "2026-10-31T10:00:00Z",
            eventStatus = EventStatus.OPEN.apiValue,
            volunteerId = "user-volunteer-1",
            volunteerName = "Олена Коваленко",
            volunteerRating = 4.9,
            volunteerPhone = "+380 67 123 45 67",
            motivationLetter = "Маю досвід організації еко-толок на Оболоні, візьму власні багаторазові рукавички.",
            status = ApplicationStatus.APPROVED.apiValue,
            canWithdraw = true,
            organizerContacts = OrganizerContactsInfoDto(
                fullName = "Тарас Мельник",
                email = "taras.melnyk@gmail.com",
                phone = "+380 50 987 65 43"
            ),
            createdAt = "2026-10-24T09:30:00Z",
            updatedAt = "2026-10-24T11:00:00Z"
        ),
        ApplicationDto(
            id = "app-2",
            eventId = "event-4",
            eventTitle = "Відновлення дитячого майданчика у дворі на Сихові",
            eventDate = "2026-10-29T10:00:00Z",
            eventStatus = EventStatus.IN_PROGRESS.apiValue,
            volunteerId = "user-volunteer-1",
            volunteerName = "Олена Коваленко",
            volunteerRating = 4.9,
            volunteerPhone = "+380 67 123 45 67",
            motivationLetter = "Допоможу з фарбуванням та висадкою рослин.",
            status = ApplicationStatus.APPROVED.apiValue,
            canWithdraw = true,
            organizerContacts = OrganizerContactsInfoDto(
                fullName = "Тарас Мельник",
                email = "taras.melnyk@gmail.com",
                phone = "+380 50 987 65 43"
            ),
            createdAt = "2026-10-23T14:15:00Z",
            updatedAt = "2026-10-23T16:00:00Z"
        )
    )

    private val initialUsers = listOf(
        UserDto(
            id = "user-volunteer-1",
            fullName = "Олена Коваленко",
            email = "olena.kovalenko@ukr.net",
            phone = "+380 67 123 45 67",
            description = "Активна волонтерка у сфері екології та допомоги притулкам для тварин у Києві.",
            role = UserRole.VOLUNTEER.apiValue,
            rating = 4.9,
            reviews = initialReviews,
            status = UserStatus.ACTIVE.apiValue,
            myTasks = initialApplications.map { app ->
                MyTaskHistoryItemDto(
                    applicationId = app.id,
                    eventId = app.eventId,
                    eventTitle = app.eventTitle,
                    eventStatus = app.eventStatus,
                    applicationStatus = app.status,
                    canWithdraw = app.canWithdraw,
                    appliedAt = app.createdAt
                )
            }
        ),
        UserDto(
            id = "user-organizer-1",
            fullName = "Тарас Мельник",
            email = "taras.melnyk@gmail.com",
            phone = "+380 50 987 65 43",
            description = "Координатор громадської ініціативи «Чисте місто» та волонтерського штабу.",
            role = UserRole.ORGANIZER.apiValue,
            rating = 4.8,
            status = UserStatus.ACTIVE.apiValue
        ),
        UserDto(
            id = "user-admin-1",
            fullName = "Ірина Бондаренко",
            email = "admin@localinteraction.ua",
            phone = "+380 44 200 10 20",
            description = "Модераторка платформи локальної взаємодії.",
            role = UserRole.ADMIN.apiValue,
            rating = 5.0,
            status = UserStatus.ACTIVE.apiValue
        )
    )

    private val initialCategories = listOf(
        CategoryDto(id = "cat-eco", name = "Екологія та довкілля", boundEventsCount = 2),
        CategoryDto(id = "cat-defense", name = "Допомога ЗСУ та ВПО", boundEventsCount = 2),
        CategoryDto(id = "cat-animals", name = "Допомога тваринам", boundEventsCount = 1),
        CategoryDto(id = "cat-urban", name = "Благоустрій громади", boundEventsCount = 1),
        CategoryDto(id = "cat-education", name = "Освіта та культура", boundEventsCount = 0)
    )

    private val defaultOrganizer = OrganizerShortInfoDto(
        id = "user-organizer-1",
        fullName = "Тарас Мельник",
        rating = 4.8
    )

    private val initialEvents = listOf(
        EventDto(
            id = "event-1",
            title = "Весняне прибирання парку «Наталка» та берегової лінії Дніпра",
            description = "Запрошуємо небайдужих мешканців Оболоні та всього Києва долучитися до великої толоки в парку «Наталка». Інвентар, рукавички, мішки для сортування пластику та скла надає організатор.",
            categoryId = "cat-eco",
            categoryName = "Екологія та довкілля",
            status = EventStatus.OPEN.apiValue,
            moderationStatus = ModerationStatus.APPROVED.apiValue,
            eventDate = "2026-10-31T10:00:00Z",
            location = GeoLocationDto(
                address = "м. Київ, Оболонська набережна, 9 (парк «Наталка»)",
                latitude = 50.5010,
                longitude = 30.5194
            ),
            volunteerQuota = 25,
            approvedVolunteersCount = 8,
            progressText = "Підтверджено: 8 з 25",
            organizer = defaultOrganizer,
            createdAt = "2026-10-20T08:00:00Z"
        ),
        EventDto(
            id = "event-2",
            title = "Плетіння маскувальних сіток та формування гуманітарних наборів",
            description = "Волонтерський хаб на Подолі запрошує помічників для плетіння сезонних маскувальних сіток та фасування теплих речей і медикаментів для захисників та вимушених переселенців.",
            categoryId = "cat-defense",
            categoryName = "Допомога ЗСУ та ВПО",
            status = EventStatus.OPEN.apiValue,
            moderationStatus = ModerationStatus.APPROVED.apiValue,
            eventDate = "2026-11-01T10:00:00Z",
            location = GeoLocationDto(
                address = "м. Київ, вул. Спаська, 15",
                latitude = 50.4666,
                longitude = 30.5177
            ),
            volunteerQuota = 15,
            approvedVolunteersCount = 11,
            progressText = "Підтверджено: 11 з 15",
            organizer = defaultOrganizer,
            createdAt = "2026-10-21T09:00:00Z"
        ),
        EventDto(
            id = "event-3",
            title = "Вигул та соціалізація собак у притулку «Сіріус»",
            description = "Потрібні волонтери для вигулу собак, прибирання вольєрів та соціалізації цуценят. Організовано централізований трансфер від станції метро «Героїв Дніпра» о 09:30.",
            categoryId = "cat-animals",
            categoryName = "Допомога тваринам",
            status = EventStatus.OPEN.apiValue,
            moderationStatus = ModerationStatus.APPROVED.apiValue,
            eventDate = "2026-11-02T10:00:00Z",
            location = GeoLocationDto(
                address = "Київська обл., с. Федорівка, притулок «Сіріус»",
                latitude = 50.8581,
                longitude = 30.2286
            ),
            volunteerQuota = 20,
            approvedVolunteersCount = 5,
            progressText = "Підтверджено: 5 з 20",
            organizer = defaultOrganizer,
            createdAt = "2026-10-21T12:00:00Z"
        ),
        EventDto(
            id = "event-4",
            title = "Відновлення дитячого майданчика у дворі на Сихові",
            description = "Фарбування дерев'яних елементів, ремонт лавок та висадка кущів лаванди й гортензії біля дитячого майданчика за підтримки місцевого ОСББ.",
            categoryId = "cat-urban",
            categoryName = "Благоустрій громади",
            status = EventStatus.IN_PROGRESS.apiValue,
            moderationStatus = ModerationStatus.APPROVED.apiValue,
            eventDate = "2026-10-29T10:00:00Z",
            location = GeoLocationDto(
                address = "м. Львів, просп. Червоної Калини, 62",
                latitude = 49.7931,
                longitude = 24.0561
            ),
            volunteerQuota = 10,
            approvedVolunteersCount = 10,
            progressText = "Підтверджено: 10 з 10",
            organizer = defaultOrganizer,
            createdAt = "2026-10-18T10:00:00Z"
        ),
        EventDto(
            id = "event-5",
            title = "Воркшоп із тактичної медицини та домедичної допомоги",
            description = "Навчальний інтенсив із зупинки критичних кровотеч та базової серцево-легеневої реанімації для мешканців району. Потрібні волонтери-координатори для реєстрації учасників.",
            categoryId = "cat-defense",
            categoryName = "Допомога ЗСУ та ВПО",
            status = EventStatus.PENDING_MODERATION.apiValue,
            moderationStatus = ModerationStatus.PENDING.apiValue,
            eventDate = "2026-11-05T10:00:00Z",
            location = GeoLocationDto(
                address = "м. Київ, вул. Хрещатик, 22",
                latitude = 50.4488,
                longitude = 30.5227
            ),
            volunteerQuota = 8,
            approvedVolunteersCount = 0,
            progressText = "Підтверджено: 0 з 8",
            organizer = defaultOrganizer,
            createdAt = "2026-10-25T15:00:00Z"
        )
    )

    private val initialComments = listOf(
        CommentDto(
            id = "comment-1",
            eventId = "event-1",
            authorId = "user-volunteer-1",
            authorName = "Олена Коваленко",
            text = "Підкажіть, будь ласка, біля якого входу в парк «Наталка» збираємося?",
            createdAtMillis = 1792810000000L
        ),
        CommentDto(
            id = "comment-2",
            eventId = "event-1",
            authorId = "user-organizer-1",
            authorName = "Тарас Мельник",
            text = "Зустрічаємося біля центральної ротонди о 10:00 ранку!",
            createdAtMillis = 1792813600000L
        )
    )

    private val initialComplaints = listOf(
        ComplaintDto(
            id = "complaint-1",
            reporterId = "user-volunteer-1",
            reporterName = "Олена Коваленко",
            violatorId = "user-spam-1",
            violatorName = "Невідомий Користувач",
            reason = "Розміщення рекламного спаму в коментарях до благодійного заходу.",
            createdAtMillis = 1792820000000L,
            isResolved = false
        )
    )

    private val usersState = MutableStateFlow(initialUsers)
    private val currentUserState = MutableStateFlow(initialUsers.first())
    private val categoriesState = MutableStateFlow(initialCategories)
    private val eventsState = MutableStateFlow(initialEvents)
    private val applicationsState = MutableStateFlow(initialApplications)
    private val commentsState = MutableStateFlow(initialComments)
    private val reviewsState = MutableStateFlow(initialReviews)
    private val complaintsState = MutableStateFlow(initialComplaints)

    override val currentUser: Flow<UserDto> = currentUserState.asStateFlow()
    override fun observeEvents(): Flow<List<EventDto>> = eventsState.asStateFlow()
    override fun observeCategories(): Flow<List<CategoryDto>> = categoriesState.asStateFlow()
    override fun observeApplications(): Flow<List<ApplicationDto>> = applicationsState.asStateFlow()
    override fun observeComments(): Flow<List<CommentDto>> = commentsState.asStateFlow()
    override fun observeReviews(): Flow<List<ReviewDto>> = reviewsState.asStateFlow()
    override fun observeComplaints(): Flow<List<ComplaintDto>> = complaintsState.asStateFlow()

    override suspend fun login(email: String, password: String): Result<UserDto> {
        delay(simulatedDelayMs)
        val trimmedEmail = email.trim()
        if (!trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
            return Result.failure(IllegalArgumentException("Введіть коректну адресу електронної пошти"))
        }
        if (password.length < 8) {
            return Result.failure(IllegalArgumentException("Пароль має містити щонайменше 8 символів"))
        }
        val existingUser = usersState.value.find { it.email.equals(trimmedEmail, ignoreCase = true) }
            ?: UserDto(
                id = "user-${UUID.randomUUID()}",
                fullName = trimmedEmail.substringBefore("@"),
                email = trimmedEmail,
                role = UserRole.VOLUNTEER.apiValue,
                rating = 5.0,
                status = UserStatus.ACTIVE.apiValue
            ).also { newUser ->
                usersState.update { it + newUser }
            }

        if (existingUser.status == UserStatus.BLOCKED.apiValue) {
            return Result.failure(IllegalStateException("Ваш обліковий запис заблоковано"))
        }

        jwtTokenManager.updateToken("mock-jwt-token-${existingUser.id}")
        currentUserState.value = existingUser
        return Result.success(existingUser)
    }

    override suspend fun register(
        fullName: String,
        email: String,
        password: String,
        role: UserRole,
        acceptedPrivacyPolicy: Boolean
    ): Result<UserDto> {
        delay(simulatedDelayMs)
        val trimmedEmail = email.trim()
        if (!trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
            return Result.failure(IllegalArgumentException("Введіть коректну адресу електронної пошти"))
        }
        if (password.length < 8) {
            return Result.failure(IllegalArgumentException("Пароль має містити щонайменше 8 символів"))
        }
        if (role != UserRole.VOLUNTEER && role != UserRole.ORGANIZER) {
            return Result.failure(IllegalArgumentException("Оберіть роль: Волонтер або Організатор"))
        }
        if (!acceptedPrivacyPolicy) {
            return Result.failure(IllegalArgumentException("Необхідно погодитися з політикою конфіденційності"))
        }
        if (usersState.value.any { it.email.equals(trimmedEmail, ignoreCase = true) }) {
            return Result.failure(IllegalArgumentException("Користувач із цією електронною поштою вже зареєстрований"))
        }

        val newUser = UserDto(
            id = "user-${UUID.randomUUID()}",
            fullName = fullName.trim().ifBlank { trimmedEmail.substringBefore("@") },
            email = trimmedEmail,
            role = role.apiValue,
            rating = 0.0,
            status = UserStatus.ACTIVE.apiValue
        )
        usersState.update { it + newUser }
        jwtTokenManager.updateToken("mock-jwt-token-${newUser.id}")
        currentUserState.value = newUser
        return Result.success(newUser)
    }

    override suspend fun logout() {
        delay(simulatedDelayMs)
        jwtTokenManager.clearToken()
        currentUserState.value = guestUser
    }

    override suspend fun switchDemoRole(role: UserRole): Result<UserDto> {
        delay(simulatedDelayMs / 2)
        val targetUser = when (role) {
            UserRole.GUEST -> guestUser.also { jwtTokenManager.clearToken() }
            UserRole.VOLUNTEER -> usersState.value.first { UserRole.fromApiValue(it.role) == UserRole.VOLUNTEER }
            UserRole.ORGANIZER -> usersState.value.first { UserRole.fromApiValue(it.role) == UserRole.ORGANIZER }
            UserRole.ADMIN -> usersState.value.first { UserRole.fromApiValue(it.role) == UserRole.ADMIN }
        }
        if (role != UserRole.GUEST) {
            jwtTokenManager.updateToken("mock-jwt-token-${targetUser.id}")
        }
        currentUserState.value = targetUser
        return Result.success(targetUser)
    }

    override suspend fun getEventById(eventId: String): Result<EventDto> {
        delay(simulatedDelayMs / 2)
        val event = eventsState.value.find { it.id == eventId }
            ?: return Result.failure(NoSuchElementException("Захід не знайдено"))
        return Result.success(event)
    }

    override suspend fun addComment(eventId: String, text: String): Result<CommentDto> {
        delay(simulatedDelayMs)
        val user = currentUserState.value
        if (UserRole.fromApiValue(user.role) == UserRole.GUEST) {
            return Result.failure(IllegalStateException("Увійдіть або зареєструйтеся для виконання цієї дії"))
        }
        if (text.isBlank()) {
            return Result.failure(IllegalArgumentException("Коментар не може бути порожнім"))
        }
        val newComment = CommentDto(
            id = "comment-${UUID.randomUUID()}",
            eventId = eventId,
            authorId = user.id,
            authorName = user.fullName ?: user.email,
            authorAvatarUrl = user.avatarUrl,
            text = text.trim(),
            createdAtMillis = System.currentTimeMillis()
        )
        commentsState.update { it + newComment }
        return Result.success(newComment)
    }

    override suspend fun createEvent(
        title: String,
        description: String,
        categoryId: String,
        dateTimeMillis: Long,
        address: String,
        location: GeoPoint,
        volunteerQuota: Int,
        photoUrls: List<String>
    ): Result<EventDto> {
        delay(simulatedDelayMs)
        val organizer = currentUserState.value
        val organizerRole = UserRole.fromApiValue(organizer.role)
        if (organizerRole != UserRole.ORGANIZER && organizerRole != UserRole.ADMIN) {
            return Result.failure(IllegalStateException("Тільки організатор може створювати заходи"))
        }
        if (title.isBlank()) {
            return Result.failure(IllegalArgumentException("Вкажіть назву заходу"))
        }
        if (description.trim().length < 50) {
            return Result.failure(IllegalArgumentException("Опис заходу має містити щонайменше 50 символів"))
        }
        if (address.isBlank()) {
            return Result.failure(IllegalArgumentException("Вкажіть фізичну адресу та оберіть координати на карті"))
        }
        if (volunteerQuota <= 0) {
            return Result.failure(IllegalArgumentException("Квота волонтерів має бути цілим числом більше 0"))
        }
        val category = categoriesState.value.find { it.id == categoryId }
            ?: return Result.failure(IllegalArgumentException("Оберіть категорію зі списку"))

        val created = EventDto(
            id = "event-${UUID.randomUUID()}",
            title = title.trim(),
            description = description.trim(),
            categoryId = category.id,
            categoryName = category.name,
            status = EventStatus.PENDING_MODERATION.apiValue,
            moderationStatus = ModerationStatus.PENDING.apiValue,
            eventDate = Instant.ofEpochMilli(dateTimeMillis).toString(),
            location = GeoLocationDto(
                address = address.trim(),
                latitude = location.latitude,
                longitude = location.longitude
            ),
            volunteerQuota = volunteerQuota,
            approvedVolunteersCount = 0,
            progressText = "Підтверджено: 0 з $volunteerQuota",
            photos = photoUrls,
            organizer = OrganizerShortInfoDto(
                id = organizer.id,
                fullName = organizer.fullName,
                avatarUrl = organizer.avatarUrl,
                rating = organizer.rating
            ),
            createdAt = Instant.now().toString()
        )
        eventsState.update { listOf(created) + it }
        categoriesState.update { categories ->
            categories.map {
                if (it.id == category.id) it.copy(boundEventsCount = it.boundEventsCount + 1) else it
            }
        }
        return Result.success(created)
    }

    override suspend fun updateEventStatus(eventId: String, newStatus: EventStatus): Result<EventDto> {
        delay(simulatedDelayMs)
        val currentEvent = eventsState.value.find { it.id == eventId }
            ?: return Result.failure(NoSuchElementException("Захід не знайдено"))
        val updated = currentEvent.copy(
            status = newStatus.apiValue,
            moderationStatus = if (newStatus == EventStatus.OPEN) {
                ModerationStatus.APPROVED.apiValue
            } else {
                currentEvent.moderationStatus
            }
        )
        eventsState.update { events -> events.map { if (it.id == eventId) updated else it } }

        val canWithdrawNow = newStatus != EventStatus.COMPLETED && newStatus != EventStatus.CANCELLED
        if (newStatus == EventStatus.CANCELLED) {
            applicationsState.update { apps ->
                apps.map { app ->
                    val appStatus = ApplicationStatus.fromApiValue(app.status)
                    if (app.eventId == eventId &&
                        (appStatus == ApplicationStatus.PENDING || appStatus == ApplicationStatus.APPROVED)
                    ) {
                        app.copy(
                            status = ApplicationStatus.REJECTED.apiValue,
                            eventStatus = newStatus.apiValue,
                            canWithdraw = false,
                            rejectionReason = "Захід скасовано організатором"
                        )
                    } else if (app.eventId == eventId) {
                        app.copy(eventStatus = newStatus.apiValue, canWithdraw = false)
                    } else {
                        app
                    }
                }
            }
        } else {
            applicationsState.update { apps ->
                apps.map { app ->
                    if (app.eventId == eventId) {
                        app.copy(eventStatus = newStatus.apiValue, canWithdraw = canWithdrawNow)
                    } else {
                        app
                    }
                }
            }
        }
        return Result.success(updated)
    }

    override suspend fun submitApplication(
        eventId: String,
        motivationLetter: String
    ): Result<ApplicationDto> {
        delay(simulatedDelayMs)
        val volunteer = currentUserState.value
        if (UserRole.fromApiValue(volunteer.role) == UserRole.GUEST) {
            return Result.failure(IllegalStateException("Увійдіть або зареєструйтеся для виконання цієї дії"))
        }
        val event = eventsState.value.find { it.id == eventId }
            ?: return Result.failure(NoSuchElementException("Захід не знайдено"))
        if (EventStatus.fromApiValue(event.status) != EventStatus.OPEN) {
            return Result.failure(IllegalStateException("Подача заявок доступна лише для відкритих заходів"))
        }
        val nowIso = Instant.now().toString()
        val newApp = ApplicationDto(
            id = "app-${UUID.randomUUID()}",
            eventId = event.id,
            eventTitle = event.title,
            eventDate = event.eventDate,
            eventStatus = event.status,
            volunteerId = volunteer.id,
            volunteerName = volunteer.fullName.orEmpty(),
            volunteerAvatarUrl = volunteer.avatarUrl,
            volunteerRating = volunteer.rating,
            volunteerPhone = volunteer.phone,
            motivationLetter = motivationLetter.trim(),
            status = ApplicationStatus.PENDING.apiValue,
            canWithdraw = true,
            createdAt = nowIso,
            updatedAt = nowIso
        )
        applicationsState.update { listOf(newApp) + it }
        return Result.success(newApp)
    }

    override suspend fun withdrawApplication(applicationId: String): Result<ApplicationDto> {
        delay(simulatedDelayMs)
        val app = applicationsState.value.find { it.id == applicationId }
            ?: return Result.failure(NoSuchElementException("Заявку не знайдено"))
        val currentEventStatus = EventStatus.fromApiValue(app.eventStatus)
        if (currentEventStatus == EventStatus.COMPLETED || currentEventStatus == EventStatus.CANCELLED) {
            return Result.failure(IllegalStateException("Неможливо відкликати заявку після завершення або скасування заходу"))
        }
        val wasApproved = ApplicationStatus.fromApiValue(app.status) == ApplicationStatus.APPROVED
        val updated = app.copy(
            status = ApplicationStatus.WITHDRAWN.apiValue,
            organizerContacts = null,
            updatedAt = Instant.now().toString()
        )
        applicationsState.update { list -> list.map { if (it.id == applicationId) updated else it } }
        if (wasApproved) {
            eventsState.update { events ->
                events.map { event ->
                    if (event.id == app.eventId) {
                        val newCount = (event.approvedVolunteersCount - 1).coerceAtLeast(0)
                        val reopenedStatus = if (
                            EventStatus.fromApiValue(event.status) == EventStatus.IN_PROGRESS &&
                            newCount < event.volunteerQuota
                        ) {
                            EventStatus.OPEN.apiValue
                        } else {
                            event.status
                        }
                        event.copy(
                            approvedVolunteersCount = newCount,
                            status = reopenedStatus,
                            progressText = "Підтверджено: $newCount з ${event.volunteerQuota}"
                        )
                    } else {
                        event
                    }
                }
            }
        }
        return Result.success(updated)
    }

    override suspend fun approveApplication(applicationId: String): Result<ApplicationDto> {
        delay(simulatedDelayMs)
        val app = applicationsState.value.find { it.id == applicationId }
            ?: return Result.failure(NoSuchElementException("Заявку не знайдено"))
        val event = eventsState.value.find { it.id == app.eventId }
        val organizerUser = usersState.value.find { it.id == event?.organizer?.id }
        val updated = app.copy(
            status = ApplicationStatus.APPROVED.apiValue,
            rejectionReason = null,
            organizerContacts = organizerUser?.let {
                OrganizerContactsInfoDto(
                    fullName = it.fullName,
                    email = it.email,
                    phone = it.phone
                )
            },
            updatedAt = Instant.now().toString()
        )
        applicationsState.update { list -> list.map { if (it.id == applicationId) updated else it } }
        eventsState.update { events ->
            events.map { currentEvent ->
                if (currentEvent.id == app.eventId) {
                    val newCount = currentEvent.approvedVolunteersCount + 1
                    val newStatus = if (
                        newCount >= currentEvent.volunteerQuota &&
                        EventStatus.fromApiValue(currentEvent.status) == EventStatus.OPEN
                    ) {
                        EventStatus.IN_PROGRESS.apiValue
                    } else {
                        currentEvent.status
                    }
                    currentEvent.copy(
                        approvedVolunteersCount = newCount,
                        status = newStatus,
                        progressText = "Підтверджено: $newCount з ${currentEvent.volunteerQuota}"
                    )
                } else {
                    currentEvent
                }
            }
        }
        return Result.success(updated)
    }

    override suspend fun rejectApplication(applicationId: String, reason: String): Result<ApplicationDto> {
        delay(simulatedDelayMs)
        if (reason.isBlank()) {
            return Result.failure(IllegalArgumentException("Вкажіть причину відхилення"))
        }
        val app = applicationsState.value.find { it.id == applicationId }
            ?: return Result.failure(NoSuchElementException("Заявку не знайдено"))
        val updated = app.copy(
            status = ApplicationStatus.REJECTED.apiValue,
            rejectionReason = reason.trim(),
            organizerContacts = null,
            updatedAt = Instant.now().toString()
        )
        applicationsState.update { list -> list.map { if (it.id == applicationId) updated else it } }
        return Result.success(updated)
    }

    override suspend fun submitReview(
        eventId: String,
        targetUserId: String,
        rating: Int,
        comment: String
    ): Result<ReviewDto> {
        delay(simulatedDelayMs)
        val reviewer = currentUserState.value
        if (rating !in 1..5) {
            return Result.failure(IllegalArgumentException("Оберіть рейтинг від 1 до 5 зірочок"))
        }
        if (comment.isBlank()) {
            return Result.failure(IllegalArgumentException("Додайте текстовий коментар до відгуку"))
        }
        val event = eventsState.value.find { it.id == eventId }
            ?: return Result.failure(NoSuchElementException("Захід не знайдено"))
        val eventStatus = EventStatus.fromApiValue(event.status)
        if (eventStatus != EventStatus.IN_PROGRESS && eventStatus != EventStatus.COMPLETED) {
            return Result.failure(
                IllegalStateException("Оцінювання доступне лише для заходів у статусі «В процесі» або «Виконано»")
            )
        }
        val alreadyReviewed = reviewsState.value.any {
            it.eventId == eventId && it.authorId == reviewer.id && it.targetUserId == targetUserId
        }
        if (alreadyReviewed) {
            return Result.failure(
                IllegalStateException("Ви вже залишили відгук для цього учасника в межах даного заходу")
            )
        }

        val review = ReviewDto(
            id = "review-${UUID.randomUUID()}",
            eventId = event.id,
            eventTitle = event.title,
            authorId = reviewer.id,
            authorName = reviewer.fullName ?: reviewer.email,
            targetUserId = targetUserId,
            rating = rating,
            comment = comment.trim(),
            createdAt = Instant.now().toString()
        )
        reviewsState.update { listOf(review) + it }
        return Result.success(review)
    }

    override suspend fun getUserProfile(userId: String): Result<UserDto> {
        delay(simulatedDelayMs / 2)
        val user = usersState.value.find { it.id == userId }
            ?: if (userId == guestUser.id) guestUser else null
        return user?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException("Користувача не знайдено"))
    }

    override suspend fun updateProfile(fullName: String, phone: String, bio: String): Result<UserDto> {
        delay(simulatedDelayMs)
        val current = currentUserState.value
        val updated = current.copy(
            fullName = fullName.trim().ifBlank { current.fullName },
            phone = phone.trim(),
            description = bio.trim()
        )
        currentUserState.value = updated
        usersState.update { users -> users.map { if (it.id == updated.id) updated else it } }
        return Result.success(updated)
    }

    override suspend fun approveEventModeration(eventId: String): Result<EventDto> {
        delay(simulatedDelayMs)
        return updateEventStatus(eventId, EventStatus.OPEN)
    }

    override suspend fun rejectEventModeration(eventId: String, reason: String): Result<EventDto> {
        delay(simulatedDelayMs)
        if (reason.isBlank()) {
            return Result.failure(IllegalArgumentException("Вкажіть причину відхилення"))
        }
        val currentEvent = eventsState.value.find { it.id == eventId }
            ?: return Result.failure(NoSuchElementException("Захід не знайдено"))
        val updated = currentEvent.copy(
            status = EventStatus.CANCELLED.apiValue,
            moderationStatus = ModerationStatus.REJECTED.apiValue,
            rejectionReason = reason.trim()
        )
        eventsState.update { list -> list.map { if (it.id == eventId) updated else it } }
        return Result.success(updated)
    }

    override suspend fun createCategory(name: String): Result<CategoryDto> {
        delay(simulatedDelayMs)
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Вкажіть назву категорії"))
        }
        val newCategory = CategoryDto(
            id = "cat-${UUID.randomUUID()}",
            name = name.trim(),
            boundEventsCount = 0,
            isArchived = false
        )
        categoriesState.update { it + newCategory }
        return Result.success(newCategory)
    }

    override suspend fun updateCategory(categoryId: String, name: String): Result<CategoryDto> {
        delay(simulatedDelayMs)
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Вкажіть назву категорії"))
        }
        val existing = categoriesState.value.find { it.id == categoryId }
            ?: return Result.failure(NoSuchElementException("Категорію не знайдено"))
        val updated = existing.copy(name = name.trim())
        categoriesState.update { list -> list.map { if (it.id == categoryId) updated else it } }
        return Result.success(updated)
    }

    override suspend fun archiveCategory(categoryId: String): Result<CategoryDto> {
        delay(simulatedDelayMs)
        val existing = categoriesState.value.find { it.id == categoryId }
            ?: return Result.failure(NoSuchElementException("Категорію не знайдено"))
        val updated = existing.copy(isArchived = true)
        categoriesState.update { list -> list.map { if (it.id == categoryId) updated else it } }
        return Result.success(updated)
    }

    override suspend fun blockUser(userId: String, reason: String): Result<UserDto> {
        delay(simulatedDelayMs)
        if (reason.isBlank()) {
            return Result.failure(IllegalArgumentException("Вкажіть причину блокування"))
        }
        val target = usersState.value.find { it.id == userId }
            ?: UserDto(
                id = userId,
                fullName = "Порушник",
                email = "violator@example.com",
                role = UserRole.VOLUNTEER.apiValue
            )
        val blocked = target.copy(
            status = UserStatus.BLOCKED.apiValue,
            blockReason = reason.trim()
        )
        usersState.update { list ->
            if (list.any { it.id == userId }) {
                list.map { if (it.id == userId) blocked else it }
            } else {
                list + blocked
            }
        }
        eventsState.update { events ->
            events.map { event ->
                if (event.organizer.id == userId) {
                    event.copy(status = EventStatus.CANCELLED.apiValue)
                } else {
                    event
                }
            }
        }
        complaintsState.update { complaints ->
            complaints.map { if (it.violatorId == userId) it.copy(isResolved = true) else it }
        }
        return Result.success(blocked)
    }
}
