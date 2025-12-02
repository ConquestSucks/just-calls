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
    private var restoredFromTokenStorage: Boolean = false

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
        println(
            "[CookiesStorage] addCookie: url=$requestUrl " +
                "name=${cookie.name} value=${cookie.value} " +
                "domain=${cookie.domain} path=${cookie.path} " +
                "secure=${cookie.secure} httpOnly=${cookie.httpOnly} expires=${cookie.expires}"
        )

        if (cookie.name == "refresh_token") {
            val decodedValue = try {
                cookie.value.decodeURLPart()
            } catch (e: Throwable) {
                cookie.value
            }
            
            if (decodedValue.isNotEmpty()) {
                println("[CookiesStorage] save refresh_token into TokenStorage (decoded: ${decodedValue.take(20)}...)")
                tokenStorage.saveRefreshToken(decodedValue)
            }
            
            delegate.addCookie(requestUrl, cookie.copy(value = decodedValue))
        } else {
            delegate.addCookie(requestUrl, cookie)
        }
    }

    override suspend fun get(requestUrl: Url): List<Cookie> {
        if (!restoredFromTokenStorage) {
            restoredFromTokenStorage = true
            val persistedRefresh = tokenStorage.getRefreshToken()
            if (!persistedRefresh.isNullOrEmpty()) {
                println("[CookiesStorage] restore refresh_token from TokenStorage for url=$requestUrl (decoded value: ${persistedRefresh.take(20)}...)")
                val restoredCookie = Cookie(
                    name = "refresh_token",
                    value = persistedRefresh,
                    path = "/",
                    secure = true,
                    httpOnly = true
                )
                delegate.addCookie(requestUrl, restoredCookie)
            } else {
                println("[CookiesStorage] no persisted refresh_token in TokenStorage")
            }
        }

        val cookies = delegate.get(requestUrl)
        println("[CookiesStorage] get: url=$requestUrl count=${cookies.size}")
        
        val decodedCookies = cookies.map { cookie ->
            if (cookie.name == "refresh_token") {
                val decodedValue = try {
                    cookie.value.decodeURLPart()
                } catch (e: Throwable) {
                    cookie.value
                }
                println(
                    "[CookiesStorage] -> cookie name=${cookie.name} " +
                        "originalValue=${cookie.value.take(30)}... decodedValue=${decodedValue.take(30)}... " +
                        "domain=${cookie.domain} path=${cookie.path}"
                )
                cookie.copy(value = decodedValue)
            } else {
                cookie
            }
        }
        
        decodedCookies.forEach { cookie ->
            println(
                "[CookiesStorage] -> cookie name=${cookie.name} value=${cookie.value.take(30)}... " +
                    "domain=${cookie.domain} path=${cookie.path} " +
                    "secure=${cookie.secure} httpOnly=${cookie.httpOnly} expires=${cookie.expires}"
            )
        }
        return decodedCookies
    }

    override fun close() {
        delegate.close()
    }
}

