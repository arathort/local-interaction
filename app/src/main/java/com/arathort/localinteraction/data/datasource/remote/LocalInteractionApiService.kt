package com.arathort.localinteraction.data.datasource.remote

import com.arathort.localinteraction.data.dto.AdminUserRegistryItemDto
import com.arathort.localinteraction.data.dto.ApplicationCreateDto
import com.arathort.localinteraction.data.dto.ApplicationDto
import com.arathort.localinteraction.data.dto.ApplicationReviewRequestDto
import com.arathort.localinteraction.data.dto.AuthTokenDto
import com.arathort.localinteraction.data.dto.CategoryAdminResponseDto
import com.arathort.localinteraction.data.dto.CategoryCreateUpdateRequestDto
import com.arathort.localinteraction.data.dto.CategoryDeleteResponseDto
import com.arathort.localinteraction.data.dto.CategoryDto
import com.arathort.localinteraction.data.dto.CommentCreateDto
import com.arathort.localinteraction.data.dto.CommentDto
import com.arathort.localinteraction.data.dto.ComplaintCreateDto
import com.arathort.localinteraction.data.dto.ComplaintRejectRequestDto
import com.arathort.localinteraction.data.dto.ComplaintResponseDto
import com.arathort.localinteraction.data.dto.ConversationSummaryResponseDto
import com.arathort.localinteraction.data.dto.DirectMessageCreateDto
import com.arathort.localinteraction.data.dto.DirectMessageResponseDto
import com.arathort.localinteraction.data.dto.EventApplicationsManageResponseDto
import com.arathort.localinteraction.data.dto.EventCreateDto
import com.arathort.localinteraction.data.dto.EventDto
import com.arathort.localinteraction.data.dto.EventModerationRequestDto
import com.arathort.localinteraction.data.dto.EventStatusUpdateDto
import com.arathort.localinteraction.data.dto.EventUpdateDto
import com.arathort.localinteraction.data.dto.ForgotPasswordRequestDto
import com.arathort.localinteraction.data.dto.NotificationListResponseDto
import com.arathort.localinteraction.data.dto.NotificationResponseDto
import com.arathort.localinteraction.data.dto.OAuthLoginRequestDto
import com.arathort.localinteraction.data.dto.OrganizerReviewVolunteerCreateDto
import com.arathort.localinteraction.data.dto.ProfileUpdateDto
import com.arathort.localinteraction.data.dto.ResetPasswordConfirmRequestDto
import com.arathort.localinteraction.data.dto.ReviewResponseDto
import com.arathort.localinteraction.data.dto.ReviewTargetInfoDto
import com.arathort.localinteraction.data.dto.SupportTicketCreateDto
import com.arathort.localinteraction.data.dto.SupportTicketResponseDto
import com.arathort.localinteraction.data.dto.SupportTicketUpdateStatusDto
import com.arathort.localinteraction.data.dto.UserBlockRequestDto
import com.arathort.localinteraction.data.dto.UserCreateDto
import com.arathort.localinteraction.data.dto.UserDto
import com.arathort.localinteraction.data.dto.UserResponseDto
import com.arathort.localinteraction.data.dto.VolunteerReviewOrganizerCreateDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
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

    @POST("api/v1/auth/oauth/{provider}")
    suspend fun oauthLogin(
        @Path("provider") provider: String,
        @Body request: OAuthLoginRequestDto
    ): AuthTokenDto

    @POST("api/v1/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequestDto)

    @POST("api/v1/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordConfirmRequestDto)

    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(): AuthTokenDto

    @POST("api/v1/auth/logout")
    suspend fun logout()

    @GET("api/v1/profile/me")
    suspend fun getMyProfile(): UserDto

    @PUT("api/v1/profile/me")
    suspend fun updateMyProfile(@Body request: ProfileUpdateDto): UserDto

    @Multipart
    @POST("api/v1/profile/avatar")
    suspend fun uploadAvatar(@Part file: MultipartBody.Part)

    @DELETE("api/v1/profile/avatar")
    suspend fun deleteAvatar()

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

    @GET("api/v1/events/{event_id}")
    suspend fun getEventById(@Path("event_id") eventId: String): EventDto

    @PUT("api/v1/events/{event_id}")
    suspend fun updateEvent(
        @Path("event_id") eventId: String,
        @Body request: EventUpdateDto
    ): EventDto

    @DELETE("api/v1/events/{event_id}")
    suspend fun deleteEvent(@Path("event_id") eventId: String): Response<Unit>

    @PATCH("api/v1/events/{event_id}/status")
    suspend fun changeEventStatus(
        @Path("event_id") eventId: String,
        @Body request: EventStatusUpdateDto
    ): EventDto

    @Multipart
    @POST("api/v1/events/{event_id}/photos")
    suspend fun uploadEventPhotos(
        @Path("event_id") eventId: String,
        @Part files: List<MultipartBody.Part>
    ): EventDto

    @PATCH("api/v1/events/{event_id}/moderation")
    suspend fun moderateEvent(
        @Path("event_id") eventId: String,
        @Body request: EventModerationRequestDto
    ): EventDto

    @POST("api/v1/events/{event_id}/applications")
    suspend fun applyForEvent(
        @Path("event_id") eventId: String,
        @Body request: ApplicationCreateDto
    ): ApplicationDto

    @GET("api/v1/events/{event_id}/applications")
    suspend fun getEventApplications(
        @Path("event_id") eventId: String
    ): EventApplicationsManageResponseDto

    @GET("api/v1/applications/my")
    suspend fun getMyApplications(): List<ApplicationDto>

    @GET("api/v1/applications/{application_id}")
    suspend fun getApplicationDetails(@Path("application_id") applicationId: String): ApplicationDto

    @PATCH("api/v1/applications/{application_id}/withdraw")
    suspend fun withdrawApplication(@Path("application_id") applicationId: String): ApplicationDto

    @PATCH("api/v1/applications/{application_id}/review")
    suspend fun reviewVolunteerApplication(
        @Path("application_id") applicationId: String,
        @Body request: ApplicationReviewRequestDto
    ): ApplicationDto

    @GET("api/v1/categories")
    suspend fun getCategories(): List<CategoryDto>

    @GET("api/v1/events/{event_id}/comments")
    suspend fun getEventComments(
        @Path("event_id") eventId: String,
        @Query("skip") skip: Int? = null,
        @Query("limit") limit: Int? = null
    ): List<CommentDto>

    @POST("api/v1/events/{event_id}/comments")
    suspend fun createEventComment(
        @Path("event_id") eventId: String,
        @Body request: CommentCreateDto
    ): CommentDto

    @DELETE("api/v1/comments/{comment_id}")
    suspend fun deleteComment(@Path("comment_id") commentId: String): Response<Unit>

    @GET("api/v1/events/{event_id}/reviews/form-info/{target_user_id}")
    suspend fun getReviewFormInfo(
        @Path("event_id") eventId: String,
        @Path("target_user_id") targetUserId: String
    ): ReviewTargetInfoDto

    @POST("api/v1/events/{event_id}/reviews/organizer")
    suspend fun submitReviewForOrganizer(
        @Path("event_id") eventId: String,
        @Body request: VolunteerReviewOrganizerCreateDto
    ): ReviewResponseDto

    @POST("api/v1/events/{event_id}/reviews/volunteer")
    suspend fun submitReviewForVolunteer(
        @Path("event_id") eventId: String,
        @Body request: OrganizerReviewVolunteerCreateDto
    ): ReviewResponseDto

    @GET("api/v1/events/{event_id}/reviews")
    suspend fun getEventReviews(
        @Path("event_id") eventId: String
    ): List<ReviewResponseDto>

    @GET("api/v1/categories/admin")
    suspend fun getCategoriesAdmin(): List<CategoryAdminResponseDto>

    @POST("api/v1/categories")
    suspend fun createCategory(
        @Body request: CategoryCreateUpdateRequestDto
    ): CategoryAdminResponseDto

    @PUT("api/v1/categories/{category_id}")
    suspend fun updateCategory(
        @Path("category_id") categoryId: String,
        @Body request: CategoryCreateUpdateRequestDto
    ): CategoryAdminResponseDto

    @DELETE("api/v1/categories/{category_id}")
    suspend fun deleteCategory(
        @Path("category_id") categoryId: String
    ): CategoryDeleteResponseDto

    @GET("api/v1/admin/users")
    suspend fun getAdminUsers(
        @Query("role") role: String? = null,
        @Query("account_status") accountStatus: String? = null
    ): List<AdminUserRegistryItemDto>

    @PATCH("api/v1/admin/users/{user_id}/block")
    suspend fun blockUser(
        @Path("user_id") userId: String,
        @Body request: UserBlockRequestDto
    ): AdminUserRegistryItemDto

    @PATCH("api/v1/admin/users/{user_id}/unblock")
    suspend fun unblockUser(
        @Path("user_id") userId: String
    ): AdminUserRegistryItemDto

    @POST("api/v1/complaints")
    suspend fun createComplaint(
        @Body request: ComplaintCreateDto
    ): ComplaintResponseDto

    @GET("api/v1/admin/complaints")
    suspend fun getAdminComplaints(
        @Query("status_filter") statusFilter: String? = null
    ): List<ComplaintResponseDto>

    @GET("api/v1/admin/complaints/{complaint_id}")
    suspend fun getComplaintDetails(
        @Path("complaint_id") complaintId: String
    ): ComplaintResponseDto

    @PATCH("api/v1/admin/complaints/{complaint_id}/reject")
    suspend fun rejectComplaint(
        @Path("complaint_id") complaintId: String,
        @Body request: ComplaintRejectRequestDto
    ): ComplaintResponseDto

    @POST("api/v1/admin/complaints/{complaint_id}/delete-content")
    suspend fun deleteReportedContent(
        @Path("complaint_id") complaintId: String
    ): ComplaintResponseDto

    @POST("api/v1/messages")
    suspend fun sendDirectMessage(
        @Body request: DirectMessageCreateDto
    ): DirectMessageResponseDto

    @GET("api/v1/messages/conversations")
    suspend fun getMyConversations(): List<ConversationSummaryResponseDto>

    @GET("api/v1/messages/conversations/{interlocutor_id}")
    suspend fun getConversationHistory(
        @Path("interlocutor_id") interlocutorId: String,
        @Query("skip") skip: Int? = null,
        @Query("limit") limit: Int? = null
    ): List<DirectMessageResponseDto>

    @POST("api/v1/support")
    suspend fun submitSupportTicket(
        @Body request: SupportTicketCreateDto
    ): SupportTicketResponseDto

    @GET("api/v1/support/my")
    suspend fun getMySupportTickets(): List<SupportTicketResponseDto>

    @GET("api/v1/support/admin")
    suspend fun getAdminSupportTickets(
        @Query("status_filter") statusFilter: String? = null
    ): List<SupportTicketResponseDto>

    @PATCH("api/v1/support/admin/{ticket_id}")
    suspend fun updateSupportTicketStatus(
        @Path("ticket_id") ticketId: String,
        @Body request: SupportTicketUpdateStatusDto
    ): SupportTicketResponseDto

    @GET("api/v1/notifications")
    suspend fun getMyNotifications(
        @Query("unread_only") unreadOnly: Boolean? = null,
        @Query("skip") skip: Int? = null,
        @Query("limit") limit: Int? = null
    ): NotificationListResponseDto

    @PATCH("api/v1/notifications/read-all")
    suspend fun markAllNotificationsAsRead()

    @PATCH("api/v1/notifications/{notification_id}/read")
    suspend fun markNotificationAsRead(
        @Path("notification_id") notificationId: String
    ): NotificationResponseDto
}
