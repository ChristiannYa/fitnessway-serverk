package com.example.routes.nutrient

import com.example.routes.nutrient.intakes.getIntakes
import io.ktor.server.routing.*

fun Route.nutrientRoutes() {
    route("/nutrient") {
        route("/intake") {
            getIntakes()
        }
    }
}