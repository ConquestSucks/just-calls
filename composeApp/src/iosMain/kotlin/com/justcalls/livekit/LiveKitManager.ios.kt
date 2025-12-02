package com.justcalls.livekit

import com.justcalls.data.models.responses.RoomTokenResult

actual class LiveKitManager {
    actual suspend fun connect(tokenResult: RoomTokenResult, serverUrl: String) {
        println("[LiveKitManager iOS] connect - пока не реализовано для iOS")
    }
    
    actual suspend fun disconnect() {
        println("[LiveKitManager iOS] disconnect - пока не реализовано для iOS")
    }
    
    actual fun setMicrophoneEnabled(enabled: Boolean) {
        println("[LiveKitManager iOS] setMicrophoneEnabled - пока не реализовано для iOS")
    }
    
    actual fun setCameraEnabled(enabled: Boolean) {
        println("[LiveKitManager iOS] setCameraEnabled - пока не реализовано для iOS")
    }
    
    actual fun getParticipants(): List<LiveKitParticipant> {
        return emptyList()
    }
    
    actual fun observeParticipants(callback: (List<LiveKitParticipant>) -> Unit) {
        callback(emptyList())
    }
    
    actual fun getVideoSurface(participantId: String): Any? {
        return null
    }
    
    actual fun getEglBaseContext(): Any? {
        return null // EGL контекст не используется на iOS
    }
}

