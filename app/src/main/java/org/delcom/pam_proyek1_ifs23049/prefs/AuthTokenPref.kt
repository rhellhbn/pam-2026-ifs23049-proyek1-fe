package org.delcom.pam_proyek1_ifs23049.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_token_prefs")

class AuthTokenPref(private val context: Context) {

    companion object {
        private val AUTH_TOKEN_KEY = stringPreferencesKey("AUTH_TOKEN_KEY")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("REFRESH_TOKEN_KEY")
        private val DARK_MODE_KEY = stringPreferencesKey("DARK_MODE_KEY")
    }

    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { it[AUTH_TOKEN_KEY] = token }
    }

    suspend fun getAuthToken(): String? =
        context.dataStore.data.map { it[AUTH_TOKEN_KEY] }.first()

    suspend fun clearAuthToken() {
        context.dataStore.edit { it.remove(AUTH_TOKEN_KEY) }
    }

    suspend fun saveRefreshToken(token: String) {
        context.dataStore.edit { it[REFRESH_TOKEN_KEY] = token }
    }

    suspend fun getRefreshToken(): String? =
        context.dataStore.data.map { it[REFRESH_TOKEN_KEY] }.first()

    suspend fun clearRefreshToken() {
        context.dataStore.edit { it.remove(REFRESH_TOKEN_KEY) }
    }

    suspend fun saveDarkMode(isDark: Boolean) {
        context.dataStore.edit { it[DARK_MODE_KEY] = if (isDark) "true" else "false" }
    }

    suspend fun getDarkMode(): Boolean? {
        val v = context.dataStore.data.map { it[DARK_MODE_KEY] }.first()
        return if (v == null) null else v == "true"
    }
}