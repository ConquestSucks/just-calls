package com.justcalls.livekit.internal

import io.livekit.android.room.track.LocalVideoTrack
import io.livekit.android.room.track.LocalTrackPublication
import io.livekit.android.room.track.RemoteTrackPublication
import io.livekit.android.room.track.VideoTrack

internal object VideoTrackExtractor {
    
    fun getTrackFromPublication(publication: Any): Any? {
        return try {
            println("[VideoTrackExtractor] Получение трека из публикации: ${publication.javaClass.name}")
            val trackField = publication.javaClass.getDeclaredField("track")
            trackField.isAccessible = true
            val track = trackField.get(publication)
            println("[VideoTrackExtractor] Получен трек через поле 'track': ${track != null}, тип=${track?.javaClass?.name}")
            track
        } catch (e1: Exception) {
            println("[VideoTrackExtractor] Ошибка при получении через поле 'track': ${e1.message}")
            try {
                val getTrackMethod = publication.javaClass.getMethod("getTrack")
                val track = getTrackMethod.invoke(publication)
                println("[VideoTrackExtractor] Получен трек через метод 'getTrack()': ${track != null}, тип=${track?.javaClass?.name}")
                track
            } catch (e2: Exception) {
                println("[VideoTrackExtractor] Не удалось получить трек из публикации: ${e2.message}")
                null
            }
        }
    }
    
    fun extractLocalVideoTrack(
        publications: Any?,
        participantId: String
    ): LocalVideoTrack? {
        if (publications == null) return null
        
        return try {
            when {
                publications is Collection<*> -> {
                    val first = publications.firstOrNull()
                    when {
                        first is Pair<*, *> -> {
                            val pairFirst = first.first
                            val pairSecond = first.second
                            
                            when {
                                pairSecond is LocalVideoTrack -> {
                                    println("[VideoTrackExtractor] Найден трек в Pair.second")
                                    pairSecond
                                }
                                pairFirst is LocalVideoTrack -> {
                                    println("[VideoTrackExtractor] Найден трек в Pair.first")
                                    pairFirst
                                }
                                else -> {
                                    val publication = pairFirst
                                    if (publication != null) {
                                        getTrackFromPublication(publication) as? LocalVideoTrack
                                    } else null
                                }
                            }
                        }
                        else -> {
                            val publication = first
                            when {
                                publication is LocalVideoTrack -> publication
                                publication != null -> getTrackFromPublication(publication) as? LocalVideoTrack
                                else -> null
                            }
                        }
                    }
                }
                publications is Map<*, *> -> {
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
            println("[VideoTrackExtractor] Ошибка при извлечении локального трека: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    fun extractRemoteVideoTrack(
        publications: Any,
        participantId: String
    ): VideoTrack? {
        return try {
            val publication: RemoteTrackPublication? = when {
                publications is Collection<*> -> {
                    val first = publications.firstOrNull()
                    when {
                        first is Pair<*, *> -> {
                            val pairFirst = first.first
                            if (pairFirst is RemoteTrackPublication) pairFirst else null
                        }
                        first is RemoteTrackPublication -> first
                        else -> null
                    }
                }
                publications is Map<*, *> -> {
                    val firstValue = publications.values.firstOrNull()
                    if (firstValue is RemoteTrackPublication) firstValue else null
                }
                else -> null
            }
            
            if (publication != null) {
                println("[VideoTrackExtractor] Найдена публикация: ${publication.javaClass.name}")
                
                subscribeToPublication(publication)
                
                var track = getTrackFromPublication(publication) as? VideoTrack
                
                if (track == null && publications is Collection<*>) {
                    val first = publications.firstOrNull()
                    if (first is Pair<*, *>) {
                        val pairSecond = first.second
                        if (pairSecond is VideoTrack) {
                            println("[VideoTrackExtractor] Получен трек из Pair.second")
                            track = pairSecond
                        }
                    }
                }
                
                track
            } else {
                null
            }
        } catch (e: Exception) {
            println("[VideoTrackExtractor] Ошибка при извлечении удалённого трека: ${e.message}")
            e.printStackTrace()
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
                println("[VideoTrackExtractor] Подписываемся на публикацию трека")
                setSubscribedMethod.invoke(publication, true)
                Thread.sleep(200) // Ждём немного, чтобы трек стал доступен
            } else {
                println("[VideoTrackExtractor] Уже подписаны на публикацию")
            }
        } catch (e: Exception) {
            println("[VideoTrackExtractor] Ошибка при подписке на публикацию: ${e.message}")
            e.printStackTrace()
        }
    }
}

