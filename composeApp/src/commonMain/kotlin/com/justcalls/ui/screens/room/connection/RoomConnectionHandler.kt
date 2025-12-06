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
            return@LaunchedEffect
        }

        try {
            val result = service.getRoomToken(roomName)
            
            result.fold(
                onSuccess = { apiResult ->
                    if (apiResult.success && apiResult.data != null) {
                        val data = apiResult.data
                        
                        try {
                            liveKitManager.connect(data, "wss://wss.ghjc.ru")
                        } catch (e: Exception) {
                            val errorMsg = "Ошибка подключения к видеозвонку: ${e.message}"
                            onConnectionError(errorMsg)
                        }
                    } else {
                        val errorCode = apiResult.error?.code ?: "UNKNOWN"
                        val errorMsg = apiResult.error?.message ?: "unknown error"
                        
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
                    val errorMsg = exception.message ?: "unknown error"
                    
                    val fullErrorMsg = if (errorMsg.contains("access denied", ignoreCase = true)) {
                        "Доступ запрещен. Возможно, вы уже подключены к другой комнате. Попробуйте выйти и войти снова."
                    } else {
                        "Ошибка при получении токена: $errorMsg"
                    }

                    onConnectionError(fullErrorMsg)
                }
            )
        } catch (e: Exception) {
            val errorMsg = "Ошибка при получении токена: ${e.message}"
            onConnectionError(errorMsg)
        }
    }
    
    DisposableEffect(roomName) {
        onDispose {
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                liveKitManager.disconnect()
            }
        }
    }
}

