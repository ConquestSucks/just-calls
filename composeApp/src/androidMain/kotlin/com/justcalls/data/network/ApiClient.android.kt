package com.justcalls.data.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.ConnectionPool
import java.util.concurrent.TimeUnit

actual fun createHttpClientEngine(): HttpClientEngine {
    return OkHttp.create {
        config {
            connectionPool(ConnectionPool(10, 5, TimeUnit.MINUTES))
            retryOnConnectionFailure(true)
        }
    }
}

