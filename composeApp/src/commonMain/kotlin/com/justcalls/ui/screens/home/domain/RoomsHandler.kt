package com.justcalls.ui.screens.home.domain

import com.justcalls.data.network.RoomService
import com.justcalls.utils.NetworkErrorHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RoomsHandler(
    private val roomService: RoomService,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    fun loadRooms(
        onSuccess: (List<com.justcalls.data.models.responses.RoomDto>) -> Unit,
        onError: (String) -> Unit,
        onTimeout: (String) -> Unit
    ) {
        coroutineScope.launch {
            try {
                val result = roomService.getRooms()
                val apiResult = result.getOrNull()
                if (apiResult != null && apiResult.success && apiResult.data != null) {
                    onSuccess(apiResult.data)
                } else {
                    val message = apiResult?.error?.message ?: "Не удалось загрузить комнаты"
                    if (apiResult?.error?.code == "UNAUTHORIZED" || apiResult?.error?.code?.equals("unauthorized", ignoreCase = true) == true) {
                        onError("Ошибка авторизации. Пожалуйста, войдите снова.")
                    } else {
                        onError(message)
                    }
                }
            } catch (e: Exception) {
                val message = e.message ?: ""
                val isTimeout = message.contains("timeout", ignoreCase = true) || 
                               message.contains("Timed out", ignoreCase = true) ||
                               e::class.simpleName?.contains("Timeout", ignoreCase = true) == true
                
                if (isTimeout) {
                    onTimeout("Превышено время ожидания загрузки комнат")
                } else if (message.contains("401") || message.contains("Unauthorized")) {
                    onError("Ошибка авторизации. Пожалуйста, войдите снова.")
                } else {
                    onError(NetworkErrorHandler.getErrorMessage(e))
                }
            }
        }
    }
    
    fun createRoom(
        roomTitle: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        coroutineScope.launch {
            try {
                val result = roomService.createRoom(roomTitle)
                val apiResult = result.getOrNull()
                
                if (apiResult != null && apiResult.success && apiResult.data != null) {
                    onSuccess(apiResult.data)
                } else {
                    val message = apiResult?.error?.message ?: "Не удалось создать комнату"
                    onError(message)
                }
            } catch (e: Exception) {
                onError(NetworkErrorHandler.getErrorMessage(e))
            }
        }
    }
}

