package com.arathort.localinteraction.data.datasource.remote

import com.arathort.localinteraction.data.dto.ApplicationDto
import com.arathort.localinteraction.data.dto.AuthTokenDto
import com.arathort.localinteraction.data.dto.CategoryDto
import com.arathort.localinteraction.data.dto.CommentDto
import com.arathort.localinteraction.data.dto.ComplaintDto
import com.arathort.localinteraction.data.dto.EventDto
import com.arathort.localinteraction.data.dto.ReviewDto
import com.arathort.localinteraction.data.dto.UserDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface LocalInteractionApiService {
    @POST("api/v1/auth/login")
    suspend fun login(): AuthTokenDto

    @GET("api/v1/events")
    suspend fun getEvents(): List<EventDto>

    @GET("api/v1/events/{id}")
    suspend fun getEventById(@Path("id") eventId: String): EventDto

    @GET("api/v1/categories")
    suspend fun getCategories(): List<CategoryDto>

    @GET("api/v1/events/{id}/applications")
    suspend fun getEventApplications(@Path("id") eventId: String): List<ApplicationDto>

    @GET("api/v1/events/{id}/comments")
    suspend fun getEventComments(@Path("id") eventId: String): List<CommentDto>

    @GET("api/v1/users/{id}")
    suspend fun getUserProfile(@Path("id") userId: String): UserDto

    @GET("api/v1/users/{id}/reviews")
    suspend fun getUserReviews(@Path("id") userId: String): List<ReviewDto>

    @GET("api/v1/admin/complaints")
    suspend fun getComplaints(): List<ComplaintDto>
}
