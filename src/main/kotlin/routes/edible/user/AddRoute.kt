package com.example.routes.edible.user

import com.example.config.UserEdibleServiceKey
import com.example.config.UserPrincipalKey
import com.example.domain.EdibleType
import com.example.dto.DtoRes
import com.example.dto.UserEdibleAddRequest
import com.example.validation.toValidationResult
import com.example.validation.validate
import io.ktor.http.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.add() {
    post {
        val req = call.receive<UserEdibleAddRequest>()
        val userPrincipal = call.attributes[UserPrincipalKey]
        val userEdibleService = application.attributes[UserEdibleServiceKey]

        val userEdible = userEdibleService.add(req, userPrincipal)

        call.respond(
            HttpStatusCode.OK,
            DtoRes.success(
                "User ${req.edibleType} added successfully",
                mapOf("user_edible" to userEdible)
            )
        )
    }
}

fun UserEdibleAddRequest.validate(): ValidationResult {
    this.base.validate().let {
        if (it is ValidationResult.Invalid) return it
    }

    this.nutrients.validate().let {
        if (it is ValidationResult.Invalid) return it
    }

    this.edibleType.validate("edible type") {
        it.isEnumValidated<EdibleType>()
    }.toValidationResult().let {
        if (it is ValidationResult.Invalid) return it
    }

    return ValidationResult.Valid
}