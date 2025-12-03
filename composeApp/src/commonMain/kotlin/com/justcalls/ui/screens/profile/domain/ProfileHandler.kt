package com.justcalls.ui.screens.profile.domain

import com.justcalls.data.models.requests.UpdateUserRequest
import com.justcalls.data.network.AuthService
import com.justcalls.data.storage.TokenStorage
import com.justcalls.utils.AuthErrorHandler
import com.justcalls.utils.NetworkErrorHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileHandler(
    private val authService: AuthService,
    private val tokenStorage: TokenStorage,
    private val onLogout: () -> Unit,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    suspend fun loadProfile(
        onSuccess: (ProfileData) -> Unit,
        onError: (String) -> Unit,
        onTimeout: (String) -> Unit
    ) {
        try {
            val result = authService.getUser()
            
            result.fold(
                onSuccess = { apiResult ->
                    if (apiResult.success && apiResult.data != null) {
                        val user = apiResult.data
                        onSuccess(
                            ProfileData(
                                userId = user.id,
                                email = user.email,
                                displayName = user.displayName,
                                createdAt = user.createdAt
                            )
                        )
                    } else {
                        if (!AuthErrorHandler.handleUnauthorizedError(apiResult, tokenStorage, onLogout)) {
                            onError(apiResult.error?.message ?: "Ошибка загрузки данных")
                        }
                    }
                },
                onFailure = { exception ->
                    val isTimeout = exception is kotlinx.coroutines.TimeoutCancellationException || 
                                   exception.message?.contains("timeout", ignoreCase = true) == true ||
                                   exception.message?.contains("Timed out", ignoreCase = true) == true
                    
                    if (isTimeout) {
                        onTimeout("Превышено время ожидания загрузки профиля")
                    } else {
                        if (!AuthErrorHandler.handleUnauthorizedException(exception, tokenStorage, onLogout)) {
                            onError(NetworkErrorHandler.getErrorMessage(exception))
                        }
                    }
                }
            )
        } catch (e: Exception) {
            val isTimeout = e is kotlinx.coroutines.TimeoutCancellationException || 
                           e.message?.contains("timeout", ignoreCase = true) == true ||
                           e.message?.contains("Timed out", ignoreCase = true) == true
            
            if (isTimeout) {
                onTimeout("Превышено время ожидания загрузки профиля")
            } else {
                onError(e.message ?: "Ошибка при загрузке профиля")
            }
        }
    }
    
    fun saveProfile(
        displayName: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (displayName.isBlank()) {
            onError("Имя не может быть пустым")
            return
        }
        
        coroutineScope.launch {
            val result = authService.updateUser(UpdateUserRequest(displayName))
            
            result.fold(
                onSuccess = { apiResult ->
                    if (apiResult.success && apiResult.data != null) {
                        onSuccess()
                    } else {
                        if (!AuthErrorHandler.handleUnauthorizedError(apiResult, tokenStorage, onLogout)) {
                            onError(apiResult.error?.message ?: "Ошибка сохранения")
                        }
                    }
                },
                onFailure = { exception ->
                    if (!AuthErrorHandler.handleUnauthorizedException(exception, tokenStorage, onLogout)) {
                        onError(NetworkErrorHandler.getErrorMessage(exception))
                    }
                }
            )
        }
    }
}

data class ProfileData(
    val userId: String,
    val email: String,
    val displayName: String,
    val createdAt: String
)

