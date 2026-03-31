package com.example.routes.foods.log

import io.ktor.server.routing.*

fun Route.foodLogRoutes() {
    route("/log") {
        findById()
        getLatest()
        add()
    }
}