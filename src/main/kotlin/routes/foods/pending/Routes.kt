package com.example.routes.foods.pending

import com.example.plugins.withAdmin
import io.ktor.server.routing.*

fun Route.pendingFoodRoutes() {
    route("/pending") {
        route("/find-by") {
            withAdmin {
                findByUserId()
                findByUserType()
                reviewPendingFood()
            }
        }

        findMyOwn()
        addPendingFood()
        dismissReview()
    }
}