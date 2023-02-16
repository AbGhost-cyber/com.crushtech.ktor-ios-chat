//package com.example.plugins
//
//import com.example.Connection
//import com.example.Message
//import com.example.database.*
//import io.ktor.http.*
//import io.ktor.serialization.*
//import io.ktor.serialization.kotlinx.*
//import io.ktor.server.application.*
//import io.ktor.server.request.*
//import io.ktor.server.response.*
//import io.ktor.server.routing.*
//import io.ktor.server.websocket.*
//import kotlinx.coroutines.isActive
//import kotlinx.serialization.json.Json
//import java.time.Duration
//import java.util.*
//
//fun Application.configureSockets() {
//    install(WebSockets) {
//        pingPeriod = Duration.ofSeconds(15)
//        timeout = Duration.ofSeconds(15)
//        maxFrameSize = Long.MAX_VALUE
//        masking = false
//        contentConverter = KotlinxWebsocketSerializationConverter(Json)
//    }
//    routing {
//        val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
//        val groups = mutableListOf<Group>()
//        val users = mutableListOf<User>()
//        users.add(User("Ab", "1234"))
//
//
//        route("/group") {
//            post {
//                val request = try {
//                    call.receive<CreateGroupRequest>()
//                } catch (e: ContentTransformationException) {
//                    e.printStackTrace()
//                    return@post call.respond(HttpStatusCode.BadRequest)
//                }
//                val user =
//                    users.find { it.userId == request.userId } ?: return@post call.respond("this user doesn't exist")
//                val groupId = shortUUID()
//                println("group url: https://localhost/group/$groupId")
//                val newGroup = Group(
//                    adminId = user.userId,
//                    groupId = groupId,
//                    groupName = request.name,
//                    groupUrl = " http://0.0.0.0:8081/group/$groupId",
//                    groupDesc = request.desc,
//                    dateCreated = System.currentTimeMillis(),
//                    users = listOf(user),
//                    requests = listOf()
//                )
//                groups.add(newGroup)
//                call.respond(HttpStatusCode.OK, "Created group ✅")
//            }
//            get {
//                call.respond(HttpStatusCode.OK, groups.count())
//            }
//            get("{id}") {
//                val groupId = call.parameters["id"] ?: return@get call.respond("no id passed")
//                val group = groups.find { it.groupId == groupId } ?: return@get call.respond("no such group")
//                call.respond(group.groupName)
//            }
//        }
//
//        route("join/group/{id}") {
//            post {
//                val groupId = call.parameters["id"] ?: return@post call.respond("must have an id")
//                val group = groups.firstOrNull { it.groupId == groupId } ?: return@post call.respond("No such group")
//                val request = try {
//                    call.receive<JoinGroupRequest>()
//                } catch (e: ContentTransformationException) {
//                    return@post call.respond(HttpStatusCode.BadRequest)
//                }
//                group.requests += request.user
//                val index = groups.indexOf(group)
//                groups[index] = group
//                call.respond(HttpStatusCode.OK, "request sent, please await admin response")
//            }
//        }
//        webSocket("join/groups/{id}") {
//            val groupId = call.parameters["id"] ?: return@webSocket sendSerialized("group id must be provided")
//            val group = groups.firstOrNull { it.groupId == groupId } ?: return@webSocket sendSerialized("No such group")
//            val adminId = call.request.queryParameters["admin"]
//            val adminConnection = if (adminId != null) {
//                Connection(adminId, this)
//            } else null
//            val connection = Connection("user", this)
//            connections.add(connection)
//            adminConnection?.let {
//                connections.add(it)
//                println("added")
//                if (group.requests.isNotEmpty()) {
//                    it.session.sendSerialized("${group.requests.count()} new group requests")
//                }
//            }
//            for (frame in incoming) {
//                val incomingRequest = converter?.deserialize<JoinGroupRequest>(frame) ?: continue
//                group.requests += incomingRequest.user
//                val index = groups.indexOf(group)
//                groups[index] = group
////                connections.find { it.name == adminId }?.session
////                    ?.sendSerialized("${group.requests.count()} new group requests")
//                println("called")
//                connections.forEach {
//                    if (it.name == group.adminId) {
//                        it.session.sendSerialized("fuck off")
//                        println("sent")
//                    } else if (it == connection) {
//                        it.session.sendSerialized("request sent, please await admin response")
//                    }
//                }
//            }
//        }
//
//        webSocket("group/channel/admin/{id}/group/{groupId}") {
//            val adminId = call.parameters["id"] ?: return@webSocket sendSerialized("id must be provided")
//            val groupId = call.parameters["groupId"] ?: return@webSocket sendSerialized("group id must be provided")
//            if (groups.none { it.adminId == adminId }) return@webSocket sendSerialized("only admin can receive notifications")
//            val group = groups.find { it.adminId == adminId && it.groupId == groupId }
//                ?: return@webSocket sendSerialized("you don't have any group")
//            val connection = Connection("Ab", this)
//            try {
//                connections.add(connection)
//                if (connection.session.isActive) {
//                    sendSerialized("${group.requests.count()} new group requests")
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            } finally {
//                println("removed admin")
//                connections -= connection
//            }
//        }
//
//        webSocket("/leave{username?}") {
//            val userName = call.parameters["username"]
//                ?: return@webSocket call.respond("must have a name")
//            if (connections.any { it.name == userName }) {
//                try {
//                    val thisUser = connections.find { it.name == userName }!!
//                    connections -= thisUser
//                    connections.forEach {
//                        it.session.apply {
//                            sendSerialized(Message(message = "${thisUser.name} left the chat"))
//                            sendSerialized(connections.count())
//                        }
//                    }
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            }
//        }
//        webSocket("/chat{username?}") {
//            val userName = call.parameters["username"]
//                ?: return@webSocket call.respond("must have a name")
//            val thisConnection = Connection(userName, this)
//            try {
//                if (userName.isBlank() || userName.isEmpty() || userName == "") return@webSocket print("invalid $userName")
//                val isInGroupAlready = connections.any { it.name == userName }
//                if (!isInGroupAlready) {
//                    connections.add(thisConnection)
//                }
//
//                if (thisConnection.session.isActive) {
//                    sendSerialized(
//                        Message(message = "welcome to the chat!, there are ${connections.count()} people in the chat")
//                    )
//                    connections.forEach {
//                        it.session.apply {
//                            sendSerialized(Message(message = "$userName joined the chat!"))
//                            sendSerialized(connections.count())
//                        }
//                    }
//                    for (frame in incoming) {
//                        connections.forEach {
//                            val incomingMessage = it.session.converter?.deserialize<Message>(frame)
//                            it.session.sendSerialized(incomingMessage)
//                        }
//                    }
//                }
//            } catch (e: Exception) {
//                println(e.localizedMessage)
//            } finally {
//                println("removing user ${thisConnection.name}")
//                connections -= thisConnection
//            }
//        }
//    }
//}