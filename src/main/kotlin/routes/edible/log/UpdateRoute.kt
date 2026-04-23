package com.example.routes.edible.log

import com.example.config.EdibleLogServiceKey
import com.example.config.UserPrincipalKey
import com.example.domain.FoodLogUpdate
import com.example.dto.DtoRes
import com.example.dto.FoodLogUpdateRequest
import com.example.validation.toValidationResult
import com.example.validation.validate
import io.ktor.http.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.update() {
    put {
        val req = call.receive<FoodLogUpdateRequest>()
        val userPrincipal = call.attributes[UserPrincipalKey]
        val foodLogService = application.attributes[EdibleLogServiceKey]

        val foodLog = foodLogService.update(
            FoodLogUpdate(
                userId = userPrincipal.id,
                isUserPremium = userPrincipal.isPremium,
                foodLogId = req.foodLogId,
                userFoodSnapshotId = req.userFoodSnapshotId,
                servings = req.servings
            )
        )

        call.respond(
            HttpStatusCode.OK,
            DtoRes.success(
                "food log updated successfully",
                mapOf("food_log_updated" to foodLog)
            )
        )
    }
}

fun FoodLogUpdateRequest.validate(): ValidationResult {

    this.foodLogId.toString().validate("food log id") {
        it.isPositiveDouble()
    }.toValidationResult().let {
        if (it is ValidationResult.Invalid) return it
    }

    this.userFoodSnapshotId?.toString()?.validate("user food snapshot it") {
        it.isPositiveDouble()
    }?.toValidationResult().let {
        if (it is ValidationResult.Invalid) return it
    }

    this.servings.toString().validate("servings") {
        it.isPositiveDouble()
    }.toValidationResult().let {
        if (it is ValidationResult.Invalid) return it
    }

    return ValidationResult.Valid
}