package com.example.database.models.response

import com.example.database.models.request.JoinGroupRequestOutGoing
import kotlinx.serialization.Serializable

@Serializable
data class GroupResponse(
    val groupId: String,
    val groupIcon: String,
    val groupName: String,
    val groupDesc: String,
    val groupUrl: String,
    val dateCreated: Long,
    val users: List<String>,
    var requests: List<JoinGroupRequestOutGoing>,
    val messages: List<OutGoingMessage>,
    val updatedTime: Long,
    val adminName: String,
    val currentUserIsAdmin: Boolean,
    val id: String
)

