package com.justcalls.data.storage

import platform.Foundation.NSUserDefaults

actual class TokenStorage {
    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val ACCESS_TOKEN_KEY = "access_token"
    private val REFRESH_TOKEN_KEY = "refresh_token"
    
    actual fun saveAccessToken(token: String) {
        userDefaults.setObject(token, forKey = ACCESS_TOKEN_KEY)
    }
    
    actual fun getAccessToken(): String? {
        return userDefaults.stringForKey(ACCESS_TOKEN_KEY)
    }
    
    actual fun clearTokens() {
        userDefaults.removeObjectForKey(ACCESS_TOKEN_KEY)
        userDefaults.removeObjectForKey(REFRESH_TOKEN_KEY)
    }
    
    actual fun saveRefreshToken(token: String) {
        userDefaults.setObject(token, forKey = REFRESH_TOKEN_KEY)
    }
    
    actual fun getRefreshToken(): String? {
        return userDefaults.stringForKey(REFRESH_TOKEN_KEY)
    }
}

