package com.example.routes.edible.log

import io.ktor.server.routing.*

fun Route.foodLogRoutes() {
    route("/log") {
        findById()
        findByDate()
        findLatest()
        add()
        update()
    }
}