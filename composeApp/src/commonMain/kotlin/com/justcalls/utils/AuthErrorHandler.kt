package com.justcalls.utils

import com.justcalls.data.models.responses.ApiResult
import com.justcalls.data.storage.TokenStorage

object AuthErrorHandler {
    fun handleUnauthorizedError(
        apiResult: ApiResult<*>?,
        tokenStorage: TokenStorage,
        onUnauthorized: () -> Unit
    ): Boolean {
        if (apiResult?.error?.code == "UNAUTHORIZED") {
            tokenStorage.clearTokens()
            onUnauthorized()
            return true
        }
        return false
    }
    
    fun handleUnauthorizedException(
        exception: Throwable,
        tokenStorage: TokenStorage,
        onUnauthorized: () -> Unit
    ): Boolean {
        val message = exception.message ?: ""
        if (message.contains("401") || message.contains("Unauthorized")) {
            tokenStorage.clearTokens()
            onUnauthorized()
            return true
        }
        return false
    }
}

