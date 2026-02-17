package com.example

import com.example.config.JwtServiceKey
import com.example.config.configureDatabase
import com.example.config.configureDependencies
import com.example.config.loadDotenv
import com.example.plugins.*
import io.ktor.server.application.*

fun main(args: Array<String>) {
    // Load .env variables
    loadDotenv()
    io.ktor.server.netty.EngineMain.main(args)
}

@Suppress("unused")
fun Application.module() {
    // Configure dependencies
    configureDependencies()

    // Configure infrastructure
    configureDatabase()
    configureSerialization()
    configureStatusPages()
    configureJwt(this.attributes[JwtServiceKey])

    // Configure routes
    configureRequestValidation()
    configureAppRoutes()
    configureApiRoues()
}