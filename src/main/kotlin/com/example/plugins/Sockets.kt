package com.example.plugins

import com.example.Connection
import com.example.Message
import io.ktor.serialization.*
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.Json
import java.time.Duration
import java.util.*

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
    }
    routing {
        val users = Collections.synchronizedSet<Connection?>(LinkedHashSet())

        webSocket("/leave{username?}") {
            val userName = call.parameters["username"]
                ?: return@webSocket call.respond("must have a name")
            if (users.any { it.name == userName }) {
                try {
                    val thisUser = users.find { it.name == userName }!!
                    users -= thisUser
                    users.forEach {
                        it.session.sendSerialized(Message(message = "${thisUser.name} left the chat"))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        webSocket("/chat{username?}") {
            val userName = call.parameters["username"]
                ?: return@webSocket call.respond("must have a name")
            val thisConnection = Connection(userName, this)
            try {
                if (userName.isBlank() || userName.isEmpty() || userName == "") return@webSocket print("invalid $userName")
                val isInGroupAlready = users.any { it.name == userName }
                if (!isInGroupAlready) {
                    users.add(thisConnection)
                }

                if (thisConnection.session.isActive) {
                    sendSerialized(Message(message = "welcome to the chat!, there are ${users.count()} people in the chat"))
                    users.forEach {
                        it.session.sendSerialized(Message(message = "$userName joined the chat!"))
                    }
                    for (frame in incoming) {
                        users.forEach {
                            val incomingMessage = it.session.converter?.deserialize<Message>(frame)
                            it.session.sendSerialized(incomingMessage)
                        }
                    }
                }
            } catch (e: Exception) {
                println(e.localizedMessage)
            } finally {
                println("removing user ${thisConnection.name}")
                users -= thisConnection
            }
        }
    }
}