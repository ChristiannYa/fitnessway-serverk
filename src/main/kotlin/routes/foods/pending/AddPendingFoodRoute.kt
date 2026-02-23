package com.example.routes.foods.pending

import com.example.config.PendingFoodServiceKey
import com.example.config.UserPrincipalKey
import com.example.domain.FoodInformation
import com.example.domain.PendingFoodCreate
import com.example.dto.AddPendingFoodRequest
import com.example.dto.DtoRes
import com.example.validation.validate
import io.ktor.http.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.addPendingFood() {
    post("/add") {
        val req = call.receive<AddPendingFoodRequest>()
        val userPrincipal = call.attributes[UserPrincipalKey]
        val pendingFoodService = application.attributes[PendingFoodServiceKey]

        val pendingFoodSubmitted = pendingFoodService.add(
            PendingFoodCreate(
                foodInformation = FoodInformation(
                    base = req.base,
                    nutrients = req.nutrients
                ),
                author = userPrincipal.id
            )
        )

        call.respond(
            HttpStatusCode.Created,
            DtoRes.success(
                "pending food request submitted successfully",
                mapOf("pending_food_submitted" to pendingFoodSubmitted)
            )
        )
    }
}

fun AddPendingFoodRequest.validate(): ValidationResult {
    this.base.validate().let {
        if (it is ValidationResult.Invalid) return it
    }

    this.nutrients.validate().let {
        if (it is ValidationResult.Invalid) return it
    }

    return ValidationResult.Valid
}