package com.justcalls.data.models.requests

import kotlinx.serialization.Serializable

@Serializable
data class SignUpRequest(
    val email: String,
    val guid: String? = null
)

