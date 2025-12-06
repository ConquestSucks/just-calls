package com.justcalls.livekit.internal

import com.justcalls.livekit.LiveKitParticipant
import platform.Foundation.*
import platform.objc.*
import kotlinx.cinterop.*

@OptIn(ExperimentalForeignApi::class)
internal object ParticipantUpdaterIOS {
    
    fun updateParticipants(wrapper: ObjCObject): List<LiveKitParticipant> {
        val participants = mutableListOf<LiveKitParticipant>()
        
        try {
            addLocalParticipant(wrapper, participants)
            addRemoteParticipants(wrapper, participants)
        } catch (e: Exception) {
            // Ignore
        }
        
        return participants
    }
    
    private fun addLocalParticipant(
        wrapper: ObjCObject,
        participants: MutableList<LiveKitParticipant>
    ) {
        try {
            @Suppress("UNCHECKED_CAST")
            val identityFunc: (ObjCObject, Any?) -> Any? = reinterpretCast(objc_msgSend)
            val identity = identityFunc(wrapper, sel_registerName("getLocalParticipantIdentity")) as? String ?: "local"
            val name = identityFunc(wrapper, sel_registerName("getLocalParticipantName")) as? String ?: identity
            val isCameraEnabled = (identityFunc(wrapper, sel_registerName("isLocalCameraEnabled")) as? NSNumber)?.boolValue ?: false
            val isMicrophoneEnabled = (identityFunc(wrapper, sel_registerName("isLocalMicrophoneEnabled")) as? NSNumber)?.boolValue ?: false
            
            participants.add(
                LiveKitParticipant(
                    identity = identity,
                    name = name,
                    isLocal = true,
                    isCameraEnabled = isCameraEnabled,
                    isMicrophoneEnabled = isMicrophoneEnabled
                )
            )
        } catch (e: Exception) {
            // Ignore
        }
    }
    
    private fun addRemoteParticipants(
        wrapper: ObjCObject,
        participants: MutableList<LiveKitParticipant>
    ) {
        try {
            @Suppress("UNCHECKED_CAST")
            val getRemoteFunc: (ObjCObject, Any?) -> Any? = reinterpretCast(objc_msgSend)
            val remoteParticipants = getRemoteFunc(wrapper, sel_registerName("getRemoteParticipants")) as? NSArray
            if (remoteParticipants != null) {
                for (i in 0 until remoteParticipants.count.toInt()) {
                    val participantDict = remoteParticipants.objectAtIndex(i.toULong()) as? NSDictionary
                    if (participantDict != null) {
                        val identity = (participantDict.objectForKey("identity" as NSString) as? String) ?: "unknown"
                        val name = (participantDict.objectForKey("name" as NSString) as? String) ?: identity
                        val isCameraEnabled = (participantDict.objectForKey("isCameraEnabled" as NSString) as? NSNumber)?.boolValue ?: false
                        val isMicrophoneEnabled = (participantDict.objectForKey("isMicrophoneEnabled" as NSString) as? NSNumber)?.boolValue ?: false
                        
                        participants.add(
                            LiveKitParticipant(
                                identity = identity,
                                name = name,
                                isLocal = false,
                                isCameraEnabled = isCameraEnabled,
                                isMicrophoneEnabled = isMicrophoneEnabled
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
    }
}
