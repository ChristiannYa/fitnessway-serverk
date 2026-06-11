package com.example.routes.edible.app

import com.example.config.AppEdibleServiceKey
import com.example.config.UserPrincipalKey
import com.example.dto.AppEdibleWriteRequest
import com.example.dto.DtoRes
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.submit() {
    post {
        val req = call.receive<AppEdibleWriteRequest>()
        val appEdibleService = application.attributes[AppEdibleServiceKey]
        val userPrincipal = call.attributes[UserPrincipalKey]

        val appEdible = appEdibleService.submit(req, userPrincipal.id)

        call.respond(
            HttpStatusCode.Created,
            DtoRes.success(
                "app ${req.edibleRequest.edibleType} created successfully",
                mapOf("app_edible" to appEdible)
            )
        )
    }
}