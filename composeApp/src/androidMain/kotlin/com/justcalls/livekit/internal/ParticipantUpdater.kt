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
            // Ignore
        }
        
        return participants
    }
    
    private fun addLocalParticipant(
        room: Room,
        participants: MutableList<LiveKitParticipant>
    ) {
        val local = room.localParticipant
        
        val identityStr = local.identity?.toString() ?: "local"
        val nameStr = local.name ?: identityStr
        
        val isCameraEnabled = try {
            val isCameraEnabledMethod = local.javaClass.getMethod("isCameraEnabled")
            val cameraEnabled = isCameraEnabledMethod.invoke(local) as? Boolean ?: false
            cameraEnabled
        } catch (e: Exception) {
            val (cameraEnabled, _) = getTrackStates(local.videoTrackPublications, local.audioTrackPublications)
            cameraEnabled
        }
        
        val isMicrophoneEnabled = try {
            val isMicrophoneEnabledMethod = local.javaClass.getMethod("isMicrophoneEnabled")
            val micEnabled = isMicrophoneEnabledMethod.invoke(local) as? Boolean ?: false
            micEnabled
        } catch (e: Exception) {
            val (_, micEnabled) = getTrackStates(local.videoTrackPublications, local.audioTrackPublications)
            micEnabled
        }
        
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
            // Ignore
        }
    }
    
    private fun getTrackStates(
        videoPubs: Any,
        audioPubs: Any
    ): Pair<Boolean, Boolean> {
        return try {
            val isCameraEnabled = when (videoPubs) {
                is Collection<*> -> {
                    val hasPubs = videoPubs.isNotEmpty()
                    if (hasPubs) {
                        val firstPub = videoPubs.firstOrNull()
                        val isPublished = try {
                            if (firstPub != null) {
                                val publication = when {
                                    firstPub is Pair<*, *> -> firstPub.second ?: firstPub.first
                                    else -> firstPub
                                }

                                try {
                                    val isMutedMethod = publication?.javaClass?.getMethod("isMuted")
                                    val muted = isMutedMethod?.invoke(publication) as? Boolean ?: false
                                    !muted
                                } catch (e: Exception) {
                                    true
                                }
                            } else {
                                false
                            }
                        } catch (e: Exception) {
                            true
                        }
                        isPublished
                    } else {
                        false
                    }
                }
                is Map<*, *> -> videoPubs.isNotEmpty()
                else -> false
            }
            
            val isMicrophoneEnabled = when (audioPubs) {
                is Collection<*> -> audioPubs.isNotEmpty()
                is Map<*, *> -> audioPubs.isNotEmpty()
                else -> false
            }
            
            Pair(isCameraEnabled, isMicrophoneEnabled)
        } catch (e: Exception) {
            Pair(false, false)
        }
    }
}
