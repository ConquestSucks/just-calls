package com.justcalls.ui.screens.room.connection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import com.justcalls.data.network.RoomService
import com.justcalls.livekit.LiveKitManager
import kotlinx.coroutines.launch

@Composable
fun RoomConnectionHandler(
    roomName: String,
    roomService: RoomService?,
    liveKitManager: LiveKitManager,
    onConnectionError: (String) -> Unit
) {

    LaunchedEffect(roomName, roomService) {
        val service = roomService
        if (service == null) {
            println("[RoomConnectionHandler] RoomService is null, пропускаем запрос токена")
            return@LaunchedEffect
        }

        try {
            println("[RoomConnectionHandler] Запрашиваем токен для комнаты roomKey=$roomName")
            val result = service.getRoomToken(roomName)
            
            result.fold(
                onSuccess = { apiResult ->
                    if (apiResult.success && apiResult.data != null) {
                        val data = apiResult.data
                        println("[RoomConnectionHandler] getRoomToken success: token=${data.token.take(20)}..., userIdentity=${data.userIdentity}")
                        
                        try {
                            liveKitManager.connect(data, "wss://wss.ghjc.ru")
                            println("[RoomConnectionHandler] LiveKit подключение инициировано")
                        } catch (e: Exception) {
                            e.printStackTrace()
                            val errorMsg = "Ошибка подключения к видеозвонку: ${e.message}"
                            onConnectionError(errorMsg)
                            println("[RoomConnectionHandler] LiveKit подключение ошибка: ${e.message}")
                        }
                    } else {
                        val errorCode = apiResult.error?.code ?: "UNKNOWN"
                        val errorMsg = apiResult.error?.message ?: "unknown error"
                        println("[RoomConnectionHandler] getRoomToken error - code: $errorCode, message: $errorMsg")
                        
                        val fullErrorMsg = if (errorCode.contains("ACCESS_DENIED", ignoreCase = true) || 
                                               errorMsg.contains("access denied", ignoreCase = true)) {
                            "Доступ запрещен. Возможно, вы уже подключены к другой комнате. Попробуйте выйти и войти снова."
                        } else {
                            "Не удалось получить токен: $errorMsg"
                        }

                        onConnectionError(fullErrorMsg)
                    }
                },
                onFailure = { exception ->
                    exception.printStackTrace()
                    val errorMsg = exception.message ?: "unknown error"
                    println("[RoomConnectionHandler] getRoomToken exception: $errorMsg")
                    
                    val fullErrorMsg = if (errorMsg.contains("access denied", ignoreCase = true)) {
                        "Доступ запрещен. Возможно, вы уже подключены к другой комнате. Попробуйте выйти и войти снова."
                    } else {
                        "Ошибка при получении токена: $errorMsg"
                    }

                    onConnectionError(fullErrorMsg)
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            val errorMsg = "Ошибка при получении токена: ${e.message}"
            onConnectionError(errorMsg)
            println("[RoomConnectionHandler] getRoomToken exception: ${e.message}")
        }
    }
    
    DisposableEffect(roomName) {
        onDispose {
            println("[RoomConnectionHandler] Отключение от LiveKit")
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                liveKitManager.disconnect()
            }
        }
    }
}

