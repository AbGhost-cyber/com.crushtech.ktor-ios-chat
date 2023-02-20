package com.routes

import com.example.database.ChatService
import com.example.database.models.request.AuthRequest
import com.example.security.hashing.HashingService
import com.example.security.hashing.SaltedHash
import com.example.security.token.TokenClaim
import com.example.security.token.TokenConfig
import com.example.security.token.TokenService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.loginRoute(
    chatService: ChatService,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    post("/login") {
        val request = kotlin.runCatching { call.receiveNullable<AuthRequest>() }.getOrNull()
            ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "bad data format!")
                return@post
            }
        val user = chatService.getUserByName(request.username)
            ?: return@post call.respond(HttpStatusCode.Conflict, "user doesn't exist")

        val isValidPassword = hashingService.verify(
            request.password,
            saltedHash = SaltedHash(
                hash = user.password,
                salt = user.salt
            )
        )
        if (!isValidPassword) {
            call.respond(HttpStatusCode.Conflict, "Incorrect username or password")
            return@post
        }
        val token = tokenService.generate(
            config = tokenConfig,
            TokenClaim(name = "userId", value = user.id.toString())
        )
        call.respond(HttpStatusCode.OK, message = token)
    }
}