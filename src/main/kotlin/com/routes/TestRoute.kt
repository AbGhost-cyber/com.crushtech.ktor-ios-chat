package com.routes

import com.example.database.models.ActiveUser
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import java.util.*

val onlineUsers = Collections.synchronizedSet<ActiveUser?>(LinkedHashSet())
fun Route.testJwt() {
    val items = mutableListOf("the", "asok")
    post("secrett") {
        items += "asake"
        call.respond(HttpStatusCode.OK, onlineUsers.count())
        onlineUsers.forEach {
            it.session.sendSerialized("$items")
        }
        call.respond(HttpStatusCode.OK, "sent")
    }
    webSocket("secret") {
        val active = ActiveUser("Ab", this)
        try {
            onlineUsers += active
            sendSerialized("$items")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        for (frame in incoming) {
            sendSerialized("frame received: $frame")
        }
    }
}