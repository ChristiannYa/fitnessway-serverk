package com.example

import com.example.config.configureDatabase
import com.example.config.loadDotenv
import com.example.plugins.configureSerialization
import com.example.plugins.configureStatusPages
import io.ktor.server.application.*

fun main(args: Array<String>) {
    // Load .env variables
    loadDotenv()
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    // Configure infrastructure
    configureDatabase()
    configureSerialization()
    configureStatusPages()

    // Configure routes
    configureRouting()
}