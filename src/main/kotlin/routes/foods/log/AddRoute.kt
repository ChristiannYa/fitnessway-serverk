package com.example.routes.foods.log

import com.example.config.FoodLogServiceKey
import com.example.config.UserPrincipalKey
import com.example.domain.FoodLogAdd
import com.example.domain.FoodLogCategory
import com.example.domain.FoodSource
import com.example.dto.DtoRes
import com.example.dto.FoodLogAddRequest
import com.example.validation.toValidationResult
import com.example.validation.validate
import io.ktor.http.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.add() {
    post {
        val req = call.receive<FoodLogAddRequest>()
        val userPrincipal = call.attributes[UserPrincipalKey]
        val foodLogService = application.attributes[FoodLogServiceKey]

        val foodLog = foodLogService.add(
            FoodLogAdd(
                userId = userPrincipal.id,
                foodId = req.foodId,
                servings = req.servings,
                category = req.category,
                time = req.time,
                source = req.source,
            )
        )
        
        call.respond(
            HttpStatusCode.Created,
            DtoRes.success(
                "food log added successfully",
                mapOf("food_log_added" to foodLog)
            )
        )
    }
}

fun FoodLogAddRequest.validate(): ValidationResult {
    this.foodId.toString().validate("food id") {
        it.isPositiveDouble()
    }.toValidationResult().let {
        if (it is ValidationResult.Invalid) return it
    }

    this.servings.toString().validate("servings") {
        it.isPositiveDouble()
    }.toValidationResult().let {
        if (it is ValidationResult.Invalid) return it
    }

    this.category.toString().validate("category") {
        it.isEnumValidated<FoodLogCategory>()
    }.toValidationResult().let {
        if (it is ValidationResult.Invalid) return it
    }

    this.source.toString().validate("source") {
        it.isEnumValidated<FoodSource>()
    }.toValidationResult().let {
        if (it is ValidationResult.Invalid) return it
    }

    return ValidationResult.Valid
}