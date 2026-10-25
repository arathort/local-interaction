package com.arathort.localinteraction.data.datasource.mock

import com.arathort.localinteraction.data.datasource.LocalInteractionDataSource
import com.arathort.localinteraction.data.datasource.remote.JwtTokenManager
import com.arathort.localinteraction.data.dto.ApplicationDto
import com.arathort.localinteraction.data.dto.CategoryDto
import com.arathort.localinteraction.data.dto.CommentDto
import com.arathort.localinteraction.data.dto.ComplaintDto
import com.arathort.localinteraction.data.dto.EventDto
import com.arathort.localinteraction.data.dto.ReviewDto
import com.arathort.localinteraction.data.dto.UserDto
import com.arathort.localinteraction.domain.model.ApplicationStatus
import com.arathort.localinteraction.domain.model.EventStatus
import com.arathort.localinteraction.domain.model.GeoPoint
import com.arathort.localinteraction.domain.model.UserRole
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
        role = UserRole.GUEST.name
    )

    private val initialUsers = listOf(
        UserDto(
            id = "user-volunteer-1",
            fullName = "Олена Коваленко",
            email = "olena.kovalenko@ukr.net",
            phone = "+380 67 123 45 67",
            bio = "Активна волонтерка у сфері екології та допомоги притулкам для тварин у Києві.",
            role = UserRole.VOLUNTEER.name,
            averageRating = 4.9,
            reviewsCount = 12
        ),
        UserDto(
            id = "user-organizer-1",
            fullName = "Тарас Мельник",
            email = "taras.melnyk@gmail.com",
            phone = "+380 50 987 65 43",
            bio = "Координатор громадської ініціативи «Чисте місто» та волонтерського штабу.",
            role = UserRole.ORGANIZER.name,
            averageRating = 4.8,
            reviewsCount = 24
        ),
        UserDto(
            id = "user-admin-1",
            fullName = "Ірина Бондаренко",
            email = "admin@localinteraction.ua",
            phone = "+380 44 200 10 20",
            bio = "Модераторка платформи локальної взаємодії.",
            role = UserRole.ADMIN.name,
            averageRating = 5.0,
            reviewsCount = 5
        )
    )

    private val initialCategories = listOf(
        CategoryDto(id = "cat-eco", name = "Екологія та довкілля", boundEventsCount = 2),
        CategoryDto(id = "cat-defense", name = "Допомога ЗСУ та ВПО", boundEventsCount = 2),
        CategoryDto(id = "cat-animals", name = "Допомога тваринам", boundEventsCount = 1),
        CategoryDto(id = "cat-urban", name = "Благоустрій громади", boundEventsCount = 1),
        CategoryDto(id = "cat-education", name = "Освіта та культура", boundEventsCount = 0)
    )

    private val initialEvents = listOf(
        EventDto(
            id = "event-1",
            title = "Весняне прибирання парку «Наталка» та берегової лінії Дніпра",
            description = "Запрошуємо небайдужих мешканців Оболоні та всього Києва долучитися до великої толоки в парку «Наталка». Інвентар, рукавички, мішки для сортування пластику та скла надає організатор.",
            categoryId = "cat-eco",
            categoryName = "Екологія та довкілля",
            status = EventStatus.OPEN.name,
            dateTimeMillis = 1793437200000L,
            address = "м. Київ, Оболонська набережна, 9 (парк «Наталка»)",
            latitude = 50.5010,
            longitude = 30.5194,
            volunteerQuota = 25,
            approvedCount = 8,
            organizerId = "user-organizer-1",
            organizerName = "Тарас Мельник",
            organizerRating = 4.8,
            organizerContactPhone = "+380 50 987 65 43"
        ),
        EventDto(
            id = "event-2",
            title = "Плетіння маскувальних сіток та формування гуманітарних наборів",
            description = "Волонтерський хаб на Подолі запрошує помічників для плетіння сезонних маскувальних сіток та фасування теплих речей і медикаментів для захисників та вимушених переселенців.",
            categoryId = "cat-defense",
            categoryName = "Допомога ЗСУ та ВПО",
            status = EventStatus.OPEN.name,
            dateTimeMillis = 1793523600000L,
            address = "м. Київ, вул. Спаська, 15",
            latitude = 50.4666,
            longitude = 30.5177,
            volunteerQuota = 15,
            approvedCount = 11,
            organizerId = "user-organizer-1",
            organizerName = "Тарас Мельник",
            organizerRating = 4.8,
            organizerContactPhone = "+380 50 987 65 43"
        ),
        EventDto(
            id = "event-3",
            title = "Вигул та соціалізація собак у притулку «Сіріус»",
            description = "Потрібні волонтери для вигулу собак, прибирання вольєрів та соціалізації цуценят. Організовано централізований трансфер від станції метро «Героїв Дніпра» о 09:30.",
            categoryId = "cat-animals",
            categoryName = "Допомога тваринам",
            status = EventStatus.OPEN.name,
            dateTimeMillis = 1793610000000L,
            address = "Київська обл., с. Федорівка, притулок «Сіріус»",
            latitude = 50.8581,
            longitude = 30.2286,
            volunteerQuota = 20,
            approvedCount = 5,
            organizerId = "user-organizer-1",
            organizerName = "Тарас Мельник",
            organizerRating = 4.8,
            organizerContactPhone = "+380 50 987 65 43"
        ),
        EventDto(
            id = "event-4",
            title = "Відновлення дитячого майданчика у дворі на Сихові",
            description = "Фарбування дерев'яних елементів, ремонт лавок та висадка кущів лаванди й гортензії біля дитячого майданчика за підтримки місцевого ОСББ.",
            categoryId = "cat-urban",
            categoryName = "Благоустрій громади",
            status = EventStatus.IN_PROGRESS.name,
            dateTimeMillis = 1793264400000L,
            address = "м. Львів, просп. Червоної Калини, 62",
            latitude = 49.7931,
            longitude = 24.0561,
            volunteerQuota = 10,
            approvedCount = 10,
            organizerId = "user-organizer-1",
            organizerName = "Тарас Мельник",
            organizerRating = 4.8,
            organizerContactPhone = "+380 50 987 65 43"
        ),
        EventDto(
            id = "event-5",
            title = "Воркшоп із тактичної медицини та домедичної допомоги",
            description = "Навчальний інтенсив із зупинки критичних кровотеч та базової серцево-легеневої реанімації для мешканців району. Потрібні волонтери-координатори для реєстрації учасників.",
            categoryId = "cat-defense",
            categoryName = "Допомога ЗСУ та ВПО",
            status = EventStatus.PENDING_MODERATION.name,
            dateTimeMillis = 1793869200000L,
            address = "м. Київ, вул. Хрещатик, 22",
            latitude = 50.4488,
            longitude = 30.5227,
            volunteerQuota = 8,
            approvedCount = 0,
            organizerId = "user-organizer-1",
            organizerName = "Тарас Мельник",
            organizerRating = 4.8,
            organizerContactPhone = "+380 50 987 65 43"
        )
    )

    private val initialApplications = listOf(
        ApplicationDto(
            id = "app-1",
            eventId = "event-1",
            eventTitle = "Весняне прибирання парку «Наталка» та берегової лінії Дніпра",
            eventStatus = EventStatus.OPEN.name,
            volunteerId = "user-volunteer-1",
            volunteerName = "Олена Коваленко",
            volunteerRating = 4.9,
            motivationLetter = "Маю досвід організації еко-толок на Оболоні, візьму власні багаторазові рукавички.",
            status = ApplicationStatus.APPROVED.name,
            createdAtMillis = 1792800000000L
        ),
        ApplicationDto(
            id = "app-2",
            eventId = "event-4",
            eventTitle = "Відновлення дитячого майданчика у дворі на Сихові",
            eventStatus = EventStatus.IN_PROGRESS.name,
            volunteerId = "user-volunteer-1",
            volunteerName = "Олена Коваленко",
            volunteerRating = 4.9,
            motivationLetter = "Допоможу з фарбуванням та висадкою рослин.",
            status = ApplicationStatus.APPROVED.name,
            createdAtMillis = 1792700000000L
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

    private val initialReviews = listOf(
        ReviewDto(
            id = "review-1",
            eventId = "event-1",
            eventTitle = "Осіння висадка дерев на Подолі",
            reviewerId = "user-organizer-1",
            reviewerName = "Тарас Мельник",
            targetUserId = "user-volunteer-1",
            rating = 5,
            comment = "Надзвичайно відповідальна волонтерка! Дякуємо за активну участь.",
            createdAtMillis = 1792600000000L
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
                role = UserRole.VOLUNTEER.name,
                averageRating = 5.0,
                reviewsCount = 0
            ).also { newUser ->
                usersState.update { it + newUser }
            }

        if (existingUser.isBlocked) {
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
            role = role.name,
            averageRating = 0.0,
            reviewsCount = 0
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
            UserRole.VOLUNTEER -> usersState.value.first { it.role == UserRole.VOLUNTEER.name }
            UserRole.ORGANIZER -> usersState.value.first { it.role == UserRole.ORGANIZER.name }
            UserRole.ADMIN -> usersState.value.first { it.role == UserRole.ADMIN.name }
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
        if (user.role == UserRole.GUEST.name) {
            return Result.failure(IllegalStateException("Увійдіть або зареєструйтеся для виконання цієї дії"))
        }
        if (text.isBlank()) {
            return Result.failure(IllegalArgumentException("Коментар не може бути порожнім"))
        }
        val newComment = CommentDto(
            id = "comment-${UUID.randomUUID()}",
            eventId = eventId,
            authorId = user.id,
            authorName = user.fullName,
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
        if (organizer.role != UserRole.ORGANIZER.name && organizer.role != UserRole.ADMIN.name) {
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
            status = EventStatus.PENDING_MODERATION.name,
            dateTimeMillis = dateTimeMillis,
            address = address.trim(),
            latitude = location.latitude,
            longitude = location.longitude,
            volunteerQuota = volunteerQuota,
            approvedCount = 0,
            photoUrls = photoUrls,
            organizerId = organizer.id,
            organizerName = organizer.fullName,
            organizerAvatarUrl = organizer.avatarUrl,
            organizerRating = organizer.averageRating,
            organizerContactPhone = organizer.phone.ifBlank { "+380 50 000 00 00" }
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
        val updated = currentEvent.copy(status = newStatus.name)
        eventsState.update { events -> events.map { if (it.id == eventId) updated else it } }

        if (newStatus == EventStatus.CANCELLED) {
            applicationsState.update { apps ->
                apps.map { app ->
                    if (app.eventId == eventId &&
                        (app.status == ApplicationStatus.PENDING.name || app.status == ApplicationStatus.APPROVED.name)
                    ) {
                        app.copy(
                            status = ApplicationStatus.REJECTED.name,
                            eventStatus = newStatus.name,
                            rejectionReason = "Захід скасовано організатором"
                        )
                    } else if (app.eventId == eventId) {
                        app.copy(eventStatus = newStatus.name)
                    } else {
                        app
                    }
                }
            }
        } else {
            applicationsState.update { apps ->
                apps.map { app ->
                    if (app.eventId == eventId) app.copy(eventStatus = newStatus.name) else app
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
        if (volunteer.role == UserRole.GUEST.name) {
            return Result.failure(IllegalStateException("Увійдіть або зареєструйтеся для виконання цієї дії"))
        }
        val event = eventsState.value.find { it.id == eventId }
            ?: return Result.failure(NoSuchElementException("Захід не знайдено"))
        if (event.status != EventStatus.OPEN.name) {
            return Result.failure(IllegalStateException("Подача заявок доступна лише для відкритих заходів"))
        }
        val newApp = ApplicationDto(
            id = "app-${UUID.randomUUID()}",
            eventId = event.id,
            eventTitle = event.title,
            eventStatus = event.status,
            volunteerId = volunteer.id,
            volunteerName = volunteer.fullName,
            volunteerAvatarUrl = volunteer.avatarUrl,
            volunteerRating = volunteer.averageRating,
            motivationLetter = motivationLetter.trim(),
            status = ApplicationStatus.PENDING.name,
            createdAtMillis = System.currentTimeMillis()
        )
        applicationsState.update { listOf(newApp) + it }
        return Result.success(newApp)
    }

    override suspend fun withdrawApplication(applicationId: String): Result<ApplicationDto> {
        delay(simulatedDelayMs)
        val app = applicationsState.value.find { it.id == applicationId }
            ?: return Result.failure(NoSuchElementException("Заявку не знайдено"))
        if (app.eventStatus == EventStatus.COMPLETED.name || app.eventStatus == EventStatus.CANCELLED.name) {
            return Result.failure(IllegalStateException("Неможливо відкликати заявку після завершення або скасування заходу"))
        }
        val wasApproved = app.status == ApplicationStatus.APPROVED.name
        val updated = app.copy(status = ApplicationStatus.WITHDRAWN.name)
        applicationsState.update { list -> list.map { if (it.id == applicationId) updated else it } }
        if (wasApproved) {
            eventsState.update { events ->
                events.map { event ->
                    if (event.id == app.eventId) {
                        event.copy(approvedCount = (event.approvedCount - 1).coerceAtLeast(0))
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
        val updated = app.copy(status = ApplicationStatus.APPROVED.name, rejectionReason = null)
        applicationsState.update { list -> list.map { if (it.id == applicationId) updated else it } }
        eventsState.update { events ->
            events.map { event ->
                if (event.id == app.eventId) {
                    val newCount = event.approvedCount + 1
                    val newStatus = if (newCount >= event.volunteerQuota && event.status == EventStatus.OPEN.name) {
                        EventStatus.IN_PROGRESS.name
                    } else {
                        event.status
                    }
                    event.copy(approvedCount = newCount, status = newStatus)
                } else {
                    event
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
            status = ApplicationStatus.REJECTED.name,
            rejectionReason = reason.trim()
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
        if (event.status != EventStatus.IN_PROGRESS.name && event.status != EventStatus.COMPLETED.name) {
            return Result.failure(
                IllegalStateException("Оцінювання доступне лише для заходів у статусі «В процесі» або «Виконано»")
            )
        }
        val alreadyReviewed = reviewsState.value.any {
            it.eventId == eventId && it.reviewerId == reviewer.id && it.targetUserId == targetUserId
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
            reviewerId = reviewer.id,
            reviewerName = reviewer.fullName,
            targetUserId = targetUserId,
            rating = rating,
            comment = comment.trim(),
            createdAtMillis = System.currentTimeMillis()
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
            bio = bio.trim()
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
            status = EventStatus.CANCELLED.name,
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
                role = UserRole.VOLUNTEER.name
            )
        val blocked = target.copy(isBlocked = true, blockReason = reason.trim())
        usersState.update { list ->
            if (list.any { it.id == userId }) {
                list.map { if (it.id == userId) blocked else it }
            } else {
                list + blocked
            }
        }
        eventsState.update { events ->
            events.map { event ->
                if (event.organizerId == userId) {
                    event.copy(status = EventStatus.CANCELLED.name)
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
