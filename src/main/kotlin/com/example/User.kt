package com.example

import io.ktor.server.websocket.*
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Message(
    val name: String? = null,
    val message: String,
    val id: String = UUID.randomUUID().toString()
)

data class Connection(val name: String, val session: DefaultWebSocketServerSession)