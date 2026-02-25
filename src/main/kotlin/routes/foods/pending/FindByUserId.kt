package com.example.routes.foods.pending

import com.example.config.PendingFoodServiceKey
import com.example.config.UserPrincipalKey
import com.example.dto.DtoRes
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.findByUserId() {
    get("/user-id") {
        val userPrincipal = call.attributes[UserPrincipalKey]
        val pendingFoodService = application.attributes[PendingFoodServiceKey]

        val pendingFoods = pendingFoodService.findByUserId(userPrincipal.id)

        call.respond(
            HttpStatusCode.OK,
            DtoRes.success(
                "pending foods retrieved successfully",
                mapOf("pending_foods" to pendingFoods)
            )
        )
    }
}