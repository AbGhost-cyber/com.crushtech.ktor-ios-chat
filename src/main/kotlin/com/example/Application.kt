package com.example

import com.example.database.ChatService
import com.example.database.ChatServiceImpl
import com.example.plugins.*
import com.example.security.hashing.SHA256HashingService
import com.example.security.token.JwtTokenService
import com.example.security.token.TokenConfig
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8081, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val chatService: ChatService = ChatServiceImpl()
    val tokenService = JwtTokenService()

    /*
    Note that secret information should not be assigned like this.
     Consider using environment variables to specify such parameters.
     */
    val tokenConfig = TokenConfig(
        issuer = "http://0.0.0.0:8081",
        audience = "users",
        expiresIn = 365L * 1000L * 60L * 60L * 24L,
        secret = "my awesome secret"
    )
    val hashingService = SHA256HashingService()

    configureContentNegotiation()
    configureSerialization()
    configureSecurity(tokenConfig)
    configureRouting(chatService, hashingService, tokenConfig, tokenService)
    configureSockets()
    configureMonitoring()
}
