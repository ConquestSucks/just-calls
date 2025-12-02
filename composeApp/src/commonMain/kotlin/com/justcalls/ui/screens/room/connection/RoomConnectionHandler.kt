package com.justcalls.ui.screens.room.connection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.justcalls.data.models.responses.RoomTokenResult
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
    var connectionError by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(roomName, roomService) {
        val service = roomService
        if (service == null) {
            println("[RoomConnectionHandler] RoomService is null, пропускаем запрос токена")
            return@LaunchedEffect
        }

        try {
            println("[RoomConnectionHandler] Запрашиваем токен для комнаты roomKey=$roomName")
            val result = service.getRoomToken(roomName)
            val apiResult = result.getOrNull()
            if (apiResult != null && apiResult.success && apiResult.data != null) {
                val data = apiResult.data
                println("[RoomConnectionHandler] getRoomToken success: token=${data.token.take(20)}..., userIdentity=${data.userIdentity}")
                
                try {
                    liveKitManager.connect(data, "wss://wss.ghjc.ru")
                    println("[RoomConnectionHandler] LiveKit подключение инициировано")
                } catch (e: Exception) {
                    e.printStackTrace()
                    val errorMsg = "Ошибка подключения к видеозвонку: ${e.message}"
                    connectionError = errorMsg
                    onConnectionError(errorMsg)
                    println("[RoomConnectionHandler] LiveKit подключение ошибка: ${e.message}")
                }
            } else {
                val errorMsg = apiResult?.error?.message ?: "unknown error"
                val fullErrorMsg = "Не удалось получить токен: $errorMsg"
                connectionError = fullErrorMsg
                onConnectionError(fullErrorMsg)
                println("[RoomConnectionHandler] getRoomToken error: $errorMsg")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val errorMsg = "Ошибка при получении токена: ${e.message}"
            connectionError = errorMsg
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

