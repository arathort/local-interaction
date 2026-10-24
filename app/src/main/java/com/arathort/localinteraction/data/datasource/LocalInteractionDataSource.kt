package com.arathort.localinteraction.data.datasource

import com.arathort.localinteraction.data.dto.ApplicationDto
import com.arathort.localinteraction.data.dto.CategoryDto
import com.arathort.localinteraction.data.dto.CommentDto
import com.arathort.localinteraction.data.dto.ComplaintDto
import com.arathort.localinteraction.data.dto.EventDto
import com.arathort.localinteraction.data.dto.ReviewDto
import com.arathort.localinteraction.data.dto.UserDto
import com.arathort.localinteraction.domain.model.EventStatus
import com.arathort.localinteraction.domain.model.GeoPoint
import com.arathort.localinteraction.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

interface LocalInteractionDataSource {
    val currentUser: Flow<UserDto>
    fun observeEvents(): Flow<List<EventDto>>
    fun observeCategories(): Flow<List<CategoryDto>>
    fun observeApplications(): Flow<List<ApplicationDto>>
    fun observeComments(): Flow<List<CommentDto>>
    fun observeReviews(): Flow<List<ReviewDto>>
    fun observeComplaints(): Flow<List<ComplaintDto>>

    suspend fun login(email: String, password: String): Result<UserDto>
    suspend fun register(
        fullName: String,
        email: String,
        password: String,
        role: UserRole,
        acceptedPrivacyPolicy: Boolean
    ): Result<UserDto>
    suspend fun logout()
    suspend fun switchDemoRole(role: UserRole): Result<UserDto>

    suspend fun getEventById(eventId: String): Result<EventDto>
    suspend fun addComment(eventId: String, text: String): Result<CommentDto>
    suspend fun createEvent(
        title: String,
        description: String,
        categoryId: String,
        dateTimeMillis: Long,
        address: String,
        location: GeoPoint,
        volunteerQuota: Int,
        photoUrls: List<String>
    ): Result<EventDto>
    suspend fun updateEventStatus(eventId: String, newStatus: EventStatus): Result<EventDto>

    suspend fun submitApplication(eventId: String, motivationLetter: String): Result<ApplicationDto>
    suspend fun withdrawApplication(applicationId: String): Result<ApplicationDto>
    suspend fun approveApplication(applicationId: String): Result<ApplicationDto>
    suspend fun rejectApplication(applicationId: String, reason: String): Result<ApplicationDto>

    suspend fun submitReview(
        eventId: String,
        targetUserId: String,
        rating: Int,
        comment: String
    ): Result<ReviewDto>

    suspend fun getUserProfile(userId: String): Result<UserDto>
    suspend fun updateProfile(fullName: String, phone: String, bio: String): Result<UserDto>

    suspend fun approveEventModeration(eventId: String): Result<EventDto>
    suspend fun rejectEventModeration(eventId: String, reason: String): Result<EventDto>
    suspend fun createCategory(name: String): Result<CategoryDto>
    suspend fun updateCategory(categoryId: String, name: String): Result<CategoryDto>
    suspend fun archiveCategory(categoryId: String): Result<CategoryDto>
    suspend fun blockUser(userId: String, reason: String): Result<UserDto>
}
