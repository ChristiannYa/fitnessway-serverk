package com.example.plugins

import com.example.plugins.scoped.AuthPlugin
import com.example.plugins.scoped.RequiresAdminPlugin
import com.example.plugins.scoped.RequiresPremiumPlugin
import io.ktor.server.auth.*
import io.ktor.server.routing.*

private class AuthorizationRouteSelector(private val name: String) : RouteSelector() {
    override suspend fun evaluate(context: RoutingResolveContext, segmentIndex: Int) =
        RouteSelectorEvaluation.Constant

    override fun toString() = "(authorize $name)"
}

fun Route.withAuth(build: Route.() -> Unit) = authenticate("auth-jwt") {
    install(AuthPlugin)
    build()
}

fun Route.withAdmin(build: Route.() -> Unit) = apply {
    val adminRoute = createChild(AuthorizationRouteSelector("admin"))
    adminRoute.install(RequiresAdminPlugin)
    adminRoute.build()
}

fun Route.withPremium(build: Route.() -> Unit) = apply {
    val premiumRoute = createChild(AuthorizationRouteSelector("premium"))
    premiumRoute.install(RequiresPremiumPlugin)
    premiumRoute.build()
}