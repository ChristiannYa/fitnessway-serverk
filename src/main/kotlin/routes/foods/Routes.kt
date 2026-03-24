package com.example.routes.foods

import com.example.routes.foods.app.appFoodRoutes
import com.example.routes.foods.log.foodLogRoutes
import com.example.routes.foods.pending.pendingFoodRoutes
import io.ktor.server.routing.*

fun Route.foodRoutes() {
    route("/food") {
        appFoodRoutes()
        pendingFoodRoutes()
        foodLogRoutes()
    }
}