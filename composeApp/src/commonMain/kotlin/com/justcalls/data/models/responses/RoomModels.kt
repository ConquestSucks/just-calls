package com.justcalls.data.models.responses

import kotlinx.serialization.Serializable

@Serializable
data class RoomDto(
    val name: String,
    val title: String? = null,
    val maxParticipants: Int,
    val participants: Int
)

@Serializable
data class IceServerDto(
    val urls: List<String>
)

@Serializable
data class IceConfigDto(
    val iceServers: List<IceServerDto>,
    val username: String,
    val credential: String
)

@Serializable
data class RoomTokenResult(
    val token: String,
    val userIdentity: String,
    val iceConfig: IceConfigDto
)



