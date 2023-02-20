package com.routes

import com.example.database.ChatService
import com.example.database.models.User
import com.example.database.models.request.AuthRequest
import com.example.security.hashing.HashingService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.signUpRoute(
    hashingService: HashingService,
    chatService: ChatService
) {
    post("/signup") {
        val request = kotlin.runCatching { call.receiveNullable<AuthRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest, "bad data format!")
            return@post
        }
        val areFieldsBlank = request.username.isBlank() || request.password.isBlank()
        val isPwTooShort = request.password.length < 6
        if (areFieldsBlank || isPwTooShort) {
            call.respond(HttpStatusCode.Conflict, "password is too short")
            return@post
        }
        val saltedHash = hashingService.generateSaltedHash(request.password)
        val user = User(
            username = request.username,
            password = saltedHash.hash,
            salt = saltedHash.salt
        )
        val wasAcknowledged = chatService.register(user)
        if (!wasAcknowledged) {
            call.respond(HttpStatusCode.Conflict, "Couldn't sign up at this moment, please try again later")
            return@post
        }
        call.respond(HttpStatusCode.OK, "registration successful")
    }
}