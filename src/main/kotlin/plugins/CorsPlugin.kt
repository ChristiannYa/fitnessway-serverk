package com.example.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureCors() {
    val allowedOrigins =
        (System.getProperty("CORS_ALLOWED_ORIGINS")
            ?: System.getenv("CORS_ALLOWED_ORIGINS")) // Railway uses `.getenv`
            ?.split(",")
            ?: listOf("localhost:3000", "10.0.0.4:3000")

    install(CORS) {
        allowedOrigins.forEach {
            allowHost(it, schemes = listOf("https", "http"))
        }

        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
    }
}