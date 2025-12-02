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
        block: suspend () -> T
    ): Result<T> {
        return try {
            Result.success(block())
        } catch (e: io.ktor.client.plugins.ClientRequestException) {
            val statusCode = e.response.status.value
            if (statusCode == 401) {
                println("[RoomService] 401 Unauthorized, пытаемся обновить токен...")
                val refreshResult = authService.refresh()
                val refreshBody = refreshResult.getOrNull()
                val isUnauthorized = refreshBody?.success == false &&
                    refreshBody.error?.code.equals("UNAUTHORIZED", ignoreCase = true)

                if (isUnauthorized) {
                    println("[RoomService] refresh тоже вернул UNAUTHORIZED")
                    Result.failure(e)
                } else {
                    println("[RoomService] Токен обновлен, повторяем запрос комнаты")
                    Result.success(block())
                }
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createRoom(title: String): Result<ApiResult<String>> {
        return executeWithTokenRefresh {
            val authHeader = apiClient.getAuthHeader()
            println("[RoomService] createRoom - title=$title, authHeader=${authHeader != null}")

            val response = apiClient.client.post("${apiClient.baseUrl}/room/") {
                contentType(ContentType.Application.Json)
                if (authHeader != null) {
                    header(HttpHeaders.Authorization, authHeader)
                }
                setBody(CreateRoomRequest(title = title))
            }.body<ApiResult<String>>()

            println("[RoomService] createRoom - success=${response.success}, data=${response.data}, error=${response.error?.message}")
            response
        }
    }

    suspend fun getRooms(): Result<ApiResult<List<RoomDto>>> {
        return executeWithTokenRefresh {
            val authHeader = apiClient.getAuthHeader()
            val url = "${apiClient.baseUrl}/room/"
            println("[RoomService] getRooms - URL=$url, authHeaderPresent=${authHeader != null}")

            val httpResponse: HttpResponse = apiClient.client.get(url) {
                if (authHeader != null) {
                    header(HttpHeaders.Authorization, authHeader)
                }
            }

            val setCookieHeaders = httpResponse.headers.getAll(HttpHeaders.SetCookie)
            println("[RoomService] getRooms - HTTP статус: ${httpResponse.status}")
            println("[RoomService] getRooms - Set-Cookie заголовки: $setCookieHeaders")

            val response = httpResponse.body<ApiResult<List<RoomDto>>>()

            println(
                "[RoomService] getRooms - success=${response.success}, " +
                    "count=${response.data?.size}, errorCode=${response.error?.code}, " +
                    "errorMessage=${response.error?.message}"
            )
            response
        }
    }

    suspend fun getRoomToken(roomKey: String): Result<ApiResult<RoomTokenResult>> {
        return executeWithTokenRefresh {
            val authHeader = apiClient.getAuthHeader()
            println("[RoomService] getRoomToken - roomKey=$roomKey, authHeader=${authHeader != null}")

            val response = apiClient.client.get("${apiClient.baseUrl}/room/token") {
                if (authHeader != null) {
                    header(HttpHeaders.Authorization, authHeader)
                }
                parameter("roomKey", roomKey)
            }.body<ApiResult<RoomTokenResult>>()

            println("[RoomService] getRoomToken - success=${response.success}, userIdentity=${response.data?.userIdentity}, error=${response.error?.message}")
            response
        }
    }
}


