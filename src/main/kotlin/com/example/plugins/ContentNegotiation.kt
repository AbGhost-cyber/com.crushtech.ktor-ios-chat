package com.example.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.defaultheaders.*

fun Application.configureContentNegotiation() {
    install(DefaultHeaders) {

    }
}