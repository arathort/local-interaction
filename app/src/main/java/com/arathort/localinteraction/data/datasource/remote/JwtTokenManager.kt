package com.arathort.localinteraction.data.datasource.remote

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JwtTokenManager @Inject constructor() {
    @Volatile
    var accessToken: String? = null
        private set

    fun updateToken(token: String?) {
        accessToken = token
    }

    fun clearToken() {
        accessToken = null
    }
}
