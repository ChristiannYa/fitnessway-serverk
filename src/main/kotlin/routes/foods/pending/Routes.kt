package com.example.routes.foods.pending

import io.ktor.server.routing.*

fun Route.pendingFoodRoutes() {
    route("/pending") {
        route("/find-by") {
            findByUserId()
            findByUserType()
        }

        addPendingFood()
        reviewPendingFood()
        dismissReview()
    }
}