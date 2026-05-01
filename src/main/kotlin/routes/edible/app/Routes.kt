package com.example.routes.edible.app

import io.ktor.server.routing.*

fun Route.appFoodRoutes() {
    route("/app") {
        findById()
        search()
    }
}