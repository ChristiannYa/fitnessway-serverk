package com.example.routes.foods.pending

import io.ktor.server.routing.*

fun Route.pendingFoodRoutes() {
    route("/pending") {
        addPendingFood()
        reviewPendingFood()
        dismissReview()
    }
}