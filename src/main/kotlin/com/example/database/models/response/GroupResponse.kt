package com.example.database.models.response

import com.example.database.models.request.JoinGroupRequest
import kotlinx.serialization.Serializable

@Serializable
data class GroupResponse(
    val adminId: String,
    val groupId: String,
    val groupName: String,
    val groupDesc: String,
    val groupUrl: String,
    val dateCreated: Long,
    val users: List<String> = listOf(),
    var requests: List<JoinGroupRequest> = listOf(),
    val messages: List<MessageDTO> = listOf(),
    val id: String
)