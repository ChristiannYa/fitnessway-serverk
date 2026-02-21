package com.example.routes.auth

import com.example.config.AuthServiceKey
import com.example.domain.UserRegisterData
import com.example.domain.UserType
import com.example.dto.DtoRes
import com.example.dto.RegisterRequest
import com.example.utils.asEnum
import com.example.utils.enumContains
import com.example.utils.listEnumValues
import com.example.validation.*
import io.ktor.http.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.register() {
    post("/register") {
        val req = call.receive<RegisterRequest>()
        val authService = application.attributes[AuthServiceKey]

        // Register and obtain tokens
        val tokens = authService.register(
            UserRegisterData(
                req.name,
                req.email,
                req.password,
                req.userType.asEnum<UserType>()
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

fun RegisterRequest.validate(): ValidationResult {
    val req = this

    req.name.validateAsName().toValidationResult().let {
        if (it is ValidationResult.Invalid) return it
    }

    if (!req.email.isValidEmail()) {
        return ValidationResult.Invalid("invalid email")
    }

    req.password.validateAsPassword().toValidationResult().let {
        if (it is ValidationResult.Invalid) return it
    }

    req.confirmedPassword.validate("confirmed password") {
        it.isEqualTo(req.password, "password")
    }.toValidationResult().let {
        if (it is ValidationResult.Invalid) return it
    }

    if (!enumContains<UserType>(req.userType)) {
        return ValidationResult.Invalid(
            "\"${req.userType}\" is an invalid user type, must be one of: ${listEnumValues<UserType>()}"
        )
    }

    return ValidationResult.Valid
}