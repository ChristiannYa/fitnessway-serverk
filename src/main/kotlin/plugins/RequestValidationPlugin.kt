package com.example.plugins

import com.example.dto.*
import com.example.routes.auth.validate
import com.example.routes.foods.log.validate
import com.example.routes.foods.pending.validate
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*

fun Application.configureRequestValidation() {
    install(RequestValidation) {
        // --------------
        // AUTHENTICATION
        // --------------
        validate<RegisterRequest> { it.validate() }
        validate<RefreshRequest> { it.validate() }
        validate<LoginRequest> { it.validate() }
        validate<LogoutRequest> { it.validate() }

        // --------
        // FOOD LOG
        // --------
        validate<FoodLogAddRequest> { it.validate() }
        validate<FoodLogUpdateRequest> { it.validate() }

        // ------------
        // PENDING FOOD
        // ------------
        validate<PendingFoodAddRequest> { it.validate() }
        validate<PendingFoodReviewRequest> { it.validate() }
    }
}