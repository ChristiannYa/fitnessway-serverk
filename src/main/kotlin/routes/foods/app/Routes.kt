package com.example.routes.foods.app

import com.example.plugins.withPremium
import io.ktor.server.routing.*

fun Route.appFoodRoutes() {
    route("/app") {
        withPremium {
            searchAppFood()
        }
    }
}