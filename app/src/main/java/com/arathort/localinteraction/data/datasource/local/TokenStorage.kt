package com.arathort.localinteraction.data.datasource.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

interface TokenStorage {
    val accessTokenFlow: Flow<String?>
    suspend fun saveAccessToken(token: String)
    suspend fun clearAccessToken()
}

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_tokens")

@Singleton
class DataStoreTokenStorage @Inject constructor(
    @param:ApplicationContext private val context: Context
) : TokenStorage {

    private object Keys {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
    }

    override val accessTokenFlow: Flow<String?> = context.authDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[Keys.ACCESS_TOKEN]
        }

    override suspend fun saveAccessToken(token: String) {
        context.authDataStore.edit { preferences ->
            preferences[Keys.ACCESS_TOKEN] = token
        }
    }

    override suspend fun clearAccessToken() {
        context.authDataStore.edit { preferences ->
            preferences.remove(Keys.ACCESS_TOKEN)
        }
    }
}
