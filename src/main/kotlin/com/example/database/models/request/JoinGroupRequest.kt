package com.example.database.models.request

import com.example.database.models.GroupJoinReq
import kotlinx.serialization.Serializable

@Serializable
data class JoinGroupRequest(val username: String, val publicKey: List<Int>) {
    fun toDomain(): GroupJoinReq {
        return GroupJoinReq(username, publicKey)
    }
}