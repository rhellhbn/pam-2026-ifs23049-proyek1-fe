package org.delcom.pam_proyek1_ifs23049.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class AuthTokenPref(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("auth_token_prefs", Context.MODE_PRIVATE)

    fun saveAuthToken(token: String) {
        sharedPreferences.edit { putString("AUTH_TOKEN_KEY", token) }
    }

    fun getAuthToken(): String? {
        return sharedPreferences.getString("AUTH_TOKEN_KEY", null)
    }

    fun clearAuthToken() {
        sharedPreferences.edit { remove("AUTH_TOKEN_KEY") }
    }

    fun saveRefreshToken(token: String) {
        sharedPreferences.edit { putString("REFRESH_TOKEN_KEY", token) }
    }

    fun getRefreshToken(): String? {
        return sharedPreferences.getString("REFRESH_TOKEN_KEY", null)
    }

    fun clearRefreshToken() {
        sharedPreferences.edit { remove("REFRESH_TOKEN_KEY") }
    }
}