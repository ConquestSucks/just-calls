package com.justcalls.utils

import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import kotlinx.serialization.SerializationException

object NetworkErrorHandler {
    fun getErrorMessage(exception: Throwable): String {
        return when (exception) {
            is ConnectTimeoutException -> {
                "Превышено время ожидания подключения к серверу. Проверьте интернет-соединение."
            }
            is SocketTimeoutException -> {
                "Превышено время ожидания ответа от сервера. Попробуйте позже."
            }
            is SerializationException -> {
                val message = exception.message ?: ""
                if (message.contains("JSON", ignoreCase = true) || 
                    message.contains("parse", ignoreCase = true) || 
                    message.contains("token", ignoreCase = true) ||
                    message.contains("Unexpected", ignoreCase = true) ||
                    message.contains("Expected", ignoreCase = true) ||
                    message.contains("Ilegal", ignoreCase = true)) {
                    "Ошибка обработки ответа сервера из-за нестабильного интернет-соединения. " +
                    "Запрос может быть выполнен (проверьте почту). Попробуйте еще раз."
                } else {
                    "Ошибка обработки данных от сервера. Проверьте интернет-соединение и попробуйте еще раз."
                }
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
                } else if (message.contains("JSON", ignoreCase = true) || 
                           message.contains("parse", ignoreCase = true) || 
                           message.contains("token", ignoreCase = true) ||
                           message.contains("Unexpected", ignoreCase = true) ||
                           message.contains("Expected", ignoreCase = true) ||
                           message.contains("Ilegal", ignoreCase = true)) {
                    "Ошибка обработки ответа сервера из-за нестабильного интернет-соединения. " +
                    "Запрос может быть выполнен (проверьте почту). Попробуйте еще раз."
                } else {
                    "Ошибка подключения к серверу: $message"
                }
            }
        }
    }
}

