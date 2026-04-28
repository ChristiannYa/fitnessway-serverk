package com.example.routes.edible.pending

import com.example.config.PendingFoodServiceKey
import com.example.config.UserPrincipalKey
import com.example.dto.DtoRes
import com.example.dto.EdibleAddRequest
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.addPendingFood() {
    post {
        val req = call.receive<EdibleAddRequest>()
        val userPrincipal = call.attributes[UserPrincipalKey]
        val pendingFoodService = application.attributes[PendingFoodServiceKey]

        pendingFoodService.add(req, userPrincipal)

        call.respond(
            HttpStatusCode.Created,
            DtoRes.success<Unit>(
                "pending ${req.edibleType} request submitted successfully",
            )
        )
    }
}