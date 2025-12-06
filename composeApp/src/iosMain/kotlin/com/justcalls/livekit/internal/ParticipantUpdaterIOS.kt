package com.justcalls.livekit.internal

import com.justcalls.livekit.LiveKitParticipant
import platform.Foundation.*
import platform.objc.*
import kotlinx.cinterop.*

@OptIn(ExperimentalForeignApi::class)
internal object ParticipantUpdaterIOS {
    
    fun updateParticipants(wrapper: platform.objc.ObjCObject): List<LiveKitParticipant> {
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
        wrapper: platform.objc.ObjCObject,
        participants: MutableList<LiveKitParticipant>
    ) {
        try {
            val identity = objc_msgSend(wrapper, sel_registerName("getLocalParticipantIdentity")) as? String ?: "local"
            val name = objc_msgSend(wrapper, sel_registerName("getLocalParticipantName")) as? String ?: identity
            val isCameraEnabled = objc_msgSend(wrapper, sel_registerName("isLocalCameraEnabled")) as? Boolean ?: false
            val isMicrophoneEnabled = objc_msgSend(wrapper, sel_registerName("isLocalMicrophoneEnabled")) as? Boolean ?: false
            
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
        wrapper: platform.objc.ObjCObject,
        participants: MutableList<LiveKitParticipant>
    ) {
        try {
            val remoteParticipants = objc_msgSend(wrapper, sel_registerName("getRemoteParticipants")) as? NSArray
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
