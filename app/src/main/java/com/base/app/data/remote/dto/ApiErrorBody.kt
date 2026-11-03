package com.base.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Body lỗi phẳng của BE dùng envelope [BaseResponse]. Mọi field nullable: body lỗi có thể
 * rỗng, là HTML của proxy, hoặc thiếu field — parse hỏng thì vẫn còn HTTP status.
 */
@Serializable
data class ApiErrorBody(
    @SerialName("statusCode") val statusCode: Int? = null,
    @SerialName("message") val message: String? = null,
    @SerialName("errorCode") val errorCode: String? = null,
)
