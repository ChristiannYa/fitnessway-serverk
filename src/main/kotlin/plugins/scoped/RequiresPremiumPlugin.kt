package com.example.plugins.scoped

import com.example.config.UserPrincipalKey
import com.example.exception.ForbiddenException
import io.ktor.server.application.*
import io.ktor.server.auth.*

val RequiresPremiumPlugin = createRouteScopedPlugin(name = "RequiresPremiumPlugin") {
    on(AuthenticationChecked) { call ->
        val userPrincipal = call.attributes[UserPrincipalKey]

        if (!userPrincipal.isPremium) {
            throw ForbiddenException("Non-premium users don't have access to this resource")
        }
    }
}