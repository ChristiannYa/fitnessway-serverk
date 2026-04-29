package com.example.routes.edible.app

import com.example.routes.foods.app.findById
import io.ktor.server.routing.*

fun Route.appFoodRoutes() {
    route("/app") {
        findById()
        search()
    }
}