package com.example.routes.nutrient.intakes

import com.example.config.NutrientIntakeServiceKey
import com.example.config.UserPrincipalKey
import com.example.dto.DtoRes
import com.example.utils.extensions.extractPathParamOrThrow
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.getIntakes() {
    get("/date/{date}") {
        val userPrincipal = call.attributes[UserPrincipalKey]
        val nutrientIntakesService = application.attributes[NutrientIntakeServiceKey]

        val date = call.extractPathParamOrThrow("date")
        val nutrientIntakes = nutrientIntakesService.findByDate(userPrincipal, date)

        call.respond(
            HttpStatusCode.OK,
            DtoRes.success(
                "nutrient intakes retrieved successfully",
                mapOf("nutrient_intakes" to nutrientIntakes)
            )
        )
    }
}