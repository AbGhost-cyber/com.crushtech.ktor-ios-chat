package com.routes

import com.example.database.ChatService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*


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

            val principal = call.principal<JWTPrincipal>()

            val userId = principal?.getClaim("userId", String::class)
                ?: return@post call.respond(HttpStatusCode.Conflict, "seems you're not authorized")

            val group = chatService.getGroupById(groupId)
                ?: return@post call.respond(HttpStatusCode.Conflict, "No such group")

            chatService.getUserByName(userToBeAdded)
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
            chatService.upsertGroup(group)

            //update user's groups if user is online
            val getAddedUserGroups = chatService.getUserGroups(userToBeAdded).map {
                it.toGroupResponse(it.adminId == userId)
            }
            val activeUser = chatService.getActiveUserByName(userToBeAdded) ?: return@post
            //send group secret to user
            activeUser.session.sendSerialized(credentials)
            //update user's groups
            activeUser.session.sendSerialized(getAddedUserGroups)
        }
    }
}