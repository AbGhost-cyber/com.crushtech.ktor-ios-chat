package com.example.plugins // ktlint-disable filename

import com.example.database.ChatService
import com.example.security.hashing.HashingService
import com.example.security.token.TokenConfig
import com.example.security.token.TokenService
import com.routes.*
import com.routes.sockets.groupChatSocket
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    chatService: ChatService,
    hashingService: HashingService,
    tokenConfig: TokenConfig,
    tokenService: TokenService
) {
    routing {
        signUpRoute(hashingService, chatService)
        loginRoute(chatService, hashingService, tokenService, tokenConfig)
        groupRoute(chatService)
        testJwt()
        joinGroupRoute(chatService)
        adminAcceptUserRoute(chatService)
        groupChatSocket(chatService)
        searchGroupRoute(chatService)
    }
}
