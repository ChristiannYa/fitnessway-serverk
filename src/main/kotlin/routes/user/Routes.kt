package com.example.routes.user

import io.ktor.server.routing.*

fun Route.userRoutes() {
    route("/user") {
        getUser()
    }
}