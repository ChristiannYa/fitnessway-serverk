package com.example.routes.auth

import com.example.config.AuthServiceKey
import com.example.domain.UserRegisterData
import com.example.dto.DtoRes
import com.example.dto.RegisterRequestDto
import com.example.utils.*
import io.ktor.http.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.register() {
    post(path = "/register") {
        val req = call.receive<RegisterRequestDto>()
        val authService = application.attributes[AuthServiceKey]

        // Register and obtain tokens
        val tokens = authService.register(
            UserRegisterData(
                req.name,
                req.email,
                req.password,
            ),
            req.deviceName
        )

        // Send user the tokens data
        call.respond(
            HttpStatusCode.Created,
            DtoRes.success(
                "user registered successfully",
                mapOf(
                    "access_token" to tokens.accessToken,
                    "refresh_token" to tokens.refreshToken
                )
            )
        )
    }
}

fun validateRegisterRequest(req: RegisterRequestDto): ValidationResult {
    validateName(req.name).toValidationResult().let {
        if (it is ValidationResult.Invalid) return it
    }

    if (!req.email.isValidEmail()) {
        return ValidationResult.Invalid("invalid email")
    }

    validatePassword(req.password).toValidationResult().let {
        if (it is ValidationResult.Invalid) return it
    }

    req.confirmedPassword.validate("confirmed password") {
        it.isEqualTo(req.password, "password")
    }.toValidationResult().let {
        if (it is ValidationResult.Invalid) return it
    }

    return ValidationResult.Valid
}