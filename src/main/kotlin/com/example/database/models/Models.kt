package com.example.database.models

import com.example.database.models.request.GroupAcceptResponse
import com.example.database.models.request.JoinGroupRequest
import com.example.database.models.response.GroupResponse
import com.example.database.models.response.OutGoingMessage
import io.ktor.server.websocket.*
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId


data class Group(
    val adminId: String,
    val groupId: String,
    var groupIcon: String,
    val groupName: String,
    val groupDesc: String,
    val groupUrl: String,
    val dateCreated: Long,
    var users: List<String>,
    var requests: List<GroupJoinReq>,
    var messages: List<Message>,
    @BsonId val id: ObjectId = ObjectId()
) {
    fun toGroupResponse(isAdmin: Boolean): GroupResponse {
        return GroupResponse(
            groupId = this.groupId,
            groupName = this.groupName,
            groupIcon = this.groupIcon,
            groupDesc = this.groupDesc,
            groupUrl = this.groupUrl,
            dateCreated = this.dateCreated,
            users = this.users,
            requests = if (isAdmin) this.requests.map { it.toJoinGroupDTO() } else listOf(),
            messages = this.messages.map { it.toDTO() },
            id = this.id.toString()
        )
    }
}

data class GroupJoinReq(
    val username: String,
    val publicKey: List<Int>,
    @BsonId val id: ObjectId = ObjectId()
) {
    fun toJoinGroupDTO(): JoinGroupRequest {
        return JoinGroupRequest(publicKey)
    }
}

data class GroupAccept(
    val username: String,
    val groupId: String,
    val publicKey: List<Int>,
    @BsonId val id: ObjectId = ObjectId()
) {
    fun toDTO(): GroupAcceptResponse {
        return GroupAcceptResponse(username, groupId, publicKey)
    }
}

data class User(
    val username: String,
    val password: String,
    @BsonId val id: ObjectId = ObjectId(),
    val salt: String
)

data class Message(
    val name: String,
    val message: String,
    @BsonId val id: ObjectId = ObjectId()
) {
    fun toDTO(): OutGoingMessage {
        return OutGoingMessage(name, message)
    }
}

data class UserIds(
    var value: Int,
    @BsonId val id: ObjectId = ObjectId()
)

data class ActiveUser(val username: String, val session: DefaultWebSocketServerSession)

