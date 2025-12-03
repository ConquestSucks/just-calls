package com.justcalls.data.network.cookies

import com.justcalls.data.storage.TokenStorage
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.http.Cookie
import io.ktor.http.Url
import io.ktor.http.decodeURLPart

class LoggingCookiesStorage(
    private val delegate: CookiesStorage,
    private val tokenStorage: TokenStorage
) : CookiesStorage {
    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
        if (cookie.name == "refresh_token") {
            val decodedValue = try {
                cookie.value.decodeURLPart()
            } catch (e: Throwable) {
                cookie.value
            }
            
            if (decodedValue.isNotEmpty()) {
                tokenStorage.saveRefreshToken(decodedValue)
            }
            
            delegate.addCookie(requestUrl, cookie.copy(value = decodedValue))
        } else {
            delegate.addCookie(requestUrl, cookie)
        }
    }

    override suspend fun get(requestUrl: Url): List<Cookie> {
        val persistedRefresh = tokenStorage.getRefreshToken()
        val existingCookies = delegate.get(requestUrl)
        val hasRefreshCookie = existingCookies.any { it.name == "refresh_token" }
        
        if (!hasRefreshCookie && !persistedRefresh.isNullOrEmpty()) {
            val restoredCookie = Cookie(
                name = "refresh_token",
                value = persistedRefresh,
                path = "/",
                secure = true,
                httpOnly = true
            )
            delegate.addCookie(requestUrl, restoredCookie)
        } else if (hasRefreshCookie && !persistedRefresh.isNullOrEmpty()) {
            val existingRefreshCookie = existingCookies.firstOrNull { it.name == "refresh_token" }
            if (existingRefreshCookie != null) {
                val existingValue = try {
                    existingRefreshCookie.value.decodeURLPart()
                } catch (e: Throwable) {
                    existingRefreshCookie.value
                }
                if (existingValue != persistedRefresh) {
                    val updatedCookie = Cookie(
                        name = "refresh_token",
                        value = persistedRefresh,
                        path = "/",
                        secure = true,
                        httpOnly = true
                    )
                    delegate.addCookie(requestUrl, updatedCookie)
                }
            }
        }

        val decodedCookies = existingCookies.map { cookie ->
            if (cookie.name == "refresh_token") {
                val decodedValue = try {
                    cookie.value.decodeURLPart()
                } catch (e: Throwable) {
                    cookie.value
                }
                cookie.copy(value = decodedValue)
            } else {
                cookie
            }
        }
        
        return decodedCookies
    }

    override fun close() {
        delegate.close()
    }
}
