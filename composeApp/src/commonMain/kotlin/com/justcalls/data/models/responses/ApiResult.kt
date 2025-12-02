package com.justcalls.data.models.responses

import kotlinx.serialization.Serializable

@Serializable
data class ApiResult<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ApiError? = null
)

@Serializable
data class ApiError(
    val code: String,
    val message: String
)

