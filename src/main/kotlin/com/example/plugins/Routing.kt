package com.example.plugins // ktlint-disable filename

import com.example.database.ChatService
import com.example.database.models.CreateGroupRequest
import com.example.database.models.Group
import com.example.database.models.User
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Conflict
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(service: ChatService) {
    routing {
        route("/group") {
            post {
                val request = try {
                    call.receive<CreateGroupRequest>()
                } catch (e: ContentTransformationException) {
                    e.printStackTrace()
                    return@post call.respond(BadRequest)
                }
                val admin = service.getUserById(request.userId)
                    ?: return@post call.respond(
                        BadRequest,
                        "User doesn't exist"
                    )

                val groupId = service.getIdForGroup()
                val groupUrl = "http://0.0.0.0:8081/group/$groupId"

                //TODO, let admin create a group together with
                val newGroup = Group(
                    adminId = request.userId,
                    groupId = groupId.toString(),
                    groupName = request.name,
                    groupUrl = groupUrl,
                    groupDesc = request.desc,
                    dateCreated = System.currentTimeMillis(),
                    users = listOf(admin),
                    requests = listOf()
                )
                if (service.createGroup(newGroup)) {
                    return@post call.respond(
                        Created,
                        "group was successfully created"
                    )
                }
                return@post call.respond(
                    Conflict,
                    "an unknown error occurred, please try again "
                )
            }
            get("/all") {
                val request = try {
                    call.receive<User>()
                } catch (e: ContentTransformationException) {
                    e.printStackTrace()
                    return@get call.respond(BadRequest)
                }
                val user = service.getUserByName(request.username)
                    ?: return@get call.respond(
                        BadRequest,
                        "User doesn't exist"
                    )
                if (user.password != request.password) {
                    return@get call.respond(BadRequest, "Wrong password")
                }
                call.respond(OK, service.getUserGroups(user))
            }
            get("{id}") {

            }
        }
    }
}
