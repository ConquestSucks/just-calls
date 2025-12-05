package com.justcalls.livekit

import com.justcalls.JustCallsApplication
import com.justcalls.data.models.responses.RoomTokenResult
import com.justcalls.livekit.internal.EglContextManager
import com.justcalls.livekit.internal.ParticipantUpdater
import com.justcalls.livekit.internal.VideoTrackExtractor
import io.livekit.android.LiveKit
import io.livekit.android.room.Room
import io.livekit.android.room.track.VideoTrack
import io.livekit.android.room.track.LocalVideoTrack
import io.livekit.android.util.LoggingLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

actual class LiveKitManager {
    private var room: Room? = null
    
    private val _participants = MutableStateFlow<List<LiveKitParticipant>>(emptyList())
    private var participantsCallback: ((List<LiveKitParticipant>) -> Unit)? = null
    
    private val videoTracks = mutableMapOf<String, VideoTrack>()
    private val localVideoTracks = mutableMapOf<String, LocalVideoTrack>()
    
    private val eglContextManager = EglContextManager()
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    init {
        LiveKit.loggingLevel = LoggingLevel.DEBUG
        
        scope.launch {
            _participants.asStateFlow().collect { participants: List<LiveKitParticipant> ->
                participantsCallback?.invoke(participants)
            }
        }
    }
    
    actual suspend fun connect(tokenResult: RoomTokenResult, serverUrl: String) {
        try {
            println("[LiveKitManager] Подключение к комнате: serverUrl=$serverUrl, userIdentity=${tokenResult.userIdentity}")
            
            val appContext = JustCallsApplication.instance
            
            eglContextManager.initialize()
            
            val newRoom = LiveKit.create(appContext)
            
            newRoom.connect(serverUrl, tokenResult.token)
            
            this.room = newRoom
            
            scope.launch {
                while (room != null) {
                    updateParticipants()
                    kotlinx.coroutines.delay(1000)
                }
            }
            
            println("[LiveKitManager] Подключение успешно")
            updateParticipants()
            
        } catch (e: Exception) {
            println("[LiveKitManager] Ошибка подключения: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
    
    actual suspend fun disconnect() {
        try {
            println("[LiveKitManager] Отключение от комнаты")
            
            videoTracks.clear()
            localVideoTracks.clear()
            
            room?.disconnect()
            room = null
            
            eglContextManager.release()
            
            _participants.value = emptyList()
            println("[LiveKitManager] Отключение завершено")
        } catch (e: Exception) {
            println("[LiveKitManager] Ошибка при отключении: ${e.message}")
            e.printStackTrace()
        }
    }
    
    actual fun setMicrophoneEnabled(enabled: Boolean) {
        try {
            println("[LiveKitManager] setMicrophoneEnabled: $enabled")
            scope.launch {
                try {
                    room?.localParticipant?.setMicrophoneEnabled(enabled)
                    updateParticipants()
                } catch (e: Exception) {
                    println("[LiveKitManager] Ошибка setMicrophoneEnabled: ${e.message}")
                }
            }
        } catch (e: Exception) {
            println("[LiveKitManager] Ошибка при изменении состояния микрофона: ${e.message}")
            e.printStackTrace()
        }
    }
    
    actual fun setCameraEnabled(enabled: Boolean) {
        try {
            println("[LiveKitManager] setCameraEnabled: $enabled")
            scope.launch {
                try {
                    room?.localParticipant?.setCameraEnabled(enabled)
                    
                    if (!enabled) {
                        val localIdentity = room?.localParticipant?.identity?.toString()
                        localIdentity?.let { localVideoTracks.remove(it) }
                        updateParticipants()
                    } else {
                        // После включения камеры ждём, пока трек станет доступен
                        val localIdentity = room?.localParticipant?.identity?.toString()
                        var attempts = 0
                        while (attempts < 15) {
                            kotlinx.coroutines.delay(200)
                            updateParticipants()
                            
                            // Пытаемся получить трек
                            val localParticipant = room?.localParticipant
                            val localVideoTrack = localParticipant?.videoTrackPublications?.let { pubs ->
                                VideoTrackExtractor.extractLocalVideoTrack(pubs, localIdentity ?: "")
                            }
                            
                            if (localVideoTrack != null && localIdentity != null) {
                                localVideoTracks[localIdentity] = localVideoTrack
                                println("[LiveKitManager] Локальный видео трек получен после включения камеры: $localIdentity, track=$localVideoTrack")
                                updateParticipants()
                                break
                            }
                            attempts++
                        }
                        
                        if (attempts >= 15) {
                            println("[LiveKitManager] Не удалось получить локальный видео трек после включения камеры")
                            updateParticipants()
                        }
                    }
                } catch (e: Exception) {
                    println("[LiveKitManager] Ошибка setCameraEnabled: ${e.message}")
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            println("[LiveKitManager] Ошибка при изменении состояния камеры: ${e.message}")
            e.printStackTrace()
        }
    }
    
    actual fun getParticipants(): List<LiveKitParticipant> {
        return _participants.value
    }
    
    actual fun observeParticipants(callback: (List<LiveKitParticipant>) -> Unit) {
        participantsCallback = callback
        callback(_participants.value)
    }
    
    actual fun getVideoSurface(participantId: String): Any? {
        val currentRoom = room ?: return null
        
        try {
            val localIdentity = currentRoom.localParticipant.identity?.toString()
            if (participantId == localIdentity) {
                // Сначала проверяем сохранённый трек
                localVideoTracks[participantId]?.let {
                    println("[LiveKitManager] Используем сохранённый локальный видео трек для участника: $participantId")
                    return "local:$participantId"
                }
                
                // Если трек не сохранён, пытаемся получить его из публикаций
                val localParticipant = currentRoom.localParticipant
                println("[LiveKitManager] Локальный участник: получение видео трека")
                println("[LiveKitManager] Количество публикаций: ${localParticipant.videoTrackPublications.size}")
                
                val localVideoTrack = localParticipant.videoTrackPublications.let { pubs ->
                    VideoTrackExtractor.extractLocalVideoTrack(pubs, participantId)
                }
                
                if (localVideoTrack != null) {
                    localVideoTracks[participantId] = localVideoTrack
                    println("[LiveKitManager] Сохранён локальный видео трек для участника: $participantId, track=$localVideoTrack")
                    return "local:$participantId"
                } else {
                    println("[LiveKitManager] Локальный видео трек не найден для $participantId, публикаций: ${localParticipant.videoTrackPublications.size}")
                    // Если трек не найден, но камера включена, возвращаем поверхность для повторных попыток
                    val isCameraEnabled = localParticipant.videoTrackPublications.let { pubs ->
                        (pubs as Collection<*>).isNotEmpty()
                    }
                    
                    if (isCameraEnabled) {
                        println("[LiveKitManager] Камера включена, но трек ещё не доступен, возвращаем поверхность для повторных попыток")
                        return "local:$participantId"
                    }
                }
            } else {
                val remoteParticipant = currentRoom.remoteParticipants.values.find { participant -> 
                    participant.identity?.toString() == participantId 
                }
                
                if (remoteParticipant != null) {
                    println("[LiveKitManager] Удалённый участник $participantId: получение видео трека")
                    println("[LiveKitManager] Количество публикаций: ${remoteParticipant.videoTrackPublications.size}")
                    
                    val videoTrack = VideoTrackExtractor.extractRemoteVideoTrack(
                        remoteParticipant.videoTrackPublications,
                        participantId
                    )
                    
                    if (videoTrack != null) {
                        videoTracks[participantId] = videoTrack
                        println("[LiveKitManager] Сохранён видео трек для удалённого участника: $participantId")
                        return "remote:$participantId"
                    } else {
                        println("[LiveKitManager] Видео трек не найден для удалённого участника $participantId")
                    }
                } else {
                    println("[LiveKitManager] Удалённый участник не найден: $participantId")
                }
            }
        } catch (e: Exception) {
            println("[LiveKitManager] Ошибка при создании видео поверхности для $participantId: ${e.message}")
            e.printStackTrace()
        }
        
        return null
    }
    
    fun getVideoTrack(participantId: String): VideoTrack? {
        return videoTracks[participantId]
    }
    
    fun getLocalVideoTrack(participantId: String): LocalVideoTrack? {
        // Сначала проверяем сохранённый трек
        localVideoTracks[participantId]?.let {
            println("[LiveKitManager] getLocalVideoTrack: возвращаем сохранённый трек для $participantId")
            return it
        }
        
        // Если трек не сохранён, пытаемся получить его из публикаций
        val localParticipant = room?.localParticipant
        if (localParticipant != null) {
            println("[LiveKitManager] getLocalVideoTrack: пытаемся получить трек из публикаций для $participantId")
            
            // Пытаемся получить трек напрямую из LocalParticipant
            try {
                val cameraVideoTrackMethod = localParticipant.javaClass.getMethod("cameraVideoTrack")
                val cameraTrack = cameraVideoTrackMethod.invoke(localParticipant) as? LocalVideoTrack
                if (cameraTrack != null) {
                    println("[LiveKitManager] getLocalVideoTrack: получен трек через cameraVideoTrack()")
                    localVideoTracks[participantId] = cameraTrack
                    return cameraTrack
                }
            } catch (e: Exception) {
                println("[LiveKitManager] getLocalVideoTrack: cameraVideoTrack() не доступен: ${e.message}")
            }
            
            // Fallback: получаем трек из публикаций
            val localVideoTrack = localParticipant.videoTrackPublications.let { pubs ->
                VideoTrackExtractor.extractLocalVideoTrack(pubs, participantId)
            }
            
            if (localVideoTrack != null) {
                localVideoTracks[participantId] = localVideoTrack
                println("[LiveKitManager] getLocalVideoTrack: сохранён трек для $participantId")
                return localVideoTrack
            } else {
                println("[LiveKitManager] getLocalVideoTrack: трек не найден в публикациях для $participantId")
            }
        }
        
        return null
    }
    
    actual fun getEglBaseContext(): Any? {
        return eglContextManager.getContext()
    }
    
    fun getRoom(): Room? {
        return room
    }
    
    private fun updateParticipants() {
        val currentRoom = room ?: return
        _participants.value = ParticipantUpdater.updateParticipants(currentRoom)
    }
}
