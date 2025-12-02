package com.justcalls.data.network

import com.justcalls.data.models.requests.CompleteRequest
import com.justcalls.data.models.requests.SignInRequest
import com.justcalls.data.models.requests.SignUpRequest
import com.justcalls.data.models.requests.UpdateUserRequest
import com.justcalls.data.models.responses.ApiResult
import com.justcalls.data.models.responses.RefreshTokenResponse
import com.justcalls.data.models.responses.SignInResponse
import com.justcalls.data.models.responses.SignUpResponse
import com.justcalls.data.models.responses.UserResponse
import com.justcalls.data.storage.TokenStorage
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode

class AuthService(
    private val apiClient: ApiClient,
    private val tokenStorage: TokenStorage
) {
    private suspend fun <T> executeWithTokenRefresh(
        request: suspend () -> Result<ApiResult<T>>
    ): Result<ApiResult<T>> {
        val result = request()
        
        var shouldRefresh = false
        
        result.fold(
            onSuccess = { apiResult ->
                if (!apiResult.success && apiResult.error?.code?.equals("UNAUTHORIZED", ignoreCase = true) == true) {
                    shouldRefresh = true
                }
            },
            onFailure = { exception ->
                if (exception is ClientRequestException && exception.response.status == HttpStatusCode.Unauthorized) {
                    shouldRefresh = true
                } else {
                    val message = exception.message ?: ""
                    if (message.contains("401") || message.contains("Unauthorized")) {
                        shouldRefresh = true
                    }
                }
            }
        )
        
        if (shouldRefresh) {
            val refreshResult = refresh()
            refreshResult.fold(
                onSuccess = { refreshResponse ->
                    if (refreshResponse.success && refreshResponse.data != null) {
                        return request()
                    }
                },
                onFailure = {
                }
            )
        }
        
        return result
    }
    suspend fun signUp(request: SignUpRequest): Result<ApiResult<SignUpResponse>> {
        return try {
            val response = apiClient.client.post("${apiClient.baseUrl}/auth/signUp") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(request)
            }.body<ApiResult<SignUpResponse>>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun complete(request: CompleteRequest): Result<ApiResult<Unit>> {
        return try {
            val response = apiClient.client.post("${apiClient.baseUrl}/auth/complete") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(request)
            }.body<ApiResult<Unit>>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signIn(request: SignInRequest): Result<ApiResult<SignInResponse>> {
        return try {
            println("[AuthService] signIn - Отправка запроса на ${apiClient.baseUrl}/auth/signIn")
            
            val httpResponse: HttpResponse = apiClient.client.post("${apiClient.baseUrl}/auth/signIn") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(request)
            }
            val setCookieHeaders = httpResponse.headers.getAll(HttpHeaders.SetCookie)
            println("[AuthService] signIn - HTTP статус: ${httpResponse.status}")
            println("[AuthService] signIn - Set-Cookie заголовки: $setCookieHeaders")
            
            val response = httpResponse.body<ApiResult<SignInResponse>>()
            
            println("[AuthService] signIn - Ответ получен: success=${response.success}")
            
            if (response.success && response.data != null) {
                tokenStorage.saveAccessToken(response.data.accessToken)
                println("[AuthService] signIn - Access token сохранен")
            }
            
            Result.success(response)
        } catch (e: Exception) {
            println("[AuthService] signIn - Ошибка: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    suspend fun refresh(): Result<ApiResult<RefreshTokenResponse>> {
        return try {
            println("[AuthService] refresh - Отправка запроса на ${apiClient.baseUrl}/auth/refresh")
            println("[AuthService] refresh - Cookies будут отправлены автоматически через HttpCookies плагин")
            
            val httpResponse: HttpResponse = apiClient.client.post("${apiClient.baseUrl}/auth/refresh") {
            }
            val setCookieHeaders = httpResponse.headers.getAll(HttpHeaders.SetCookie)

            val requestCookieHeader = httpResponse.call.request.headers[HttpHeaders.Cookie]
            println("[AuthService] refresh - HTTP статус: ${httpResponse.status}")
            println("[AuthService] refresh - Request Cookie header: $requestCookieHeader")
            println("[AuthService] refresh - Set-Cookie заголовки: $setCookieHeaders")
            
            val response = httpResponse.body<ApiResult<RefreshTokenResponse>>()
            
            println("[AuthService] refresh - Ответ получен: success=${response.success}, error=${response.error?.code}, message=${response.error?.message}")
            
            if (response.success && response.data != null) {
                tokenStorage.saveAccessToken(response.data.accessToken)
                println("[AuthService] refresh - Новый access token сохранен")
            } else {
                println("[AuthService] refresh - Ошибка обновления токена: ${response.error?.message}")
            }
            
            Result.success(response)
        } catch (e: Exception) {
            println("[AuthService] refresh - Исключение: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    suspend fun logout(): Result<ApiResult<Unit>> {
        return try {
            val authHeader = apiClient.getAuthHeader()
            val response = apiClient.client.post("${apiClient.baseUrl}/auth/logout") {
                if (authHeader != null) {
                    header(HttpHeaders.Authorization, authHeader)
                }
            }.body<ApiResult<Unit>>()
            
            if (response.success) {
                tokenStorage.clearTokens()
                apiClient.clearCookies()
            }
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUser(): Result<ApiResult<UserResponse>> {
        return executeWithTokenRefresh {
            try {
                val authHeader = apiClient.getAuthHeader()
                val response = apiClient.client.get("${apiClient.baseUrl}/user") {
                    if (authHeader != null) {
                        header(HttpHeaders.Authorization, authHeader)
                    }
                }.body<ApiResult<UserResponse>>()
                
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun updateUser(request: UpdateUserRequest): Result<ApiResult<UserResponse>> {
        return executeWithTokenRefresh {
            try {
                val authHeader = apiClient.getAuthHeader()
                val response = apiClient.client.put("${apiClient.baseUrl}/user") {
                    if (authHeader != null) {
                        header(HttpHeaders.Authorization, authHeader)
                    }
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    setBody(request)
                }.body<ApiResult<UserResponse>>()
                
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

