package com.example.routes.edible.app

import com.example.plugins.withAdmin
import io.ktor.server.routing.*

fun Route.appFoodRoutes() {
    route("/app") {
        withAdmin {
            findAdminSubmissions()
            getReports()
            submit()
            update()
            setBarcode()
        }

        findById()
        findByBarcode()

        search()

        route("/report") {
            report()

            withAdmin {
                reviewReport()
            }
        }
    }
}