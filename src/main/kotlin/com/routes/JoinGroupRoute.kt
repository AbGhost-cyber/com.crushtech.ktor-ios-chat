package com.routes

import com.example.database.ChatService
import com.example.database.models.request.JoinGroupRequest
import com.example.database.models.response.WebSocketResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import org.bson.types.ObjectId


fun Route.joinGroupRoute(chatService: ChatService) {
    route("/join/group/{id}") {
        authenticate {
            post {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.getClaim("userId", String::class)
                    ?: return@post call.respond(
                        HttpStatusCode.Conflict, "seems you're not authorized"
                    )

                val user = chatService.getUserById(ObjectId(userId))
                    ?: return@post call.respond(
                        HttpStatusCode.Conflict, "user doesn't exist"
                    )

                val groupId = call.parameters["id"]
                    ?: return@post call.respond(
                        HttpStatusCode.Conflict, "group id must be provided"
                    )

                val group = chatService.getGroupById(groupId)
                    ?: return@post call.respond(
                        HttpStatusCode.Conflict, "group doesn't exist"
                    )

                val request = kotlin.runCatching { call.receiveNullable<JoinGroupRequest>() }.getOrNull()
                    ?: kotlin.run { return@post call.respond(HttpStatusCode.BadRequest) }

                val isPendingOrIsInGroup = group.requests.find { it.username == user.username } != null
                if (isPendingOrIsInGroup) return@post call.respond(
                    HttpStatusCode.Conflict,
                    "unable to perform action because you're in the group already or you previously sent a request."
                )
                group.requests += request.toDomain(username = user.username)
                val wasAcknowledged = chatService.upsertGroup(group)
                if (!wasAcknowledged) {
                    return@post call.respond(HttpStatusCode.Conflict, "couldn't join group, please try again later")
                }
                call.respond(HttpStatusCode.OK, "request sent, you'll be notified if accepted")

                //try to notify admin if online
                val admin = chatService.getUserById(ObjectId(group.adminId))
                    ?: return@post
                val adminSocket = chatService.getActiveUserByName(admin.username)
                    ?: return@post
                adminSocket.session.sendSerialized(
                    WebSocketResponse.NotificationResponse("${user.username} want's to join your group")
                )
            }
            get {
                val principal = call.principal<JWTPrincipal>()

                val userId = principal?.getClaim("userId", String::class)
                    ?: return@get call.respond(
                        HttpStatusCode.Conflict, "seems you're not authorized"
                    )
                val groupId = call.parameters["id"]
                    ?: return@get call.respond(
                        HttpStatusCode.Conflict, "group id must be provided"
                    )

                val group = chatService.getGroupById(groupId)
                    ?: return@get call.respond(
                        HttpStatusCode.Conflict, "group doesn't exist"
                    )
                if (userId != group.adminId) {
                    return@get call.respond(
                        HttpStatusCode.Conflict, "admin only operation"
                    )
                }
                call.respond(HttpStatusCode.OK, group.requests.map { it.toJoinGroupDTO() })
            }
        }
    }
}

