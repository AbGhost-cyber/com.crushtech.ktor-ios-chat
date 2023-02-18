package com.routes.sockets

import com.example.database.ChatService
import com.example.database.models.ActiveUser
import com.example.database.models.request.JoinGroupRequest
import io.ktor.serialization.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import org.bson.types.ObjectId


fun Route.joinGroupSocket(chatService: ChatService) {
    authenticate {
        //admin call also join this socket to get request in real time
        webSocket("/join/group/{id}") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)
                ?: return@webSocket sendSerialized("seems you're not authorized")

            val user = chatService.getUserById(ObjectId(userId))
                ?: return@webSocket sendSerialized("user doesn't exist")

            val groupId = call.parameters["id"]
                ?: return@webSocket sendSerialized("group id must be provided")

            val group = chatService.getGroupById(groupId)
                ?: return@webSocket sendSerialized("group doesn't exist")

            val activeUser = chatService.getActiveUserByName(user.username)
                ?: ActiveUser(user.username, session = this)

            try {
                //we call the add online users regardless, it will only add new ones
                chatService.addUserToActive(activeUser)

                //check if user is admin on join, then send recent group request if any
                if (userId == group.adminId) {
                    //this might be good as a notification
                    if (group.requests.isNotEmpty()) {
                        activeUser.session.sendSerialized(
                            "you have ${group.requests.count()} new group request, check them out!"
                        )
                    }
                }
                //listen for incoming data frame
                for (frame in incoming) {
                    val incomingRequest = converter?.deserialize<JoinGroupRequest>(frame) ?: continue
                    val isPendingOrIsInGroup =
                        group.requests.find { it.username == incomingRequest.username } != null || group.users.contains(
                            incomingRequest.username
                        )
                    if (isPendingOrIsInGroup) return@webSocket sendSerialized(
                        "unable to perform action because you're in the group already or you previously sent a request."
                    )
                    if (chatService.getUserByName(incomingRequest.username) == null || user.username != incomingRequest.username) {
                        return@webSocket sendSerialized("username is incorrect, please try again")
                    }
                    group.requests += incomingRequest.toDomain()
                    val wasAcknowledged = chatService.upsertGroup(group)
                    if (!wasAcknowledged) {
                        return@webSocket sendSerialized("couldn't join group, please try again later")
                    }
                    val admin = chatService.getUserById(ObjectId(group.adminId))
                        ?: return@webSocket
                    val adminSocket = chatService.getActiveUserByName(admin.username)
                        ?: return@webSocket

                    if (activeUser.username == incomingRequest.username) {
                        sendSerialized("request sent, please await admin response")
                    }

                    adminSocket.session.sendSerialized(
                        "${incomingRequest.username} want's to join your group"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                println("removing user from online status")
                chatService.removeUserFromActive(activeUser.username)
            }
        }
    }
}