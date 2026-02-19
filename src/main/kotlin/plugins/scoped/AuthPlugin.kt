package com.example.plugins.scoped

import com.example.config.UserPrincipalKey
import com.example.domain.UserPrincipal
import io.ktor.server.application.*
import io.ktor.server.auth.*

val AuthPlugin = createRouteScopedPlugin(name = "RoutePrincipalPlugin") {
    on(AuthenticationChecked) { call ->
        // If we're here, authentication succeeded
        val userPrincipal = call.principal<UserPrincipal>()

        // This should never be null inside authenticate() block
        // But we'll handle it defensively
        if (userPrincipal != null) {
            call.attributes.put(UserPrincipalKey, userPrincipal)
        }
    }
}