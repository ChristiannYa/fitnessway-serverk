package com.example.routes.edible.user

import io.ktor.server.routing.*

fun Route.userEdibleRoutes() {
    route("/user") {
        findMyOwn()
    }
}