package com.example

import com.example.plugins.configureRouting
import com.example.plugins.configureSerialization
import com.example.plugins.configureSockets
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.defaultheaders.*

fun main() {
    embeddedServer(Netty, port = 8081, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(DefaultHeaders) {

    }
    configureSerialization()
    configureRouting()
    configureSockets()
}
