package com.example.plugins

import com.example.plugins.scoped.AuthPlugin
import com.example.plugins.scoped.RequiresAdminPlugin
import com.example.plugins.scoped.RequiresPremiumPlugin
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Route.withAuth(build: Route.() -> Unit) = authenticate("auth-jwt") {
    install(AuthPlugin)
    build()
}

fun Route.withAdmin(build: Route.() -> Unit) = apply {
    install(RequiresAdminPlugin)
    build()
}

fun Route.withPremium(build: Route.() -> Unit) = apply {
    install(RequiresPremiumPlugin)
    build()
}