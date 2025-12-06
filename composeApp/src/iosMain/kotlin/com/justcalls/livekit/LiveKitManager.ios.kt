package com.justcalls.livekit

import com.justcalls.data.models.responses.RoomTokenResult
import com.justcalls.livekit.internal.ParticipantUpdaterIOS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import platform.Foundation.*
import platform.objc.*
import platform.CoreGraphics.*
import kotlinx.cinterop.*
import com.justcalls.livekit.wrappers.*

@OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
actual class LiveKitManager {
    private var wrapper: ObjCObject? = null
    private var delegateImpl: ObjCObject? = null
    
    private val _participants = MutableStateFlow<List<LiveKitParticipant>>(emptyList())
    private var participantsCallback: ((List<LiveKitParticipant>) -> Unit)? = null
    
    private val videoTracks = mutableMapOf<String, ObjCObject>()
    private val localVideoTracks = mutableMapOf<String, ObjCObject>()
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    init {
        scope.launch {
            _participants.asStateFlow().collect { participants: List<LiveKitParticipant> ->
                participantsCallback?.invoke(participants)
            }
        }
    }
    
    actual suspend fun connect(tokenResult: RoomTokenResult, serverUrl: String) {
        try {
            // Используем сгенерированные классы из cinterop
            val newWrapper = com.justcalls.livekit.wrappers.LiveKitWrapper() as? ObjCObject
                ?: throw Exception("Failed to create LiveKitWrapper instance")
            
            // Создаем комнату
            (newWrapper as? com.justcalls.livekit.wrappers.LiveKitWrapper)?.createRoom()
            
            // Подключаемся к комнате
            suspendCancellableCoroutine<Unit> { continuation ->
                // Создаем блок для completion
                val block: (NSError?) -> Unit = { error ->
                    if (error != null) {
                        continuation.resumeWithException(Exception(error.localizedDescription ?: "Unknown error"))
                    } else {
                        continuation.resume(Unit)
                    }
                }
                
                // Используем обертки из cinterop согласно документации Kotlin/Native
                // Метод connectWithUrl:token:completion: транслируется в connectWithUrl(url:token:completion:)
                val liveKitWrapper = newWrapper as? com.justcalls.livekit.wrappers.LiveKitWrapper
                liveKitWrapper?.connectWithUrl(url = serverUrl, token = tokenResult.token, completion = block)
            }
            
            this.wrapper = newWrapper
            
            // Создаем и устанавливаем делегат для получения событий о треках
            setupDelegate(newWrapper)
            
            // Запускаем периодическое обновление участников
            scope.launch {
                while (wrapper != null) {
                    updateParticipants()
                    delay(1000)
                }
            }
            
            updateParticipants()
            
        } catch (e: Exception) {
            throw e
        }
    }
    
    actual suspend fun disconnect() {
        try {
            videoTracks.clear()
            localVideoTracks.clear()
            
            val currentWrapper = wrapper
            if (currentWrapper != null) {
                suspendCancellableCoroutine<Unit> { continuation ->
                    val block: () -> Unit = {
                        continuation.resume(Unit)
                    }
                    
                    // Используем обертки из cinterop
                    (currentWrapper as? com.justcalls.livekit.wrappers.LiveKitWrapper)?.disconnectWithCompletion(block)
                }
            }
            
            wrapper = null
            delegateImpl = null
            _participants.value = emptyList()
        } catch (e: Exception) {
            // Ignore
        }
    }
    
    actual fun setMicrophoneEnabled(enabled: Boolean) {
        scope.launch {
            try {
                val currentWrapper = wrapper
                if (currentWrapper == null) {
                    println("LiveKitManager: wrapper is null, cannot set microphone")
                    return@launch
                }
                
                suspendCancellableCoroutine<Unit> { continuation ->
                    val block: (NSError?) -> Unit = { error ->
                        if (error != null) {
                            println("LiveKitManager: setMicrophoneEnabled error: ${error.localizedDescription}")
                        } else {
                            println("LiveKitManager: setMicrophoneEnabled success: $enabled")
                            updateParticipants()
                        }
                        continuation.resume(Unit)
                    }
                    
                    // Используем обертки из cinterop согласно документации Kotlin/Native
                    // Используем позиционные параметры вместо именованных
                    val liveKitWrapper = currentWrapper as? com.justcalls.livekit.wrappers.LiveKitWrapper
                    if (liveKitWrapper == null) {
                        println("LiveKitManager: failed to cast wrapper to LiveKitWrapper")
                        continuation.resume(Unit)
                    } else {
                        println("LiveKitManager: calling setMicrophoneEnabled($enabled)")
                        liveKitWrapper.setMicrophoneEnabled(enabled, block)
                    }
                }
            } catch (e: Exception) {
                println("LiveKitManager: setMicrophoneEnabled exception: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    actual fun setCameraEnabled(enabled: Boolean) {
        scope.launch {
            try {
                val currentWrapper = wrapper
                if (currentWrapper == null) {
                    println("LiveKitManager: wrapper is null, cannot set camera")
                    return@launch
                }
                
                suspendCancellableCoroutine<Unit> { continuation ->
                    val block: (NSError?) -> Unit = { error ->
                        if (error != null) {
                            println("LiveKitManager: setCameraEnabled error: ${error.localizedDescription}")
                        } else {
                            println("LiveKitManager: setCameraEnabled success: $enabled")
                            if (!enabled) {
                                val localIdentity = getLocalParticipantIdentity()
                                localIdentity?.let { localVideoTracks.remove(it) }
                            }
                            updateParticipants()
                        }
                        continuation.resume(Unit)
                    }
                    
                    // Используем обертки из cinterop согласно документации Kotlin/Native
                    // Используем позиционные параметры вместо именованных
                    val liveKitWrapper = currentWrapper as? com.justcalls.livekit.wrappers.LiveKitWrapper
                    if (liveKitWrapper == null) {
                        println("LiveKitManager: failed to cast wrapper to LiveKitWrapper")
                        continuation.resume(Unit)
                    } else {
                        println("LiveKitManager: calling setCameraEnabled($enabled)")
                        liveKitWrapper.setCameraEnabled(enabled, block)
                    }
                }
            } catch (e: Exception) {
                println("LiveKitManager: setCameraEnabled exception: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    actual fun observeParticipants(callback: (List<LiveKitParticipant>) -> Unit) {
        participantsCallback = callback
        callback(_participants.value)
    }
    
    actual fun getVideoSurface(participantId: String): Any? {
        val currentWrapper = wrapper ?: return null
        
        try {
            val localIdentity = getLocalParticipantIdentity()
            if (participantId == localIdentity) {
                localVideoTracks[participantId]?.let {
                    return "local:$participantId"
                }
                
                val localVideoTrack = getLocalVideoTrack()
                if (localVideoTrack != null) {
                    localVideoTracks[participantId] = localVideoTrack
                    return "local:$participantId"
                } else {
                    val isCameraEnabled = isLocalCameraEnabled()
                    if (isCameraEnabled) {
                        return "local:$participantId"
                    }
                }
            } else {
                val remoteVideoTrack = getRemoteVideoTrack(participantId)
                if (remoteVideoTrack != null) {
                    videoTracks[participantId] = remoteVideoTrack
                    return "remote:$participantId"
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
        
        return null
    }
    
    fun getVideoTrack(participantId: String): ObjCObject? {
        return videoTracks[participantId] ?: localVideoTracks[participantId]
    }
    
    fun getWrapper(): ObjCObject? {
        return wrapper
    }
    
    private fun getLocalParticipantIdentity(): String? {
        val currentWrapper = wrapper ?: return null
        return try {
            (currentWrapper as? com.justcalls.livekit.wrappers.LiveKitWrapper)?.getLocalParticipantIdentity() as? String
        } catch (e: Exception) {
            null
        }
    }
    
    private fun getLocalVideoTrack(): ObjCObject? {
        val currentWrapper = wrapper ?: return null
        return try {
            (currentWrapper as? com.justcalls.livekit.wrappers.LiveKitWrapper)?.getLocalVideoTrack() as? ObjCObject
        } catch (e: Exception) {
            null
        }
    }
    
    private fun isLocalCameraEnabled(): Boolean {
        val currentWrapper = wrapper ?: return false
        return try {
            (currentWrapper as? com.justcalls.livekit.wrappers.LiveKitWrapper)?.isLocalCameraEnabled() ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    private fun getRemoteVideoTrack(participantId: String): ObjCObject? {
        val currentWrapper = wrapper ?: return null
        return try {
            (currentWrapper as? com.justcalls.livekit.wrappers.LiveKitWrapper)?.getRemoteVideoTrackWithParticipantId(participantId) as? ObjCObject
        } catch (e: Exception) {
            null
        }
    }
    
    private fun updateParticipants() {
        val currentWrapper = wrapper ?: return
        _participants.value = ParticipantUpdaterIOS.updateParticipants(currentWrapper)
    }
    
    private fun setupDelegate(wrapper: ObjCObject) {
        // Устанавливаем делегат через обертки из cinterop
        // События будут обрабатываться через периодическое обновление
        (wrapper as? com.justcalls.livekit.wrappers.LiveKitWrapper)?.setDelegate(null)
    }
    
    fun requestCameraPermission(completion: (Boolean) -> Unit) {
        val currentWrapper = wrapper ?: run {
            completion(false)
            return
        }
        scope.launch {
            try {
                suspendCancellableCoroutine<Unit> { continuation ->
                    val block: (Boolean) -> Unit = { granted ->
                        completion(granted)
                        continuation.resume(Unit)
                    }
                    val liveKitWrapper = currentWrapper as? com.justcalls.livekit.wrappers.LiveKitWrapper
                    liveKitWrapper?.requestCameraPermissionWithCompletion(block)
                }
            } catch (e: Exception) {
                completion(false)
            }
        }
    }
    
    fun requestMicrophonePermission(completion: (Boolean) -> Unit) {
        val currentWrapper = wrapper ?: run {
            completion(false)
            return
        }
        scope.launch {
            try {
                suspendCancellableCoroutine<Unit> { continuation ->
                    val block: (Boolean) -> Unit = { granted ->
                        completion(granted)
                        continuation.resume(Unit)
                    }
                    val liveKitWrapper = currentWrapper as? com.justcalls.livekit.wrappers.LiveKitWrapper
                    liveKitWrapper?.requestMicrophonePermissionWithCompletion(block)
                }
            } catch (e: Exception) {
                completion(false)
            }
        }
    }
}
