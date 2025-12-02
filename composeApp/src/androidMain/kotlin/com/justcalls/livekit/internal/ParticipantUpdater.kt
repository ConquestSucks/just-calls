package com.justcalls.livekit.internal

import com.justcalls.livekit.LiveKitParticipant
import io.livekit.android.room.Room

internal object ParticipantUpdater {
    
    fun updateParticipants(room: Room): List<LiveKitParticipant> {
        val participants = mutableListOf<LiveKitParticipant>()
        
        try {
            addLocalParticipant(room, participants)
            
            addRemoteParticipants(room, participants)
        } catch (e: Exception) {
            println("[ParticipantUpdater] Ошибка при обновлении участников: ${e.message}")
            e.printStackTrace()
        }
        
        return participants
    }
    
    private fun addLocalParticipant(
        room: Room,
        participants: MutableList<LiveKitParticipant>
    ) {
        val local = room.localParticipant ?: return
        
        val identityStr = local.identity?.toString() ?: "local"
        val nameStr = local.name ?: identityStr
        
        val (isCameraEnabled, isMicrophoneEnabled) = getTrackStates(local.videoTrackPublications, local.audioTrackPublications)
        
        participants.add(
            LiveKitParticipant(
                identity = identityStr,
                name = nameStr,
                isLocal = true,
                isCameraEnabled = isCameraEnabled,
                isMicrophoneEnabled = isMicrophoneEnabled
            )
        )
    }
    
    private fun addRemoteParticipants(
        room: Room,
        participants: MutableList<LiveKitParticipant>
    ) {
        try {
            val remoteParticipants = room.remoteParticipants
            for ((_, remote) in remoteParticipants) {
                val identityStr = remote.identity?.toString() ?: "unknown"
                val nameStr = remote.name ?: identityStr
                
                val (isCameraEnabled, isMicrophoneEnabled) = getTrackStates(
                    remote.videoTrackPublications,
                    remote.audioTrackPublications
                )
                
                participants.add(
                    LiveKitParticipant(
                        identity = identityStr,
                        name = nameStr,
                        isLocal = false,
                        isCameraEnabled = isCameraEnabled,
                        isMicrophoneEnabled = isMicrophoneEnabled
                    )
                )
            }
        } catch (e: Exception) {
            println("[ParticipantUpdater] Ошибка при получении удалённых участников: ${e.message}")
        }
    }
    
    private fun getTrackStates(
        videoPubs: Any,
        audioPubs: Any
    ): Pair<Boolean, Boolean> {
        return try {
            val isCameraEnabled = when {
                videoPubs is Collection<*> -> videoPubs.isNotEmpty()
                videoPubs is Map<*, *> -> videoPubs.isNotEmpty()
                else -> false
            }
            
            val isMicrophoneEnabled = when {
                audioPubs is Collection<*> -> audioPubs.isNotEmpty()
                audioPubs is Map<*, *> -> audioPubs.isNotEmpty()
                else -> false
            }
            
            Pair(isCameraEnabled, isMicrophoneEnabled)
        } catch (e: Exception) {
            println("[ParticipantUpdater] Ошибка при проверке треков: ${e.message}")
            Pair(false, false)
        }
    }
}

