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

@Suppress("EXPERIMENTAL_OBJC_INTEROP")
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
            // Создаем LiveKitWrapper через Objective-C runtime
            val wrapperClass = objc_getClass("LiveKitWrapper")
            if (wrapperClass == null) {
                throw Exception("LiveKitWrapper class not found. Make sure LiveKitWrapper.swift is added to the Xcode project.")
            }
            
            val allocSelector = sel_registerName("alloc")
            val initSelector = sel_registerName("init")
            val allocResult = objc_msgSend(wrapperClass, allocSelector)
            val newWrapper = objc_msgSend(allocResult, initSelector) as? ObjCObject
                ?: throw Exception("Failed to create LiveKitWrapper instance")
            
            // Создаем комнату
            val createRoomSelector = sel_registerName("createRoom")
            objc_msgSend(newWrapper, createRoomSelector)
            
            // Создаем и устанавливаем delegate для обработки событий треков
            // Delegate будет обрабатывать события через callback в scope.launch
            // Пока используем периодическое обновление, delegate можно добавить позже если нужно

            // Подключаемся к комнате
            suspendCancellableCoroutine<Unit> { continuation ->
                val connectSelector = sel_registerName("connect:token:completion:")
                val urlString = serverUrl as NSString
                val tokenString = tokenResult.token as NSString
                
                // Создаем блок для completion
                val block: @ObjCBlock (NSError?) -> Unit = { error ->
                    if (error != null) {
                        continuation.resumeWithException(Exception(error.localizedDescription ?: "Unknown error"))
                    } else {
                        continuation.resume(Unit)
                    }
                }
                
                objc_msgSend(newWrapper, connectSelector, urlString, tokenString, block)
            }
            
            this.wrapper = newWrapper
            
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
                    val disconnectSelector = sel_registerName("disconnectWithCompletion:")
                    val block: @ObjCBlock () -> Unit = {
                        continuation.resume(Unit)
                    }
                    objc_msgSend(currentWrapper, disconnectSelector, block)
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
                val currentWrapper = wrapper ?: return@launch
                suspendCancellableCoroutine<Unit> { continuation ->
                    val setMicrophoneSelector = sel_registerName("setMicrophoneEnabled:completion:")
                    val block: @ObjCBlock (NSError?) -> Unit = { error ->
                        if (error == null) {
                            updateParticipants()
                        }
                        continuation.resume(Unit)
                    }
                    objc_msgSend(currentWrapper, setMicrophoneSelector, enabled, block)
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
    
    actual fun setCameraEnabled(enabled: Boolean) {
        scope.launch {
            try {
                val currentWrapper = wrapper ?: return@launch
                suspendCancellableCoroutine<Unit> { continuation ->
                    val setCameraSelector = sel_registerName("setCameraEnabled:completion:")
                    val block: @ObjCBlock (NSError?) -> Unit = { error ->
                        if (error == null) {
                            if (!enabled) {
                                val localIdentity = getLocalParticipantIdentity()
                                localIdentity?.let { localVideoTracks.remove(it) }
                            }
                            updateParticipants()
                        }
                        continuation.resume(Unit)
                    }
                    objc_msgSend(currentWrapper, setCameraSelector, enabled, block)
                }
            } catch (e: Exception) {
                // Ignore
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
            val selector = sel_registerName("getLocalParticipantIdentity")
            objc_msgSend(currentWrapper, selector) as? String
        } catch (e: Exception) {
            null
        }
    }
    
    private fun getLocalVideoTrack(): ObjCObject? {
        val currentWrapper = wrapper ?: return null
        return try {
            val selector = sel_registerName("getLocalVideoTrack")
            objc_msgSend(currentWrapper, selector) as? ObjCObject
        } catch (e: Exception) {
            null
        }
    }
    
    private fun isLocalCameraEnabled(): Boolean {
        val currentWrapper = wrapper ?: return false
        return try {
            val selector = sel_registerName("isLocalCameraEnabled")
            (objc_msgSend(currentWrapper, selector) as? NSNumber)?.boolValue ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    private fun getRemoteVideoTrack(participantId: String): ObjCObject? {
        val currentWrapper = wrapper ?: return null
        return try {
            val selector = sel_registerName("getRemoteVideoTrack:")
            objc_msgSend(currentWrapper, selector, participantId as NSString) as? ObjCObject
        } catch (e: Exception) {
            null
        }
    }
    
    private fun updateParticipants() {
        val currentWrapper = wrapper ?: return
        _participants.value = ParticipantUpdaterIOS.updateParticipants(currentWrapper)
    }
}
