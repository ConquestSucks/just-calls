package com.justcalls.data.models.responses

import kotlinx.serialization.Serializable

@Serializable
data class SignInResponse(
    val userId: String,
    val accessToken: String
)

