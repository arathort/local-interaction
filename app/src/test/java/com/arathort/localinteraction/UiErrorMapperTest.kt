package com.arathort.localinteraction

import com.arathort.localinteraction.presentation.util.UiErrorMapper
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class UiErrorMapperTest {

    @Test
    fun `maps IOException to network error`() {
        val exception = IOException("Failed to connect")
        val result = UiErrorMapper.map(exception)
        assertEquals(R.string.error_network, result.messageResId)
    }

    @Test
    fun `maps 401 HttpException to invalid credentials`() {
        val response = Response.error<Any>(401, "".toResponseBody())
        val exception = HttpException(response)
        val result = UiErrorMapper.map(exception)
        assertEquals(R.string.error_auth_invalid_credentials, result.messageResId)
    }

    @Test
    fun `maps 409 HttpException with already exists detail to email already exists`() {
        val errorJson = """{"detail": "User with this email already exists"}"""
        val response = Response.error<Any>(409, errorJson.toResponseBody())
        val exception = HttpException(response)
        val result = UiErrorMapper.map(exception)
        assertEquals(R.string.error_email_already_exists, result.messageResId)
    }

    @Test
    fun `maps 403 HttpException to account blocked`() {
        val response = Response.error<Any>(403, "".toResponseBody())
        val exception = HttpException(response)
        val result = UiErrorMapper.map(exception)
        assertEquals(R.string.error_account_blocked, result.messageResId)
    }

    @Test
    fun `maps 404 HttpException to user not found`() {
        val response = Response.error<Any>(404, "".toResponseBody())
        val exception = HttpException(response)
        val result = UiErrorMapper.map(exception)
        assertEquals(R.string.error_user_not_found, result.messageResId)
    }

    @Test
    fun `maps 429 HttpException to too many requests`() {
        val response = Response.error<Any>(429, "".toResponseBody())
        val exception = HttpException(response)
        val result = UiErrorMapper.map(exception)
        assertEquals(R.string.error_too_many_requests, result.messageResId)
    }

    @Test
    fun `maps 500 HttpException to server unavailable`() {
        val response = Response.error<Any>(500, "".toResponseBody())
        val exception = HttpException(response)
        val result = UiErrorMapper.map(exception)
        assertEquals(R.string.error_server_unavailable, result.messageResId)
    }
}
