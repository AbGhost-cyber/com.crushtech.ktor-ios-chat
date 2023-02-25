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
import kotlinx.coroutines.isActive
import org.bson.types.ObjectId
import java.util.*


fun Route.groupChatSocket(chatService: ChatService) {
    authenticate {
        val people = Collections.synchronizedSet<ActiveUser?>(LinkedHashSet())
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

            if (user.username !in group.users) {
                return@webSocket sendSerialized(WebSocketResponse.SimpleResponse(message = "you must join the group to read messages"))
            }

            val activeUser = ActiveUser(user.username, session = this)
            try {
                chatService.addUserToActive(activeUser)
                //doesn't matter it's a set, only new values will be added
                sendSerialized(WebSocketResponse.SimpleResponse(message = "you're connected"))

                for (frame in incoming) {
                    (converter?.deserialize<IncomingMessage>(frame))?.let { incomingMsg ->
                        val fetchedGroup = chatService.getGroupById(groupId)
                            ?: return@webSocket sendSerialized(WebSocketResponse.SimpleResponse(message = "no such group!"))
                        val usersInGroups = chatService.getActiveUsers().filter { it.username in fetchedGroup.users }

                        val msgSentUser = chatService.getUserById(ObjectId(userId))
                            ?: return@webSocket sendSerialized("user doesn't exist")
                        val outGoingMessage = OutGoingMessage(msgSentUser.username, incomingMsg.message)
                        fetchedGroup.messages += outGoingMessage.toDomain()
                        fetchedGroup.updatedTime = System.currentTimeMillis()
                        chatService.upsertGroup(fetchedGroup)

                        println("users in group: $usersInGroups")
                        for (users in usersInGroups) {
                            if (users.session.isActive) {
                                val value = WebSocketResponse.SingleGroupResponse(
                                    groupResponse = fetchedGroup.toGroupResponse(
                                        isAdmin = fetchedGroup.adminId == userId
                                    )
                                )
                                users.session.sendSerialized(value)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                chatService.removeUserFromActive(activeUser.username)
            }
        }
    }
}