package com.example.routes.foods.log

import com.example.routes.edible.log.findByDate
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