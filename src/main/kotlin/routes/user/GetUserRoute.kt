package com.example.routes.user

import com.example.config.UserPrincipalKey
import com.example.config.UserServiceKey
import com.example.dto.DtoRes
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.getUser() {
    get {
        val userPrincipal = call.attributes[UserPrincipalKey]
        val userService = application.attributes[UserServiceKey]

        val user = userService.getUser(userPrincipal.id)

        call.respond(
            HttpStatusCode.OK,
            DtoRes.success(
                "user retrieved successfully",
                mapOf("user" to user)
            )
        )
    }
}