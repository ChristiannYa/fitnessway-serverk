package com.example.routes.nutrient

import com.example.config.NutrientServiceKey
import com.example.config.UserPrincipalKey
import com.example.dto.DtoRes
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.getByType() {
    get("/all-by-type") {
        val userPrincipal = call.attributes[UserPrincipalKey]
        val nutrientService = application.attributes[NutrientServiceKey]

        val nutrientsByType = nutrientService.getAllByType(userPrincipal.id)

        call.respond(
            HttpStatusCode.OK,
            DtoRes.success(
                "nutrients by type retrieved successfully",
                mapOf("nutrients_by_type" to nutrientsByType)
            )
        )
    }
}