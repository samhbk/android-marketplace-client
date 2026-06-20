package com.marketplace.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore("auth")

class TokenStore(private val context: Context) {

    private val accessKey = stringPreferencesKey("access_token")
    private val refreshKey = stringPreferencesKey("refresh_token")

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val isLoggedIn = context.dataStore.data
        .map { prefs -> !prefs[accessKey].isNullOrBlank() }
        .stateIn(
            scope,
            SharingStarted.Eagerly,
            runBlocking {
                val prefs = context.dataStore.data.first()
                !prefs[accessKey].isNullOrBlank()
            },
        )

    suspend fun save(accessToken: String, refreshToken: String) {
        context.dataStore.edit { prefs ->
            prefs[accessKey] = accessToken
            prefs[refreshKey] = refreshToken
        }
    }

    suspend fun clear() {
        context.dataStore.edit { prefs ->
            prefs.remove(accessKey)
            prefs.remove(refreshKey)
        }
    }

    suspend fun accessToken(): String? =
        context.dataStore.data.map { it[accessKey] }.first()

    suspend fun refreshToken(): String? =
        context.dataStore.data.map { it[refreshKey] }.first()
}
