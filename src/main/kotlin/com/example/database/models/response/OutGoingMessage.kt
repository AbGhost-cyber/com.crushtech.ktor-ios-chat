package com.example.database.models.response

import com.example.database.models.Message
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class OutGoingMessage(
    val name: String,
    val message: String,
    val id: String = UUID.randomUUID().toString()
    //TODO: add timestamp next
) {
    fun toDomain(): Message {
        return Message(name, message)
    }
}

@Serializable
data class IncomingMessage(val message: String, val groupId: String)
