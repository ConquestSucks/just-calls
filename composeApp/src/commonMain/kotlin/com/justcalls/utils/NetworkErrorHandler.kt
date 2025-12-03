package com.justcalls.utils

import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException

object NetworkErrorHandler {
    fun getErrorMessage(exception: Throwable): String {
        return when (exception) {
            is ConnectTimeoutException -> {
                "Превышено время ожидания подключения к серверу. Проверьте интернет-соединение."
            }
            is SocketTimeoutException -> {
                "Превышено время ожидания ответа от сервера. Попробуйте позже."
            }
            else -> {
                val message = exception.message ?: "Неизвестная ошибка"
                val exceptionName = exception::class.simpleName ?: ""
                if (message.contains("timeout", ignoreCase = true) || exceptionName.contains("Timeout", ignoreCase = true)) {
                    "Превышено время ожидания. Проверьте интернет-соединение."
                } else if (message.contains("connect", ignoreCase = true) || exceptionName.contains("Connect", ignoreCase = true)) {
                    "Не удалось подключиться к серверу. Проверьте интернет-соединение."
                } else if (message.contains("host", ignoreCase = true) || exceptionName.contains("Host", ignoreCase = true)) {
                    "Не удалось найти сервер. Проверьте интернет-соединение."
                } else {
                    "Ошибка подключения к серверу: $message"
                }
            }
        }
    }
}

