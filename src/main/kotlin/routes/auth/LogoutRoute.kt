package com.example.routes.auth

import com.example.config.AuthServiceKey
import com.example.dto.DtoRes
import com.example.dto.LogoutRequestDto
import com.example.utils.toValidationResult
import com.example.utils.validate
import io.ktor.http.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.logout() {
    post("/logout") {
        val req = call.receive<LogoutRequestDto>()
        val authService = application.attributes[AuthServiceKey]

        // Log out user
        authService.logout(req.refreshToken)

        // Send successful response
        call.respond(
            HttpStatusCode.OK,
            DtoRes.success<Unit>("user logged out successfully")
        )
    }
}

fun validateLogoutRequest(req: LogoutRequestDto): ValidationResult {
    req.refreshToken.validate("refresh_token") {
        it.isProvided()
    }.toValidationResult().let {
        if (it is ValidationResult.Invalid) return it
    }

    return ValidationResult.Valid
}