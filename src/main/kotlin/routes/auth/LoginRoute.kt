package com.example.routes.auth

import com.example.config.AuthServiceKey
import com.example.domain.UserLoginData
import com.example.dto.DtoRes
import com.example.dto.LoginRequest
import com.example.service.AuthService
import com.example.validation.isValidEmail
import io.ktor.http.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.login() {
    post("/login") {
        val req = call.receive<LoginRequest>()
        val authService: AuthService = application.attributes[AuthServiceKey]

        // Login and obtain tokens
        val tokens = authService.login(
            UserLoginData(req.email, req.password),
            req.deviceName
        )

        // Send user the tokens data
        call.respond(
            HttpStatusCode.OK,
            DtoRes.success(
                "login successful",
                mapOf(
                    "access_token" to tokens.accessToken,
                    "refresh_token" to tokens.refreshToken
                )
            )
        )
    }
}

fun LoginRequest.validate(): ValidationResult {
    if (!this.email.isValidEmail()) {
        return ValidationResult.Invalid("invalid email")
    }

    if (this.password.isEmpty()) {
        return ValidationResult.Invalid("")
    }

    return ValidationResult.Valid
}