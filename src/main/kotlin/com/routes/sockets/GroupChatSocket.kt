package com.routes.sockets

import com.example.database.ChatService
import com.example.database.models.ActiveUser
import com.example.database.models.response.IncomingMessage
import com.example.database.models.response.OutGoingMessage
import com.example.database.models.response.WebSocketResponse
import io.ktor.serialization.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import org.bson.types.ObjectId
import java.util.*


fun Route.groupChatSocket(chatService: ChatService) {
    authenticate {
        webSocket("/group/{id}/chat") {
            val principal = call.principal<JWTPrincipal>()

            val userId = principal?.getClaim("userId", String::class)
                ?: return@webSocket sendSerialized(WebSocketResponse.SimpleResponse(message = "no token provided!"))

            val user = chatService.getUserById(ObjectId(userId))
                ?: return@webSocket sendSerialized(WebSocketResponse.SimpleResponse(message = "user doesn't exist!"))

            val groupId = call.parameters["id"]
                ?: return@webSocket sendSerialized(WebSocketResponse.SimpleResponse(message = "group id is required!"))

            val group = chatService.getGroupById(groupId)
                ?: return@webSocket sendSerialized(WebSocketResponse.SimpleResponse(message = "group doesn't exist!"))

            val activeUser = ActiveUser(user.username, session = this)
            try {
                //doesn't matter it's a set, only new values will be added
                sendSerialized(WebSocketResponse.SimpleResponse(message = "you're connected to ${group.groupName}"))
                chatService.addUserToActive(activeUser)

                for (frame in incoming) {
                    val incomingMessage = converter?.deserialize<IncomingMessage>(frame) ?: continue
                    val msgSentUser = chatService.getUserById(ObjectId(userId))
                        ?: return@webSocket sendSerialized("user doesn't exist")
                    val usersInGroups = chatService.getActiveUsers().filter { it.username in group.users }
                    val outGoingMessage = OutGoingMessage(
                        msgSentUser.username,
                        incomingMessage.message,
                        UUID.randomUUID().toString()
                    )
                    group.messages += outGoingMessage.toDomain()

                    usersInGroups.forEach {
                        it.session.sendSerialized(
                            WebSocketResponse.SingleGroupResponse(
                                groupResponse = group.toGroupResponse(
                                    isAdmin = group.adminId == msgSentUser.id.toString()
                                )
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}