package com.example.routes.foods.app

import io.ktor.server.routing.*

fun Route.appFoodRoutes() {
    route("/app") {
        findById()
        search()
    }
}