package com.justcalls.data.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import platform.Foundation.NSURLRequestUseProtocolCachePolicy

actual fun createHttpClientEngine(): HttpClientEngine {
    return Darwin.create {
        configureRequest {
            setAllowsCellularAccess(true)
            setCachePolicy(NSURLRequestUseProtocolCachePolicy)
            setTimeoutInterval(60.0)
        }
    }
}
