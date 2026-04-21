package com.example.routes.edible.pending

import com.example.plugins.withAdmin
import com.example.routes.foods.pending.*
import io.ktor.server.routing.*

fun Route.pendingFoodRoutes() {
    route("/pending") {
        withAdmin {
            route("/find-by") {
                findByUserId()
                findByUserType()
            }

            reviewPendingFood()
        }

        findMyOwn()
        addPendingFood()
        dismissReview()
    }
}