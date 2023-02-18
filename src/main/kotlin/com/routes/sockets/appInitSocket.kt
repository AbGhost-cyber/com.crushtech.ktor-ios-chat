package com.routes.sockets

import com.example.database.ChatService
import com.example.database.models.ActiveUser
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.isActive
import org.bson.types.ObjectId


fun Route.appInitSocket(chatService: ChatService) {
    authenticate {
        webSocket("connect") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)
                ?: return@webSocket call.respond(HttpStatusCode.BadRequest)

            val user = chatService.getUserById(ObjectId(userId))
                ?: return@webSocket sendSerialized("user doesn't exist")
            val activeUser = ActiveUser(user.username, this)

            try {
                chatService.addUserToActive(activeUser)
                if (activeUser.session.isActive) {
                    sendSerialized("welcome back online ${activeUser.username}!")
                }
                for (frame in incoming) {
                    //keep alive
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}