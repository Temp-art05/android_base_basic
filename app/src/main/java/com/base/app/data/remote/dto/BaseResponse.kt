package com.base.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BaseResponse<T>(
    @SerialName("statusCode")
    val statusCode: Int,

    @SerialName("message")
    val message: String,

    @SerialName("data")
    val data: T? = null,

    @SerialName("timestamp")
    val timestamp: Long? = null,

    @SerialName("correlationId")
    val correlationId: String? = null,

    @SerialName("errorCode")
    val errorCode: String? = null
) {
    val isSuccess: Boolean get() = statusCode in 200..299
}
