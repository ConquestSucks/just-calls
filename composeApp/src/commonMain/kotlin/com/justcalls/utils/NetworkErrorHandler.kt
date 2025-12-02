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
            is java.net.SocketTimeoutException -> {
                "Превышено время ожидания подключения. Проверьте интернет-соединение."
            }
            is java.net.UnknownHostException -> {
                "Не удалось найти сервер. Проверьте интернет-соединение."
            }
            is java.net.ConnectException -> {
                "Не удалось подключиться к серверу. Проверьте интернет-соединение."
            }
            else -> {
                val message = exception.message ?: "Неизвестная ошибка"
                if (message.contains("timeout", ignoreCase = true)) {
                    "Превышено время ожидания. Проверьте интернет-соединение."
                } else if (message.contains("connect", ignoreCase = true)) {
                    "Не удалось подключиться к серверу. Проверьте интернет-соединение."
                } else {
                    "Ошибка подключения к серверу: $message"
                }
            }
        }
    }
}

