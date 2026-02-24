package com.example.routes.foods.pending

import io.ktor.server.routing.*

fun Route.pendingFoodRoutes() {
    route("/pending") {
        getAllByUserId()
        addPendingFood()
        reviewPendingFood()
        dismissReview()
    }
}