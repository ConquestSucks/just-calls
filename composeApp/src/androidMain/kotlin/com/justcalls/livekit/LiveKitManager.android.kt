package com.justcalls.livekit

import com.justcalls.JustCallsApplication
import com.justcalls.data.models.responses.RoomTokenResult
import com.justcalls.livekit.internal.EglContextManager
import com.justcalls.livekit.internal.ParticipantUpdater
import com.justcalls.livekit.internal.VideoTrackExtractor
import io.livekit.android.LiveKit
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
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
            val appContext = JustCallsApplication.instance
            
            eglContextManager.initialize()
            
            val newRoom = LiveKit.create(appContext)
            
            newRoom.connect(serverUrl, tokenResult.token)
            
            this.room = newRoom
            
            scope.launch {
                newRoom.events.collect { event ->
                    when (event) {
                        is RoomEvent.TrackSubscribed -> onTrackSubscribed(event)
                        is RoomEvent.TrackUnsubscribed -> onTrackUnsubscribed(event)
                        else -> {}
                    }
                }
            }
            
            scope.launch {
                while (room != null) {
                    updateParticipants()
                    kotlinx.coroutines.delay(1000)
                }
            }
            
            updateParticipants()
            
        } catch (e: Exception) {
            throw e
        }
    }
    
    private fun onTrackSubscribed(event: RoomEvent.TrackSubscribed) {
        val track = event.track
        
        if (track is VideoTrack) {
            val participantIdentity = event.participant.identity?.toString()
            if (participantIdentity != null) {
                val isLocal = room?.localParticipant?.identity?.toString() == participantIdentity
                
                if (isLocal) {
                    if (track is LocalVideoTrack) {
                        localVideoTracks[participantIdentity] = track
                    }
                } else {
                    videoTracks[participantIdentity] = track
                }
                
                updateParticipants()
            }
        }
    }
    
    private fun onTrackUnsubscribed(event: RoomEvent.TrackUnsubscribed) {
        val track = event.track
        
        if (track is VideoTrack) {
            val participantIdentity = event.participant.identity?.toString()
            if (participantIdentity != null) {
                videoTracks.remove(participantIdentity)
                localVideoTracks.remove(participantIdentity)
                
                updateParticipants()
            }
        }
    }
    
    actual suspend fun disconnect() {
        try {
            videoTracks.clear()
            localVideoTracks.clear()
            
            room?.disconnect()
            room = null
            
            eglContextManager.release()
            
            _participants.value = emptyList()
        } catch (e: Exception) {
            // Ignore
        }
    }
    
    actual fun setMicrophoneEnabled(enabled: Boolean) {
        scope.launch {
            try {
                room?.localParticipant?.setMicrophoneEnabled(enabled)
                updateParticipants()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
    
    actual fun setCameraEnabled(enabled: Boolean) {
        scope.launch {
            try {
                room?.localParticipant?.setCameraEnabled(enabled)
                
                if (!enabled) {
                    val localIdentity = room?.localParticipant?.identity?.toString()
                    localIdentity?.let { localVideoTracks.remove(it) }
                    updateParticipants()
                } else {
                    updateParticipants()
                }
            } catch (e: Exception) {
                // Ignore
            }
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
                localVideoTracks[participantId]?.let {
                    return "local:$participantId"
                }
                
                val localParticipant = currentRoom.localParticipant
                val localVideoTrack = localParticipant.videoTrackPublications.let { pubs ->
                    VideoTrackExtractor.extractLocalVideoTrack(pubs, participantId)
                }
                
                if (localVideoTrack != null) {
                    localVideoTracks[participantId] = localVideoTrack
                    return "local:$participantId"
                } else {
                    val isCameraEnabled = localParticipant.videoTrackPublications.let { pubs ->
                        (pubs as Collection<*>).isNotEmpty()
                    }
                    
                    if (isCameraEnabled) {
                        return "local:$participantId"
                    }
                }
            } else {
                val remoteParticipant = currentRoom.remoteParticipants.values.find { participant -> 
                    participant.identity?.toString() == participantId 
                }
                
                if (remoteParticipant != null) {
                    videoTracks[participantId]?.let {
                        return "remote:$participantId"
                    }
                    
                    val videoTrack = VideoTrackExtractor.extractRemoteVideoTrack(
                        remoteParticipant.videoTrackPublications,
                        participantId
                    )
                    
                    if (videoTrack != null) {
                        videoTracks[participantId] = videoTrack
                        return "remote:$participantId"
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
        
        return null
    }
    
    fun getVideoTrack(participantId: String): VideoTrack? {
        return videoTracks[participantId]
    }
    
    fun getLocalVideoTrack(participantId: String): LocalVideoTrack? {
        localVideoTracks[participantId]?.let {
            return it
        }
        
        val localParticipant = room?.localParticipant
        if (localParticipant != null) {
            try {
                val cameraVideoTrackMethod = localParticipant.javaClass.getMethod("cameraVideoTrack")
                val cameraTrack = cameraVideoTrackMethod.invoke(localParticipant) as? LocalVideoTrack
                if (cameraTrack != null) {
                    localVideoTracks[participantId] = cameraTrack
                    return cameraTrack
                }
            } catch (e: Exception) {
                // Ignore
            }
            
            val localVideoTrack = localParticipant.videoTrackPublications.let { pubs ->
                VideoTrackExtractor.extractLocalVideoTrack(pubs, participantId)
            }
            
            if (localVideoTrack != null) {
                localVideoTracks[participantId] = localVideoTrack
                return localVideoTrack
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
