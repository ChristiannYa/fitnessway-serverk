package com.example.routes.edible.app

import com.example.config.AppEdibleServiceKey
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
        val appFoodService = application.attributes[AppEdibleServiceKey]

        val appFoodId = call.extractPathParamOrThrow("id").toIntOrNull()
            ?: throw InvalidIdException("app food bsbs")

        val appFood = appFoodService.findById(appFoodId, userPrincipal.id)

        call.respond(
            HttpStatusCode.OK,
            DtoRes.success(
                "app edible retrieved successfully",
                mapOf("app_edible" to appFood)
            )
        )
    }
}