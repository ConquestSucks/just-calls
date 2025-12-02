package com.justcalls.livekit

import com.justcalls.data.models.responses.RoomTokenResult

expect class LiveKitManager() {
    suspend fun connect(tokenResult: RoomTokenResult, serverUrl: String)
    
    suspend fun disconnect()
    
    fun setMicrophoneEnabled(enabled: Boolean)
    
    fun setCameraEnabled(enabled: Boolean)
    
    fun getParticipants(): List<LiveKitParticipant>
    
    fun observeParticipants(callback: (List<LiveKitParticipant>) -> Unit)
    
    fun getVideoSurface(participantId: String): Any?
    
    fun getEglBaseContext(): Any?
}

data class LiveKitParticipant(
    val identity: String,
    val name: String,
    val isLocal: Boolean = false,
    val isCameraEnabled: Boolean = false,
    val isMicrophoneEnabled: Boolean = false
)

