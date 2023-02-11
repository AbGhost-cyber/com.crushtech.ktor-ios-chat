package com.example

import io.ktor.network.sockets.*
import io.ktor.server.websocket.*
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Message(val name: String, val message: String)


data class Connection(val name: String, val session: DefaultWebSocketServerSession)