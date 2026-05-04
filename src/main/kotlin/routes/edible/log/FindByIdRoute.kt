package com.example.routes.edible.log

import com.example.config.EdibleLogServiceKey
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
        val foodLogService = application.attributes[EdibleLogServiceKey]

        val foodLogId = call
            .extractPathParamOrThrow("id")
            .toIntOrNull()
            ?: throw InvalidIdException("food log")

        val foodLog = foodLogService.findById(foodLogId, userPrincipal.id)

        call.respond(
            HttpStatusCode.OK,
            DtoRes.success(
                "food log retrieved successfully",
                mapOf("food_log" to foodLog)
            )
        )
    }
}