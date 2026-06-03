package com.example

import com.example.config.JwtServiceKey
import com.example.config.configureDatabase
import com.example.config.configureDependencies
import com.example.plugins.*
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    loadDotenv()

    val port = System.getenv("PORT")?.toInt()
        ?: System.getProperty("PORT")?.toInt()
        ?: 8080

    embeddedServer(Netty, port = port, module = Application::module).start(wait = true)
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
        ignoreIfMissing = true
    }

    dotenv.entries().forEach { entry ->
        System.setProperty(entry.key, entry.value)
    }
}
