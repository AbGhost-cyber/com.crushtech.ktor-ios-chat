package com.routes

import com.example.database.ChatService
import com.example.database.models.request.GroupAcceptResponse
import com.example.database.models.response.OutGoingMessage
import com.example.database.models.response.WebSocketResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.isActive


//would be great to send a notification, no need to notify user that the request was denied
fun Route.adminAcceptUserRoute(chatService: ChatService) {
    authenticate {
        post("/group/{id}/{of}/acceptRequest") {
            val groupId = call.parameters["id"] ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                "must provide the group id"
            )
            val userToBeAdded = call.parameters["of"]
                ?: return@post call.respond(HttpStatusCode.BadRequest, "username to be added must not be empty")

            val credentials = kotlin.runCatching { call.receiveNullable<GroupAcceptResponse>() }.getOrNull()
                ?: kotlin.run { return@post call.respond(HttpStatusCode.BadRequest, "must have credentials") }

            val principal = call.principal<JWTPrincipal>()

            val userId = principal?.getClaim("userId", String::class)
                ?: return@post call.respond(HttpStatusCode.Conflict, "seems you're not authorized")

            val group = chatService.getGroupById(groupId)
                ?: return@post call.respond(HttpStatusCode.Conflict, "No such group")

            val otherUser = chatService.getUserByName(userToBeAdded)
                ?: return@post call.respond(HttpStatusCode.Conflict, "user doesn't exist")

            if (group.adminId != userId) return@post call.respond(
                HttpStatusCode.Conflict, "only an admin can perform this action"
            )

            if (userToBeAdded in group.users) {
                return@post call.respond(HttpStatusCode.Conflict, "user is already a group member")
            }

            val userJoinReq = group.requests.find { it.username == userToBeAdded }
                ?: return@post call.respond(HttpStatusCode.Conflict, "this user never sent a group join request")

            group.requests -= userJoinReq
            group.users += userJoinReq.username
            val outGoingMessage = OutGoingMessage("", "${userJoinReq.username} was added to the group by admin")
            group.messages += outGoingMessage.toDomain()
            group.updatedTime = System.currentTimeMillis()
            chatService.upsertGroup(group)

            val usersInGroups = chatService.getActiveUsers().filter { it.username in group.users }

            for (user in usersInGroups) {
                val localUser = chatService.getUserByName(user.username) ?: continue
                if (user.session.isActive) {
                    val value = WebSocketResponse.SingleGroupResponse(
                        groupResponse = group.toGroupResponse(
                            isAdmin = group.adminId == localUser.id.toString()
                        )
                    )
                    user.session.sendSerialized(value)
                }
            }


            //persist admin encrypted response in user's collection, it can't be decrypted by anyone except the user
            chatService.upsertUserEncryptedGKey(credentials.toDomain())
            //success
            call.respond(HttpStatusCode.OK, "User added to group")

            val activeUser = chatService.getActiveUserByName(userToBeAdded) ?: return@post
            //send group secret to user
            activeUser.session.sendSerialized(
                WebSocketResponse
                    .UserJoinAcceptAdmin(accept = credentials)
            )
            //update user's groups
            activeUser.session.sendSerialized(
                WebSocketResponse
                    .SingleGroupResponse(
                        groupResponse = group.toGroupResponse(
                            isAdmin = group.adminId == otherUser.id.toString()
                        )
                    )
            )
        }
    }
}