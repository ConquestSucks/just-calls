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

@OptIn(ExperimentalForeignApi::class)
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
            
            // Создаем экземпляр через alloc/init
            val allocSelector = sel_registerName("alloc")
            val initSelector = sel_registerName("init")
            
            @Suppress("CAST_NEVER_SUCCEEDS")
            val allocFunc: (Any?, SEL) -> Any? = objc_msgSend as (Any?, SEL) -> Any?
            val allocResult = allocFunc(wrapperClass, allocSelector)
            
            @Suppress("CAST_NEVER_SUCCEEDS")
            val initFunc: (Any?, SEL) -> Any? = objc_msgSend as (Any?, SEL) -> Any?
            val newWrapper = initFunc(allocResult, initSelector) as? ObjCObject
                ?: throw Exception("Failed to create LiveKitWrapper instance")
            
            // Создаем комнату
            val createRoomSelector = sel_registerName("createRoom")
            @Suppress("CAST_NEVER_SUCCEEDS")
            val createRoomFunc: (ObjCObject, SEL) -> Unit = objc_msgSend as (ObjCObject, SEL) -> Unit
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
                
                // Вызываем метод с блоком
                val connectSelector = sel_registerName("connectWithUrl:token:completion:")
                @Suppress("CAST_NEVER_SUCCEEDS")
                val connectFunc: (ObjCObject, SEL, NSString, NSString, @ObjCBlock (NSError?) -> Unit) -> Unit = 
                    objc_msgSend as (ObjCObject, SEL, NSString, NSString, @ObjCBlock (NSError?) -> Unit) -> Unit
                connectFunc(newWrapper, connectSelector, urlString, tokenString, block)
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
                    val block: @ObjCBlock () -> Unit = {
                        continuation.resume(Unit)
                    }
                    
                    val disconnectSelector = sel_registerName("disconnectWithCompletion:")
                    @Suppress("CAST_NEVER_SUCCEEDS")
                    val disconnectFunc: (ObjCObject, SEL, @ObjCBlock () -> Unit) -> Unit = 
                        objc_msgSend as (ObjCObject, SEL, @ObjCBlock () -> Unit) -> Unit
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
                    @Suppress("CAST_NEVER_SUCCEEDS")
                    val setMicrophoneFunc: (ObjCObject, SEL, Boolean, @ObjCBlock (NSError?) -> Unit) -> Unit = 
                        objc_msgSend as (ObjCObject, SEL, Boolean, @ObjCBlock (NSError?) -> Unit) -> Unit
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
                    @Suppress("CAST_NEVER_SUCCEEDS")
                    val setCameraFunc: (ObjCObject, SEL, Boolean, @ObjCBlock (NSError?) -> Unit) -> Unit = 
                        objc_msgSend as (ObjCObject, SEL, Boolean, @ObjCBlock (NSError?) -> Unit) -> Unit
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
            @Suppress("CAST_NEVER_SUCCEEDS")
            val func: (ObjCObject, SEL) -> Any? = objc_msgSend as (ObjCObject, SEL) -> Any?
            func(currentWrapper, selector) as? String
        } catch (e: Exception) {
            null
        }
    }
    
    private fun getLocalVideoTrack(): ObjCObject? {
        val currentWrapper = wrapper ?: return null
        return try {
            val selector = sel_registerName("getLocalVideoTrack")
            @Suppress("CAST_NEVER_SUCCEEDS")
            val func: (ObjCObject, SEL) -> Any? = objc_msgSend as (ObjCObject, SEL) -> Any?
            func(currentWrapper, selector) as? ObjCObject
        } catch (e: Exception) {
            null
        }
    }
    
    private fun isLocalCameraEnabled(): Boolean {
        val currentWrapper = wrapper ?: return false
        return try {
            val selector = sel_registerName("isLocalCameraEnabled")
            @Suppress("CAST_NEVER_SUCCEEDS")
            val func: (ObjCObject, SEL) -> Any? = objc_msgSend as (ObjCObject, SEL) -> Any?
            (func(currentWrapper, selector) as? NSNumber)?.boolValue ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    private fun getRemoteVideoTrack(participantId: String): ObjCObject? {
        val currentWrapper = wrapper ?: return null
        return try {
            val selector = sel_registerName("getRemoteVideoTrackWithParticipantId:")
            @Suppress("CAST_NEVER_SUCCEEDS")
            val func: (ObjCObject, SEL, NSString) -> Any? = objc_msgSend as (ObjCObject, SEL, NSString) -> Any?
            func(currentWrapper, selector, participantId as NSString) as? ObjCObject
        } catch (e: Exception) {
            null
        }
    }
    
    private fun updateParticipants() {
        val currentWrapper = wrapper ?: return
        _participants.value = ParticipantUpdaterIOS.updateParticipants(currentWrapper)
    }
    
    private fun setupDelegate(wrapper: ObjCObject) {
        // Создаем объект-делегат через Objective-C runtime
        // Для упрощения используем сам wrapper как делегат через блоки
        // В реальности нужно создать отдельный класс-делегат
        
        // Устанавливаем делегат через setDelegate:
        val setDelegateSelector = sel_registerName("setDelegate:")
        @Suppress("CAST_NEVER_SUCCEEDS")
        val setDelegateFunc: (ObjCObject, SEL, ObjCObject?) -> Unit = 
            objc_msgSend as (ObjCObject, SEL, ObjCObject?) -> Unit
        
        // Создаем простой делегат-объект
        // Для полноценной реализации нужно создать класс, реализующий протокол
        // Пока используем nil, так как создание делегата в Kotlin/Native сложнее
        // События будут обрабатываться через периодическое обновление
        setDelegateFunc(wrapper, setDelegateSelector, null)
    }
}
