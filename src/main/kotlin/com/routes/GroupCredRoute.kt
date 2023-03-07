package com.routes

import com.example.database.ChatService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bson.types.ObjectId


fun Route.fetchGroupCred(chatService: ChatService) {
    authenticate {
        route("/fetchGroupCred") {
            get {
                val principal = call.principal<JWTPrincipal>()

                val userId = principal?.getClaim("userId", String::class)
                    ?: return@get call.respond(HttpStatusCode.Conflict, "seems you're not authorized")

                val user = chatService.getUserById(ObjectId(userId))
                    ?: return@get call.respond(HttpStatusCode.Conflict, "user doesn't exist")

                val credentials = chatService.getUserEncryptedGroupKeys(user.username)
                call.respond(HttpStatusCode.OK, credentials.map { it.toDTO() })
            }
        }
        route("/deleteGroupCred") {
            post {
                val principal = call.principal<JWTPrincipal>()

                val userId = principal?.getClaim("userId", String::class)
                    ?: return@post call.respond(HttpStatusCode.Conflict, "seems you're not authorized")

                val user = chatService.getUserById(ObjectId(userId))
                    ?: return@post call.respond(HttpStatusCode.Conflict, "user doesn't exist")

                val request = kotlin.runCatching { call.receiveNullable<List<String>>() }.getOrNull()
                    ?: kotlin.run { return@post call.respond(HttpStatusCode.BadRequest) }

                val credentials = chatService.getUserEncryptedGroupKeys(user.username)
                    .filter { it.groupId in request }

                for (credential in credentials) {
                    chatService.deleteUserEncryptedGroupKey(credential.groupId)
                }
                call.respond(HttpStatusCode.OK, "cleared credentials")
            }
        }
    }
}