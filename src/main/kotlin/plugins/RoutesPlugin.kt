package com.example.plugins

import com.example.routes.auth.authRoutes
import com.example.routes.edible.edibleRoutes
import com.example.routes.nutrient.nutrientRoutes
import com.example.routes.user.userRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureApiRoues() {
    routing {
        route("/api/kt") {
            get {
                call.respondText("Hello from Fitnessway's API Kotlin version. This is the main api route!")
            }

            authRoutes()

            withAuth {
                userRoutes()
                edibleRoutes()
                nutrientRoutes()
            }
        }
    }
}

fun Application.configureAppRoutes() {
    routing {
        get("/") {
            call.respondText("Hello from Fitnessway's API Kotlin version. This is the welcome route!")
        }
    }
}