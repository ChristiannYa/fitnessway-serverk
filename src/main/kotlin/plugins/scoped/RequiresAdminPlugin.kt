package com.example.plugins.scoped

import com.example.config.UserPrincipalKey
import com.example.domain.UserType
import com.example.exception.ForbiddenException
import io.ktor.server.application.*
import io.ktor.server.auth.*

val RequiresAdminPlugin = createRouteScopedPlugin(name = "RequiresAdminPlugin") {
    on(AuthenticationChecked) { call ->
        val userPrincipal = call.attributes[UserPrincipalKey]
        
        if (userPrincipal.type != UserType.ADMIN) {
            throw ForbiddenException("Non-administrators don't have access to this route")
        }
    }
}