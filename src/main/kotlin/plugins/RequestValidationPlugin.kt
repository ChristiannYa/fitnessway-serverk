package com.example.plugins

import com.example.dto.*
import com.example.routes.auth.validate
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

        // ------------
        // PENDING FOOD
        // ------------
        validate<AddPendingFoodRequest> { it.validate() }
    }
}