package com.justcalls.ui.screens.home.domain

import com.justcalls.data.network.RoomService
import com.justcalls.utils.NetworkErrorHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

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
                val result = withTimeout(5000L) {
                    roomService.getRooms()
                }
                val apiResult = result.getOrNull()
                if (apiResult != null && apiResult.success && apiResult.data != null) {
                    onSuccess(apiResult.data)
                } else {
                    val message = apiResult?.error?.message ?: "Не удалось загрузить комнаты"
                    onError(message)
                }
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                onTimeout("Превышено время ожидания загрузки комнат")
            } catch (e: Exception) {
                onError(NetworkErrorHandler.getErrorMessage(e))
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

