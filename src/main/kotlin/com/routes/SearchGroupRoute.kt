package com.routes

import com.example.database.ChatService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bson.types.ObjectId

fun Route.searchGroupRoute(chatService: ChatService) {
    authenticate {
        route("/groups/search") {
            get {
                val principal = call.principal<JWTPrincipal>()

                val userId = principal?.getClaim("userId", String::class)
                    ?: return@get call.respond(HttpStatusCode.Conflict, "seems you're not authorized")

                val user = chatService.getUserById(ObjectId(userId))
                    ?: return@get call.respond(HttpStatusCode.Conflict, "user doesn't exist")

                val keyword = call.request.queryParameters["keyword"]
                    ?: return@get call.respond(HttpStatusCode.Conflict, "no group id provided")
                //TODO: scope to users groups and global search groups
                val groups = chatService.fetchAllGroups()
                    .filter { it.groupName.contains(keyword) }
                call.respond(HttpStatusCode.OK, groups.map { it.toSearchGroupDTO() })
            }
        }
    }
}