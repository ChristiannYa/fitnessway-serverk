package com.example.routes.foods.log

import io.ktor.server.routing.*

fun Route.foodLogRoutes() {
    route("/log") {
        findById()
        findByDate()
        getLatest()
        add()
        update()
    }
}