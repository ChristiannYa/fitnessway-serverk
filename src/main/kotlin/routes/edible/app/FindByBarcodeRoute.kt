package com.example.routes.edible.app

import com.example.config.AppFoodServiceKey
import com.example.config.UserPrincipalKey
import com.example.dto.DtoRes
import com.example.utils.extensions.extractPathParamOrThrow
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.findByBarcode() {
    get("/barcode/{barcode}") {
        val userPrincipal = call.attributes[UserPrincipalKey]
        val appEdibleService = application.attributes[AppFoodServiceKey]

        val barcode = call.extractPathParamOrThrow("barcode")
        val appFood = appEdibleService.findByBarCode(barcode, userPrincipal.id)

        call.respond(
            HttpStatusCode.OK,
            DtoRes.success(
                "app edible retrieved successfully",
                mapOf("app_edible" to appFood)
            )
        )
    }
}
