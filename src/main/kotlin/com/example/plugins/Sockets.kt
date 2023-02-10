package com.example.plugins

import com.example.User
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import io.ktor.websocket.serialization.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import java.io.Serial
import java.time.Duration
import java.util.Collections
import java.util.LinkedHashSet
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
    }
    routing {
        val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
        webSocket("/chat") {
            println("Adding user")
            val user = call.receive<User>()
            val thisConnection = Connection(user.name, this)
            connections.add(thisConnection)
            try {
                send("You are connected! there are ${connections.size} people in the chat")
//                for (frame in incoming) {
//                    frame as? Frame.Text ?: continue
//                    val receivedText = user.text
//                    val textWithUsername = "[${thisConnection.name}]: $receivedText"
//                    connections.forEach {
//                        if (it == thisConnection) {
//                            send("me: $receivedText")
//                        } else {
//                            it.session.send(textWithUsername)
//                        }
//                    }
//                }
                while (true) {
                    val incoming = receiveDeserialized<User>()
                    val textWithUsername = "[${thisConnection.name}]: ${incoming.text}"
                    connections.forEach {
                        if (it == thisConnection) {
                            send("me: ${incoming.text}")
                        } else {
                            it.session.send(textWithUsername)
                        }
                    }
                }
            } catch (e: Exception) {
                println(e.localizedMessage)
            } finally {
                println("remobing")
                connections.remove(thisConnection)
            }
        }
        val connection = Collections.synchronizedSet<Connection?>(LinkedHashSet())
        webSocket("/chats") {
            val user = receiveDeserialized<User>()
            val currentConnection = Connection(user.name, this)
            connection.add(currentConnection)
            try {
                sendSerialized("you're connected!")
                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val receivedText = frame.readText()
                    val textWithUsername = "[${currentConnection.name}]: $receivedText"
                    connections.forEach {
                        it.session.send(textWithUsername)
                    }
                }
            } catch (e: Exception) {
                println(e.localizedMessage)
            } finally {
                println("removing")
                connections.remove(currentConnection)
            }
        }
    }
}

data class Connection(val name: String, val session: DefaultWebSocketServerSession)