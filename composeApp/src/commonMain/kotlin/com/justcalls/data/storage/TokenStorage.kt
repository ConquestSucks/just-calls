package com.justcalls.data.storage

expect class TokenStorage() {
    fun saveAccessToken(token: String)
    fun getAccessToken(): String?
    fun clearTokens()
    fun saveRefreshToken(token: String)
    fun getRefreshToken(): String?
}

