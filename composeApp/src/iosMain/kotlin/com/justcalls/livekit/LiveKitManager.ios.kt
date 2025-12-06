package com.justcalls.livekit

import com.justcalls.data.models.responses.RoomTokenResult

actual class LiveKitManager {
    actual suspend fun connect(tokenResult: RoomTokenResult, serverUrl: String) {
        // Not implemented for iOS yet
    }
    
    actual suspend fun disconnect() {
        // Not implemented for iOS yet
    }
    
    actual fun setMicrophoneEnabled(enabled: Boolean) {
        // Not implemented for iOS yet
    }
    
    actual fun setCameraEnabled(enabled: Boolean) {
        // Not implemented for iOS yet
    }
    
    actual fun observeParticipants(callback: (List<LiveKitParticipant>) -> Unit) {
        callback(emptyList())
    }
    
    actual fun getVideoSurface(participantId: String): Any? {
        return null
    }
}

