package com.example.routes.auth

import com.example.config.AuthServiceKey
import com.example.dto.DtoRes
import com.example.dto.RefreshRequest
import com.example.validation.toValidationResult
import com.example.validation.validate
import io.ktor.http.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.refresh() {
    post("/refresh") {
        val req = call.receive<RefreshRequest>()
        val authService = application.attributes[AuthServiceKey]

        // Refresh user's token
        val newAccessToken = authService.refreshAccessToken(req.refreshToken)

        // Send the user the new access
        call.respond(
            HttpStatusCode.OK,
            DtoRes.success(
                "access token refreshed successfully",
                mapOf("access_token" to newAccessToken)
            )
        )
    }
}

fun RefreshRequest.validate(): ValidationResult {
    this.refreshToken.validate("refresh token") {
        it.isProvided()
    }.toValidationResult().let {
        if (it is ValidationResult.Invalid) return it
    }

    return ValidationResult.Valid
}