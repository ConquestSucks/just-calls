package com.justcalls.data.storage

import android.content.Context
import android.content.SharedPreferences
import com.justcalls.JustCallsApplication
import androidx.core.content.edit

actual class TokenStorage {
    private val prefs: SharedPreferences by lazy {
        JustCallsApplication.instance.getSharedPreferences("justcalls_prefs", Context.MODE_PRIVATE)
    }
    
    private val ACCESS_TOKEN_KEY = "access_token"
    private val REFRESH_TOKEN_KEY = "refresh_token"
    
    actual fun saveAccessToken(token: String) {
        prefs.edit { putString(ACCESS_TOKEN_KEY, token) }
    }
    
    actual fun getAccessToken(): String? {
        return prefs.getString(ACCESS_TOKEN_KEY, null)
    }
    
    actual fun clearTokens() {
        prefs.edit {
            remove(ACCESS_TOKEN_KEY)
                .remove(REFRESH_TOKEN_KEY)
        }
    }
    
    actual fun saveRefreshToken(token: String) {
        prefs.edit { putString(REFRESH_TOKEN_KEY, token) }
    }
    
    actual fun getRefreshToken(): String? {
        return prefs.getString(REFRESH_TOKEN_KEY, null)
    }
}

