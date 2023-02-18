package com.example.database.models.request

import com.example.database.models.GroupAccept
import kotlinx.serialization.Serializable

@Serializable
data class GroupAcceptResponse(val username: String, val groupId: String, val publicKey: List<Int>) {
    fun toDomain(): GroupAccept {
        return GroupAccept(username, groupId, publicKey)
    }
}