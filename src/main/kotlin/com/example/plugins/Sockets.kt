package com.example.plugins

import com.example.Connection
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
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
                    send("You're connected, ${users.count()} people in chat")
                    users.forEach {
                        it.session.send("$userName joined the chat!")
                    }
                    for (frame in incoming) {
                        val message = frame as? Frame.Text ?: continue
                        users.forEach {
                            val sentText = "[$userName]: ${message.readText()}"
                            if (it == thisConnection) {
                                it.session.send("me: ${message.readText()}")
                            } else {
                                it.session.send(sentText)
                            }
                        }
                    }
                }
            } catch (e:Exception) {
                println(e.localizedMessage)
            } finally {
                println("removing user")
                users -=thisConnection
            }
        }
    }
}