package com.example

import com.example.config.JwtServiceKey
import com.example.config.configureDatabase
import com.example.config.configureDependencies
import com.example.plugins.*
import io.github.cdimascio.dotenv.dotenv
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
    configureCors()
    configureJwt(this.attributes[JwtServiceKey])

    // Configure routes' request validation
    configureRequestValidation()

    // Configure routes
    configureAppRoutes()
    configureApiRoues()
}

private fun loadDotenv() {
    val dotenv = dotenv {
        ignoreIfMissing = false
    }

    dotenv.entries().forEach { entry ->
        System.setProperty(entry.key, entry.value)
    }
}