package com.justcalls.data.models.requests

import kotlinx.serialization.Serializable

@Serializable
data class CompleteRequest(
    val email: String,
    val guid: String,
    val code: String,
    val password: String
)

