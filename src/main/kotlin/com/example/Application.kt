package com.example

import com.example.database.ChatService
import com.example.database.ChatServiceImpl
import com.example.plugins.configureRouting
import com.example.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.defaultheaders.*

fun main() {
    embeddedServer(Netty, port = 8081, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val service: ChatService = ChatServiceImpl()
    install(DefaultHeaders) {

    }
    configureSerialization()
    configureRouting(service = service)
    // configureSockets()
}
