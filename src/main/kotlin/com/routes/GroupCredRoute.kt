package com.routes

import com.example.database.ChatService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
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
                val userGroups = chatService.getUserGroups(user.username)
                val userGroupIds =  userGroups.map { group -> group.groupId }
                val requestGroupIds = credentials.map { it.groupId }
                val commonIds = userGroupIds.intersect(requestGroupIds.toSet())

                //delete credential if user has joined group
                if(commonIds.isNotEmpty()) {
                    commonIds.forEach { id->
                        chatService.deleteUserEncryptedGroupKey(groupId = id)
                    }
                }
                val credentialsToSend = chatService.getUserEncryptedGroupKeys(user.username)
                    .map { it.toDTO() }
                call.respond(HttpStatusCode.OK, credentialsToSend)
            }
        }
    }
}