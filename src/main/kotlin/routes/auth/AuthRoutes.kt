package com.example.routes.auth

import io.ktor.server.routing.*

fun Route.authRoutes() {
    route("/auth") {
        register()
        login()
        logout()
        refresh()
    }
}