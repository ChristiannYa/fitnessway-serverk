package com.example.routes.edible.user

import com.example.config.UserEdibleServiceKey
import com.example.config.UserPrincipalKey
import com.example.dto.DtoRes
import com.example.dto.EdibleAddRequest
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.add() {
    post {
        val req = call.receive<EdibleAddRequest>()
        val userPrincipal = call.attributes[UserPrincipalKey]
        val userEdibleService = application.attributes[UserEdibleServiceKey]

        userEdibleService.add(req, userPrincipal)

        call.respond(
            HttpStatusCode.Created,
            DtoRes.success<Unit>("user ${req.edibleType} added successfully")
        )
    }
}