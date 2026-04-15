package com.example.routes.foods.pending

import com.example.config.PendingFoodServiceKey
import com.example.config.UserPrincipalKey
import com.example.dto.DtoRes
import com.example.dto.PendingFoodReviewRequest
import io.ktor.http.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.reviewPendingFood() {
    put("/review") {
        val req = call.receive<PendingFoodReviewRequest>()
        val userPrincipal = call.attributes[UserPrincipalKey]
        val pendingFoodService = application.attributes[PendingFoodServiceKey]

        val reviewedPendingFood = pendingFoodService.review(req, userPrincipal)

        call.respond(
            HttpStatusCode.OK,
            DtoRes.success(
                "pending food reviewed successfully",
                mapOf("pending_food_reviewed" to reviewedPendingFood)
            )
        )
    }
}

fun PendingFoodReviewRequest.validate(): ValidationResult {
    if (this.pendingFoodId <= 0) {
        return ValidationResult.Invalid("pending food id cannot be less than or equal to 0")
    }

    this.rejectionReason?.let {
        if (it.isEmpty()) {
            return ValidationResult.Invalid("rejection reason must have a body")
        }

        if (it.length >= 250) {
            return ValidationResult.Invalid("rejection reason cannot be greater than or equal to 250")
        }
    }

    return ValidationResult.Valid
}