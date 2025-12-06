package com.justcalls.livekit.internal

import io.livekit.android.room.track.LocalVideoTrack
import io.livekit.android.room.track.LocalTrackPublication
import io.livekit.android.room.track.RemoteTrackPublication
import io.livekit.android.room.track.VideoTrack

internal object VideoTrackExtractor {
    
    fun getTrackFromPublication(publication: Any): Any? {
        return try {
            val trackField = publication.javaClass.getDeclaredField("track")
            trackField.isAccessible = true
            val track = trackField.get(publication)
            track
        } catch (e1: Exception) {
            try {
                val getTrackMethod = publication.javaClass.getMethod("getTrack")
                val track = getTrackMethod.invoke(publication)
                track
            } catch (e2: Exception) {
                null
            }
        }
    }
    
    fun extractLocalVideoTrack(
        publications: Any?
    ): LocalVideoTrack? {
        if (publications == null) {
            return null
        }
        
        return try {
            when (publications) {
                is Collection<*> -> {
                    val first = publications.firstOrNull()

                    when {
                        first is Pair<*, *> -> {
                            val pairFirst = first.first
                            val pairSecond = first.second

                            when {
                                pairSecond is LocalVideoTrack -> pairSecond
                                pairFirst is LocalVideoTrack -> pairFirst
                                else -> {
                                    val publication = when {
                                        pairFirst is LocalTrackPublication -> pairFirst
                                        pairSecond is LocalTrackPublication -> pairSecond
                                        pairFirst != null -> pairFirst
                                        pairSecond != null -> pairSecond
                                        else -> null
                                    }

                                    if (publication != null) {
                                        getTrackFromPublication(publication) as? LocalVideoTrack
                                    } else {
                                        null
                                    }
                                }
                            }
                        }

                        else -> {
                            val publication = first
                            when {
                                publication is LocalVideoTrack -> publication
                                publication is LocalTrackPublication -> getTrackFromPublication(publication) as? LocalVideoTrack
                                publication != null -> getTrackFromPublication(publication) as? LocalVideoTrack
                                else -> null
                            }
                        }
                    }
                }

                is Map<*, *> -> {
                    val firstValue = publications.values.firstOrNull()
                    when {
                        firstValue is LocalVideoTrack -> firstValue
                        firstValue != null -> getTrackFromPublication(firstValue) as? LocalVideoTrack
                        else -> null
                    }
                }

                else -> {
                    val iterator = (publications as? Iterable<*>)?.iterator()
                    val first = iterator?.next()
                    when {
                        first is Pair<*, *> -> {
                            val publication = first.first
                            if (publication != null) {
                                getTrackFromPublication(publication) as? LocalVideoTrack
                            } else null
                        }
                        first is LocalVideoTrack -> first
                        first != null -> getTrackFromPublication(first) as? LocalVideoTrack
                        else -> null
                    }
                }
            }
        } catch (e: Exception) {
            null
        }
    }
    
    fun extractRemoteVideoTrack(
        publications: Any
    ): VideoTrack? {
        return try {
            val publication: RemoteTrackPublication? = when (publications) {
                is Collection<*> -> {
                    val first = publications.firstOrNull()
                    when (first) {
                        is Pair<*, *> -> {
                            val pairFirst = first.first
                            pairFirst as? RemoteTrackPublication
                        }
                        is RemoteTrackPublication -> first
                        else -> null
                    }
                }
                is Map<*, *> -> {
                    val firstValue = publications.values.firstOrNull()
                    firstValue as? RemoteTrackPublication
                }
                else -> null
            }
            
            if (publication != null) {
                subscribeToPublication(publication)
                
                var track = getTrackFromPublication(publication) as? VideoTrack
                
                if (track == null && publications is Collection<*>) {
                    val first = publications.firstOrNull()
                    if (first is Pair<*, *>) {
                        val pairSecond = first.second
                        if (pairSecond is VideoTrack) {
                            track = pairSecond
                        }
                    }
                }
                
                track
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun subscribeToPublication(publication: RemoteTrackPublication) {
        try {
            val setSubscribedMethod = publication.javaClass.getMethod("setSubscribed", Boolean::class.java)
            val isSubscribed = try {
                val getSubscribedMethod = publication.javaClass.getMethod("isSubscribed")
                getSubscribedMethod.invoke(publication) as? Boolean ?: false
            } catch (e: Exception) {
                false
            }
            
            if (!isSubscribed) {
                setSubscribedMethod.invoke(publication, true)
                
                var attempts = 0
                while (attempts < 10) {
                    Thread.sleep(300)
                    val track = getTrackFromPublication(publication) as? VideoTrack
                    if (track != null) {
                        break
                    }
                    attempts++
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
    }
}
