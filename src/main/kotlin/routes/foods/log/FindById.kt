package com.example.routes.foods.log

import com.example.config.FoodLogServiceKey
import com.example.dto.DtoRes
import com.example.exception.InvalidIdException
import com.example.utils.extensions.extractPathParamOrThrow
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.findById() {
    get("/{id}") {
        val foodLogService = application.attributes[FoodLogServiceKey]

        val foodLogId = call.extractPathParamOrThrow("id").toIntOrNull()
            ?: throw InvalidIdException("food log")

        val foodLog = foodLogService.findById(foodLogId)

        call.respond(
            HttpStatusCode.OK,
            DtoRes.success(
                "food log retrieved successfully",
                mapOf("food_log" to foodLog)
            )
        )
    }
}