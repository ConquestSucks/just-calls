package com.justcalls.data.models.responses

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val displayName: String,
    val createdAt: String
)

