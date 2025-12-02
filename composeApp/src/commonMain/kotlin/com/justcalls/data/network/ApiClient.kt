package com.justcalls.data.network

import com.justcalls.data.network.cookies.LoggingCookiesStorage
import com.justcalls.data.storage.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

expect fun createHttpClientEngine(): HttpClientEngine

class ApiClient(
    private val tokenStorage: TokenStorage,
    val baseUrl: String = "https://justcalls.ghjc.ru/api/v1"
) {
    private val cookiesStorage = LoggingCookiesStorage(AcceptAllCookiesStorage(), tokenStorage)
    
    val client = HttpClient(createHttpClientEngine()) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = false
            })
        }
        
        install(HttpTimeout) {
            connectTimeoutMillis = 30_000
            requestTimeoutMillis = 60_000
            socketTimeoutMillis = 60_000
        }
        
        install(HttpCookies) {
            storage = cookiesStorage
        }
    }
    
    fun getAuthHeader(): String? {
        val token = tokenStorage.getAccessToken()
        return token?.let { "Bearer $it" }
    }
    
    fun clearCookies() {
        tokenStorage.saveRefreshToken("")
    }
}

