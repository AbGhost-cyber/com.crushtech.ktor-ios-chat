package com.routes

import com.example.database.ChatService
import com.example.database.models.Group
import com.example.database.models.request.CreateGroupRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bson.types.ObjectId


fun Route.groupRoute(chatService: ChatService) {
    route("/group") {
        authenticate {
            post {
                val request = kotlin.runCatching { call.receiveNullable<CreateGroupRequest>() }.getOrNull()
                    ?: kotlin.run {
                        return@post call.respond(HttpStatusCode.BadRequest, "invalid request")
                    }
                val principal = call.principal<JWTPrincipal>()

                val userId = principal?.getClaim("userId", String::class)
                    ?: return@post call.respond(HttpStatusCode.Conflict, "seems you're not authorized")

                val admin = chatService.getUserById(ObjectId(userId))
                    ?: return@post call.respond(HttpStatusCode.Conflict, "user doesn't exist")
                val groupId = chatService.getIdForGroup()
                val groupUrl = "http://0.0.0.0:8081/group/$groupId"
                val newGroup = Group(
                    adminId = userId,
                    groupId = groupId.toString(),
                    groupIcon = request.groupIcon,
                    groupName = request.groupName,
                    groupUrl = groupUrl,
                    groupDesc = request.groupDesc,
                    dateCreated = System.currentTimeMillis(),
                    users = listOf(admin.username),
                    requests = listOf(),
                    messages = listOf()
                )
                if (chatService.upsertGroup(newGroup)) {
                    return@post call.respond(
                        HttpStatusCode.Created,
                        "group was successfully created"
                    )
                }
                return@post call.respond(
                    HttpStatusCode.Conflict,
                    "an unknown error occurred, please try again "
                )
            }
        }
        authenticate {
            get {
                val principal = call.principal<JWTPrincipal>()

                val userId = principal?.getClaim("userId", String::class)
                    ?: return@get call.respond(HttpStatusCode.Conflict, "seems you're not authorized")

                val user = chatService.getUserById(ObjectId(userId))
                    ?: return@get call.respond(HttpStatusCode.Conflict, "user doesn't exist")

                val groups = chatService.getUserGroups(user.username)
                val userGroupsDTO = groups.map { it.toGroupResponse(isAdmin = userId == it.adminId) }
                call.respond(HttpStatusCode.OK, userGroupsDTO)
            }
        }
        authenticate {
            get("{id}") {
                val principal = call.principal<JWTPrincipal>()

                val userId = principal?.getClaim("userId", String::class)
                    ?: return@get call.respond(HttpStatusCode.Conflict, "seems you're not authorized")

                val user = chatService.getUserById(ObjectId(userId))
                    ?: return@get call.respond(HttpStatusCode.Conflict, "user doesn't exist")

                val groupId = call.parameters["id"]
                    ?: return@get call.respond(HttpStatusCode.Conflict, "no group id provided")

                val group = chatService.getGroupById(groupId)
                    ?: return@get call.respond(HttpStatusCode.Conflict, "group is invalid")

                val userIsGroupMember = group.users.contains(user.username)


                if (userIsGroupMember) {
                    return@get call.respond(
                        HttpStatusCode.OK,
                        group.toGroupResponse(
                            isAdmin = userId == group.adminId
                        )
                    )
                }
                return@get call.respond(
                    HttpStatusCode.BadRequest,
                    "this is a private group, to view group info please use the search functionality"
                )
            }
        }
    }
}