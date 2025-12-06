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
import kotlinx.cinterop.*

@OptIn(ExperimentalForeignApi::class)
actual class LiveKitManager {
    private var wrapper: platform.objc.ObjCObject? = null
    private var delegateImpl: platform.objc.ObjCObject? = null
    
    private val _participants = MutableStateFlow<List<LiveKitParticipant>>(emptyList())
    private var participantsCallback: ((List<LiveKitParticipant>) -> Unit)? = null
    
    private val videoTracks = mutableMapOf<String, platform.objc.ObjCObject>()
    private val localVideoTracks = mutableMapOf<String, platform.objc.ObjCObject>()
    
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
            val allocFunc = platform.objc.objc_msgSend as (Any?, platform.objc.ObjCSelector) -> Any?
            val allocResult = allocFunc(wrapperClass, allocSelector)
            val initFunc = platform.objc.objc_msgSend as (Any?, platform.objc.ObjCSelector) -> Any?
            val newWrapper = initFunc(allocResult, initSelector) as? platform.objc.ObjCObject
                ?: throw Exception("Failed to create LiveKitWrapper instance")
            
            // Создаем комнату
            val createRoomSelector = sel_registerName("createRoom")
            val createRoomFunc = platform.objc.objc_msgSend as (platform.objc.ObjCObject, platform.objc.ObjCSelector) -> Unit
            createRoomFunc(newWrapper, createRoomSelector)
            
            // Подключаемся к комнате
            suspendCancellableCoroutine<Unit> { continuation ->
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
                
                // Используем правильный вызов метода с блоками
                val connectSelector = sel_registerName("connectWithUrl:token:completion:")
                val connectFunc = platform.objc.objc_msgSend as (platform.objc.ObjCObject, platform.objc.ObjCSelector, NSString, NSString, @ObjCBlock (NSError?) -> Unit) -> Unit
                connectFunc(newWrapper, connectSelector, urlString, tokenString, block)
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
                    val block: @ObjCBlock () -> Unit = {
                        continuation.resume(Unit)
                    }
                    
                    val disconnectSelector = sel_registerName("disconnectWithCompletion:")
                    val disconnectFunc = platform.objc.objc_msgSend as (platform.objc.ObjCObject, platform.objc.ObjCSelector, @ObjCBlock () -> Unit) -> Unit
                    disconnectFunc(currentWrapper, disconnectSelector, block)
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
                    val block: @ObjCBlock (NSError?) -> Unit = { error ->
                        if (error == null) {
                            updateParticipants()
                        }
                        continuation.resume(Unit)
                    }
                    
                    val setMicrophoneSelector = sel_registerName("setMicrophoneEnabled:completion:")
                    val setMicrophoneFunc = platform.objc.objc_msgSend as (platform.objc.ObjCObject, platform.objc.ObjCSelector, Boolean, @ObjCBlock (NSError?) -> Unit) -> Unit
                    setMicrophoneFunc(currentWrapper, setMicrophoneSelector, enabled, block)
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
                    
                    val setCameraSelector = sel_registerName("setCameraEnabled:completion:")
                    val setCameraFunc = platform.objc.objc_msgSend as (platform.objc.ObjCObject, platform.objc.ObjCSelector, Boolean, @ObjCBlock (NSError?) -> Unit) -> Unit
                    setCameraFunc(currentWrapper, setCameraSelector, enabled, block)
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
    
    fun getVideoTrack(participantId: String): platform.objc.ObjCObject? {
        return videoTracks[participantId] ?: localVideoTracks[participantId]
    }
    
    fun getWrapper(): platform.objc.ObjCObject? {
        return wrapper
    }
    
    private fun getLocalParticipantIdentity(): String? {
        val currentWrapper = wrapper ?: return null
        return try {
            val selector = sel_registerName("getLocalParticipantIdentity")
            val func = platform.objc.objc_msgSend as (platform.objc.ObjCObject, platform.objc.ObjCSelector) -> Any?
            func(currentWrapper, selector) as? String
        } catch (e: Exception) {
            null
        }
    }
    
    private fun getLocalVideoTrack(): platform.objc.ObjCObject? {
        val currentWrapper = wrapper ?: return null
        return try {
            val selector = sel_registerName("getLocalVideoTrack")
            val func = platform.objc.objc_msgSend as (platform.objc.ObjCObject, platform.objc.ObjCSelector) -> Any?
            func(currentWrapper, selector) as? platform.objc.ObjCObject
        } catch (e: Exception) {
            null
        }
    }
    
    private fun isLocalCameraEnabled(): Boolean {
        val currentWrapper = wrapper ?: return false
        return try {
            val selector = sel_registerName("isLocalCameraEnabled")
            val func = platform.objc.objc_msgSend as (platform.objc.ObjCObject, platform.objc.ObjCSelector) -> Any?
            (func(currentWrapper, selector) as? NSNumber)?.boolValue ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    private fun getRemoteVideoTrack(participantId: String): platform.objc.ObjCObject? {
        val currentWrapper = wrapper ?: return null
        return try {
            val selector = sel_registerName("getRemoteVideoTrack:")
            val func = platform.objc.objc_msgSend as (platform.objc.ObjCObject, platform.objc.ObjCSelector, NSString) -> Any?
            func(currentWrapper, selector, participantId as NSString) as? platform.objc.ObjCObject
        } catch (e: Exception) {
            null
        }
    }
    
    private fun updateParticipants() {
        val currentWrapper = wrapper ?: return
        _participants.value = ParticipantUpdaterIOS.updateParticipants(currentWrapper)
    }
}
