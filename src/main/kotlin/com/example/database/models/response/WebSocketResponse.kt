package com.example.database.models.response

import com.example.database.models.request.GroupAcceptResponse
import com.example.database.models.request.JoinGroupRequest
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


@Serializable
sealed class WebSocketResponse<T>(
    val type: Int,
    val data: T? = null
) {


    @Serializable
    data class NotificationResponse(@Transient val message: String? = null) :
        WebSocketResponse<String>(type = 0, data = message)

    @Serializable
    data class SingleGroupResponse(@Transient val groupResponse: GroupResponse? = null) :
        WebSocketResponse<GroupResponse>(type = 1, data = groupResponse)

    @Serializable
    data class SimpleResponse(@Transient val message: String = "") : WebSocketResponse<String>(type = 2, data = message)

    @Serializable
    data class UserJoinAcceptAdmin(@Transient val accept: GroupAcceptResponse? = null) :
        WebSocketResponse<GroupAcceptResponse>(type = 3, data = accept)

    @Serializable
    data class GroupJoinResponseUser(@Transient val request: JoinGroupRequest? = null) :
        WebSocketResponse<JoinGroupRequest>(type = 4, data = request)
}