package com.example.routes.foods.log

import com.example.routes.edible.log.add
import com.example.routes.edible.log.findByDate
import com.example.routes.edible.log.findLatest
import com.example.routes.edible.log.update
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