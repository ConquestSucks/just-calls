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
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
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
                if (exception is ClientRequestException) {
                    val status = exception.response.status
                    if (status == HttpStatusCode.Unauthorized) {
                        shouldRefresh = true
                    }
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
                        val retryResult = request()
                        return retryResult
                    }
                },
                onFailure = { }
            )
        }
        
        return result
    }
    suspend fun signUp(request: SignUpRequest): Result<ApiResult<SignUpResponse>> {
        return try {
            val httpResponse = apiClient.client.post("${apiClient.baseUrl}/auth/signUp") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(request)
            }
            
            try {
                val response = httpResponse.body<ApiResult<SignUpResponse>>()
                Result.success(response)
            } catch (e: SerializationException) {
                // Если десериализация не удалась из-за нестабильного интернета,
                // но статус успешный (200-299), пытаемся извлечь GUID из сырого ответа
                if (httpResponse.status.value in 200..299) {
                    try {
                        // Читаем сырой текст ответа через call
                        val rawText = httpResponse.call.response.readText()
                        
                        // Пытаемся распарсить JSON вручную
                        val json = Json.parseToJsonElement(rawText) as? JsonObject
                        if (json != null && json["success"]?.jsonPrimitive?.content == "true") {
                            val dataValue = json["data"]?.jsonPrimitive?.content
                            if (dataValue != null) {
                                Result.success(ApiResult(
                                    success = true,
                                    data = dataValue,
                                    error = null
                                ))
                            } else {
                                Result.failure(e)
                            }
                        } else {
                            Result.failure(e)
                        }
                    } catch (ex: Exception) {
                        Result.failure(e)
                    }
                } else {
                    Result.failure(e)
                }
            }
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
            val httpResponse: HttpResponse = apiClient.client.post("${apiClient.baseUrl}/auth/signIn") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(request)
            }
            
            val response = httpResponse.body<ApiResult<SignInResponse>>()
            
            if (response.success && response.data != null) {
                tokenStorage.saveAccessToken(response.data.accessToken)
            }
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun refresh(): Result<ApiResult<RefreshTokenResponse>> {
        return try {
            val httpResponse: HttpResponse = apiClient.client.post("${apiClient.baseUrl}/auth/refresh") {
            }
            
            val response = httpResponse.body<ApiResult<RefreshTokenResponse>>()
            
            if (response.success && response.data != null) {
                tokenStorage.saveAccessToken(response.data.accessToken)
            }
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun logout(): Result<ApiResult<Unit>> {
        return try {
            val authHeader = apiClient.getAuthHeader()
            val url = "${apiClient.baseUrl}/auth/logout"
            
            val httpResponse: HttpResponse = apiClient.client.post(url) {
                if (authHeader != null) {
                    header(HttpHeaders.Authorization, authHeader)
                }
            }
            
            val response = httpResponse.body<ApiResult<Unit>>()
            
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
                val url = "${apiClient.baseUrl}/user"
                
                val httpResponse: HttpResponse = apiClient.client.get(url) {
                    if (authHeader != null) {
                        header(HttpHeaders.Authorization, authHeader)
                    }
                }
                
                val response = httpResponse.body<ApiResult<UserResponse>>()
                
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
                val url = "${apiClient.baseUrl}/user"
                
                val httpResponse: HttpResponse = apiClient.client.put(url) {
                    if (authHeader != null) {
                        header(HttpHeaders.Authorization, authHeader)
                    }
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    setBody(request)
                }
                
                val response = httpResponse.body<ApiResult<UserResponse>>()
                
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
