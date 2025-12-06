package com.justcalls.data.network

import com.justcalls.data.models.responses.ApiResult
import com.justcalls.data.models.responses.RoomDto
import com.justcalls.data.models.responses.RoomTokenResult
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

@Serializable
data class CreateRoomRequest(
    val title: String? = null
)

class RoomService(
    private val apiClient: ApiClient,
    private val authService: AuthService
) {

    private suspend fun <T> executeWithTokenRefresh(
        requestName: String,
        block: suspend () -> T
    ): Result<T> {
        return try {
            val result = block()
            Result.success(result)
        } catch (e: io.ktor.client.plugins.ClientRequestException) {
            val status = e.response.status
            val statusCode = status.value
            
            if (statusCode == 401) {
                val refreshResult = authService.refresh()
                
                refreshResult.fold(
                    onSuccess = { refreshBody ->
                        val isUnauthorized = !refreshBody.success && refreshBody.error?.code.equals("UNAUTHORIZED", ignoreCase = true)

                        if (isUnauthorized) {
                            Result.failure(e)
                        } else if (refreshBody.success && refreshBody.data != null) {
                            try {
                                val retryResult = block()
                                Result.success(retryResult)
                            } catch (retryException: Exception) {
                                Result.failure(retryException)
                            }
                        } else {
                            Result.failure(e)
                        }
                    },
                    onFailure = { refreshException ->
                        Result.failure(refreshException)
                    }
                )
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createRoom(title: String): Result<ApiResult<String>> {
        return executeWithTokenRefresh("createRoom") {
            val authHeader = apiClient.getAuthHeader()
            val url = "${apiClient.baseUrl}/room/"

            val httpResponse: HttpResponse = apiClient.client.post(url) {
                contentType(ContentType.Application.Json)
                if (authHeader != null) {
                    header(HttpHeaders.Authorization, authHeader)
                }
                setBody(CreateRoomRequest(title = title))
            }
            
            val response = httpResponse.body<ApiResult<String>>()
            response
        }
    }

    suspend fun getRooms(): Result<ApiResult<List<RoomDto>>> {
        return executeWithTokenRefresh("getRooms") {
            val authHeader = apiClient.getAuthHeader()
            val url = "${apiClient.baseUrl}/room/"

            val httpResponse: HttpResponse = apiClient.client.get(url) {
                if (authHeader != null) {
                    header(HttpHeaders.Authorization, authHeader)
                }
            }

            val response = httpResponse.body<ApiResult<List<RoomDto>>>()
            response
        }
    }

    suspend fun getRoomToken(roomKey: String): Result<ApiResult<RoomTokenResult>> {
        return executeWithTokenRefresh("getRoomToken") {
            val authHeader = apiClient.getAuthHeader()
            val url = "${apiClient.baseUrl}/room/token"

            val httpResponse: HttpResponse = apiClient.client.get(url) {
                if (authHeader != null) {
                    header(HttpHeaders.Authorization, authHeader)
                }
                parameter("roomKey", roomKey)
            }
            
            val response = httpResponse.body<ApiResult<RoomTokenResult>>()
            response
        }
    }
}
