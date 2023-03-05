package com.routes

import com.example.database.ChatService
import com.example.database.models.request.JoinGroupRequestIncoming
import com.example.database.models.request.JoinGroupRequestOutGoing
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bson.types.ObjectId


fun Route.joinGroupRoute(chatService: ChatService) {
    route("/join/group") {
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
                val request = kotlin.runCatching { call.receiveNullable<JoinGroupRequestIncoming>() }.getOrNull()
                    ?: kotlin.run { return@post call.respond(HttpStatusCode.BadRequest) }

                val group = chatService.getGroupById(request.groupId)
                    ?: return@post call.respond(
                        HttpStatusCode.Conflict, "group doesn't exist"
                    )

                val isInGroup = group.users.find { it == user.username } != null
                if (isInGroup) return@post call.respond(
                    HttpStatusCode.Conflict,
                    "unable to perform action because you're in the group already."
                )
                if (group.requests.none { it.username == user.username }) {
                    val domainReq = JoinGroupRequestOutGoing(request.publicKey, user.username).toDomain()
                    group.requests += domainReq
                }
                val wasAcknowledged = chatService.upsertGroup(group)
                if (!wasAcknowledged) {
                    return@post call.respond(HttpStatusCode.Conflict, "couldn't join group, please try again later")
                }
                call.respond(HttpStatusCode.OK, "request sent, you'll be notified if accepted")
            }
        }
    }
}

