package com.example.routes.edible.app

import com.example.config.AppFoodServiceKey
import com.example.config.UserPrincipalKey
import com.example.dto.DtoRes
import com.example.exception.InvalidIdException
import com.example.utils.extensions.extractPathParamOrThrow
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.findById() {
    get("/{id}") {
        val userPrincipal = call.attributes[UserPrincipalKey]
        val appFoodService = application.attributes[AppFoodServiceKey]

        val appFoodId = call.extractPathParamOrThrow("id").toIntOrNull()
            ?: throw InvalidIdException("app food")

        val appFood = appFoodService.findById(appFoodId, userPrincipal.id, userPrincipal.isPremium)

        call.respond(
            HttpStatusCode.OK,
            DtoRes.success(
                "app food retrieved successfully",
                mapOf("app_food" to appFood)
            )
        )
    }
}