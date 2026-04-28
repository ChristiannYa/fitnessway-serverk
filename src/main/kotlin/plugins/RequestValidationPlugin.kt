package com.example.plugins

import com.example.dto.*
import com.example.routes.auth.validate
import com.example.routes.edible.log.validate
import com.example.routes.edible.pending.validate
import com.example.routes.edible.validate
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
        validate<EdibleLogAddRequest> { it.validate() }
        validate<FoodLogUpdateRequest> { it.validate() }

        // ----------------
        // EDIBLE (GENERAL)
        // ----------------
        validate<EdibleAddRequest> { it.validate() }

        // ------------
        // PENDING EDIBLE
        // ------------
        validate<PendingFoodReviewRequest> { it.validate() }
    }
}