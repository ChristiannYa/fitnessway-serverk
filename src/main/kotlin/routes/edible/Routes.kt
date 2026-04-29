package com.example.routes.edible

import com.example.routes.edible.app.appFoodRoutes
import com.example.routes.edible.pending.pendingFoodRoutes
import com.example.routes.edible.user.userEdibleRoutes
import com.example.routes.foods.log.foodLogRoutes
import io.ktor.server.routing.*

fun Route.edibleRoutes() {
    route("/edible") {
        appFoodRoutes()
        pendingFoodRoutes()
        userEdibleRoutes()
        foodLogRoutes()
    }
}