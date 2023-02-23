package com.example.database.models.response

import com.example.database.models.Message
import kotlinx.serialization.Serializable

@Serializable
data class OutGoingMessage(
    val name: String,
    val message: String,
    val id: String
    //TODO: add timestamp next
) {
    fun toDomain(): Message {
        return Message(name, message)
    }
}

@Serializable
data class IncomingMessage(val message: String)
