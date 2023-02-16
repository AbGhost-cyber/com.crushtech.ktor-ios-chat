package com.example.database.models

import io.ktor.server.websocket.*
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Group(
    val adminId: String,
    val groupId: String,
    val groupName: String,
    val groupDesc: String,
    val groupUrl: String,
    val dateCreated: Long,
    val users: List<User>,
    var requests: List<User>,
    //for kmongo smh 🤡
    val _id: String = UUID.randomUUID().toString()
)

@Serializable
data class CreateGroupRequest(val name: String, val desc: String, val userId: String)

@Serializable
data class JoinGroupRequest(val user: User, val publicKey: String)

@Serializable
data class FetchUserGroupsRequest(val name: String, val password: String)

@Serializable
data class User(
    val username: String,
    val password: String,
    val userId: String, val _id: String = UUID.randomUUID().toString()
)

@Serializable
data class Message(
    val name: String? = null,
    val message: String,
    val id: String = UUID.randomUUID().toString()
)

@Serializable
data class UserIds(
    val userId: String,
    var value: Int,
    val _id: String = UUID.randomUUID().toString()
)

data class ActiveUsers(val userId: String, val session: DefaultWebSocketServerSession)

