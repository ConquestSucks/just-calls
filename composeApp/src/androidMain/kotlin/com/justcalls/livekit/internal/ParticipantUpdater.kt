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
        val local = room.localParticipant
        
        val identityStr = local.identity?.toString() ?: "local"
        val nameStr = local.name ?: identityStr
        
        println("[ParticipantUpdater] Локальный участник: identity=$identityStr, name=$nameStr")
        
        // Пытаемся получить состояние камеры и микрофона через методы LocalParticipant
        val isCameraEnabled = try {
            val isCameraEnabledMethod = local.javaClass.getMethod("isCameraEnabled")
            val cameraEnabled = isCameraEnabledMethod.invoke(local) as? Boolean ?: false
            println("[ParticipantUpdater] isCameraEnabled() = $cameraEnabled")
            cameraEnabled
        } catch (e: Exception) {
            println("[ParticipantUpdater] Не удалось вызвать isCameraEnabled(): ${e.message}")
            // Fallback на проверку публикаций
            val (cameraEnabled, _) = getTrackStates(local.videoTrackPublications, local.audioTrackPublications)
            cameraEnabled
        }
        
        val isMicrophoneEnabled = try {
            val isMicrophoneEnabledMethod = local.javaClass.getMethod("isMicrophoneEnabled")
            val micEnabled = isMicrophoneEnabledMethod.invoke(local) as? Boolean ?: false
            println("[ParticipantUpdater] isMicrophoneEnabled() = $micEnabled")
            micEnabled
        } catch (e: Exception) {
            println("[ParticipantUpdater] Не удалось вызвать isMicrophoneEnabled(): ${e.message}")
            // Fallback на проверку публикаций
            val (_, micEnabled) = getTrackStates(local.videoTrackPublications, local.audioTrackPublications)
            micEnabled
        }
        
        println("[ParticipantUpdater] Локальный участник: камера=$isCameraEnabled, микрофон=$isMicrophoneEnabled")
        
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
            val isCameraEnabled = when (videoPubs) {
                is Collection<*> -> {
                    val hasPubs = videoPubs.isNotEmpty()
                    println("[ParticipantUpdater] Видео публикации (Collection): размер=${videoPubs.size}, isNotEmpty=$hasPubs")
                    if (hasPubs) {
                        // Проверяем, что трек действительно опубликован
                        val firstPub = videoPubs.firstOrNull()
                        val isPublished = try {
                            if (firstPub != null) {
                                // Если это Pair, извлекаем публикацию
                                val publication = when {
                                    firstPub is Pair<*, *> -> {
                                        println("[ParticipantUpdater] Первый элемент - Pair, извлекаем публикацию")
                                        firstPub.second ?: firstPub.first
                                    }

                                    else -> firstPub
                                }

                                // Пытаемся проверить состояние публикации
                                try {
                                    val isMutedMethod = publication?.javaClass?.getMethod("isMuted")
                                    val muted = isMutedMethod?.invoke(publication) as? Boolean ?: false
                                    println("[ParticipantUpdater] Публикация muted: $muted")
                                    !muted
                                } catch (e: Exception) {
                                    println("[ParticipantUpdater] Не удалось вызвать isMuted(): ${e.message}, считаем что включено")
                                    true // Если не можем проверить, считаем что включено
                                }
                            } else {
                                false
                            }
                        } catch (e: Exception) {
                            println("[ParticipantUpdater] Ошибка при проверке состояния публикации: ${e.message}")
                            true // Если не можем проверить, считаем что включено
                        }
                        println("[ParticipantUpdater] Видео трек опубликован: $isPublished")
                        isPublished
                    } else {
                        false
                    }
                }

                is Map<*, *> -> {
                    val hasPubs = videoPubs.isNotEmpty()
                    println("[ParticipantUpdater] Видео публикации (Map): размер=${videoPubs.size}, isNotEmpty=$hasPubs")
                    hasPubs
                }

                else -> {
                    println("[ParticipantUpdater] Видео публикации: неизвестный тип ${videoPubs.javaClass.name}")
                    false
                }
            }
            
            val isMicrophoneEnabled = when (audioPubs) {
                is Collection<*> -> audioPubs.isNotEmpty()
                is Map<*, *> -> audioPubs.isNotEmpty()
                else -> false
            }
            
            println("[ParticipantUpdater] Состояние треков: камера=$isCameraEnabled, микрофон=$isMicrophoneEnabled")
            Pair(isCameraEnabled, isMicrophoneEnabled)
        } catch (e: Exception) {
            println("[ParticipantUpdater] Ошибка при проверке треков: ${e.message}")
            e.printStackTrace()
            Pair(false, false)
        }
    }
}

