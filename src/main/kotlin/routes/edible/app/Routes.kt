package com.example.routes.edible.app

import com.example.plugins.withAdmin
import io.ktor.server.routing.*

fun Route.appFoodRoutes() {
    route("/app") {
        withAdmin {
            findPagination()
            submit()
            update()
            setBarcode()
        }

        findById()
        findByBarcode()

        search()
    }
}