package com.example.routes.user

import com.example.config.UserPrincipalKey
import com.example.config.UserServiceKey
import com.example.dto.DtoRes
import com.example.dto.UserTimezoneSetRequest
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.setTimezone() {
    patch("/set-timezone") {
        val req = call.receive<UserTimezoneSetRequest>()
        val userPrincipal = call.attributes[UserPrincipalKey]
        val userService = application.attributes[UserServiceKey]

        userService.setTimezone(userPrincipal.id, req.timezone)

        call.respond(
            HttpStatusCode.OK,
            DtoRes.success<Unit>("timezone updated successfully")
        )
    }
}