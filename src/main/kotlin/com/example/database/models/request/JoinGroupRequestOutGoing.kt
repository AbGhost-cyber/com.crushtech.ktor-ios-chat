package com.example.database.models.request

import com.example.database.models.GroupJoinReq
import kotlinx.serialization.Serializable

@Serializable
data class JoinGroupRequestOutGoing(val publicKey: List<Int>, val username: String) {
    fun toDomain() = GroupJoinReq(username, publicKey)
}

@Serializable
data class JoinGroupRequestIncoming(val publicKey: List<Int>, val groupId: String)