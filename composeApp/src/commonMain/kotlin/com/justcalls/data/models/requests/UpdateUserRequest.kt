package com.justcalls.data.models.requests

import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserRequest(
    val displayName: String
)

