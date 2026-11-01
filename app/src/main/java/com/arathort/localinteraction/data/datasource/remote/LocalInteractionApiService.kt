package com.arathort.localinteraction.data.datasource.remote

import com.arathort.localinteraction.data.dto.ApplicationCreateDto
import com.arathort.localinteraction.data.dto.ApplicationDto
import com.arathort.localinteraction.data.dto.ApplicationReviewRequestDto
import com.arathort.localinteraction.data.dto.AuthTokenDto
import com.arathort.localinteraction.data.dto.CategoryDto
import com.arathort.localinteraction.data.dto.CommentDto
import com.arathort.localinteraction.data.dto.ComplaintDto
import com.arathort.localinteraction.data.dto.EventApplicationsManageResponseDto
import com.arathort.localinteraction.data.dto.EventCreateDto
import com.arathort.localinteraction.data.dto.EventDto
import com.arathort.localinteraction.data.dto.EventModerationRequestDto
import com.arathort.localinteraction.data.dto.EventStatusUpdateDto
import com.arathort.localinteraction.data.dto.EventUpdateDto
import com.arathort.localinteraction.data.dto.ProfileUpdateDto
import com.arathort.localinteraction.data.dto.ReviewDto
import com.arathort.localinteraction.data.dto.UserCreateDto
import com.arathort.localinteraction.data.dto.UserDto
import com.arathort.localinteraction.data.dto.UserResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface LocalInteractionApiService {
    @FormUrlEncoded
    @POST("api/v1/auth/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): AuthTokenDto

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: UserCreateDto): UserResponseDto

    @POST("api/v1/auth/logout")
    suspend fun logout()

    @GET("api/v1/profile/me")
    suspend fun getMyProfile(): UserDto

    @PUT("api/v1/profile/me")
    suspend fun updateMyProfile(@Body request: ProfileUpdateDto): UserDto

    @GET("api/v1/profile/{user_id}")
    suspend fun getUserPublicProfile(@Path("user_id") userId: String): UserDto

    @GET("api/v1/events")
    suspend fun getEvents(
        @Query("search") search: String? = null,
        @Query("categories") categories: List<String>? = null,
        @Query("skip") skip: Int? = null,
        @Query("limit") limit: Int? = null
    ): List<EventDto>

    @POST("api/v1/events")
    suspend fun createEvent(@Body request: EventCreateDto): EventDto

    @GET("api/v1/events/my")
    suspend fun getMyEvents(): List<EventDto>

    @GET("api/v1/events/moderation/pending")
    suspend fun getPendingModerationEvents(): List<EventDto>

    @GET("api/v1/events/{id}")
    suspend fun getEventById(@Path("id") eventId: String): EventDto

    @PUT("api/v1/events/{id}")
    suspend fun updateEvent(
        @Path("id") eventId: String,
        @Body request: EventUpdateDto
    ): EventDto

    @DELETE("api/v1/events/{id}")
    suspend fun deleteEvent(@Path("id") eventId: String)

    @PATCH("api/v1/events/{id}/status")
    suspend fun changeEventStatus(
        @Path("id") eventId: String,
        @Body request: EventStatusUpdateDto
    ): EventDto

    @PATCH("api/v1/events/{id}/moderation")
    suspend fun moderateEvent(
        @Path("id") eventId: String,
        @Body request: EventModerationRequestDto
    ): EventDto

    @POST("api/v1/events/{id}/applications")
    suspend fun applyForEvent(
        @Path("id") eventId: String,
        @Body request: ApplicationCreateDto
    ): ApplicationDto

    @GET("api/v1/events/{id}/applications")
    suspend fun getEventApplications(
        @Path("id") eventId: String
    ): EventApplicationsManageResponseDto

    @GET("api/v1/applications/my")
    suspend fun getMyApplications(): List<ApplicationDto>

    @GET("api/v1/applications/{id}")
    suspend fun getApplicationDetails(@Path("id") applicationId: String): ApplicationDto

    @PATCH("api/v1/applications/{id}/withdraw")
    suspend fun withdrawApplication(@Path("id") applicationId: String): ApplicationDto

    @PATCH("api/v1/applications/{id}/review")
    suspend fun reviewVolunteerApplication(
        @Path("id") applicationId: String,
        @Body request: ApplicationReviewRequestDto
    ): ApplicationDto

    @GET("api/v1/categories")
    suspend fun getCategories(): List<CategoryDto>

    @GET("api/v1/events/{id}/comments")
    suspend fun getEventComments(@Path("id") eventId: String): List<CommentDto>

    @GET("api/v1/users/{id}")
    suspend fun getUserProfile(@Path("id") userId: String): UserDto

    @GET("api/v1/users/{id}/reviews")
    suspend fun getUserReviews(@Path("id") userId: String): List<ReviewDto>

    @GET("api/v1/admin/complaints")
    suspend fun getComplaints(): List<ComplaintDto>
}
