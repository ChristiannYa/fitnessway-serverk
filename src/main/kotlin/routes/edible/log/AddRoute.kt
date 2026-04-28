package com.example.routes.edible.log

import com.example.config.EdibleLogServiceKey
import com.example.config.UserPrincipalKey
import com.example.domain.EdibleType
import com.example.domain.LogCategory
import com.example.domain.LogSource
import com.example.dto.DtoRes
import com.example.dto.EdibleLogAddRequest
import com.example.validation.toValidationResult
import com.example.validation.validate
import io.ktor.http.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.add() {
    post {
        val req = call.receive<EdibleLogAddRequest>()
        val userPrincipal = call.attributes[UserPrincipalKey]
        val foodLogService = application.attributes[EdibleLogServiceKey]

        val foodLog = foodLogService.add(userPrincipal, req)

        call.respond(
            HttpStatusCode.Created,
            DtoRes.success(
                "${req.edibleType} log added successfully",
                mapOf("food_log_added" to foodLog)
            )
        )
    }
}

fun EdibleLogAddRequest.validate(): ValidationResult {

    this.edibleId.toString().validate("edible id") {
        it.isPositiveDouble()
    }.toValidationResult().let {
        if (it is ValidationResult.Invalid) return it
    }

    this.edibleType.validate("edible type") {
        it.isEnumValidated<EdibleType>()
    }.toValidationResult().let {
        if (it is ValidationResult.Invalid) return it
    }

    this.servings.toString().validate("servings") {
        it.isPositiveDouble()
    }.toValidationResult().let {
        if (it is ValidationResult.Invalid) return it
    }

    this.category.validate("category") {
        it.isEnumValidated<LogCategory>()
    }.toValidationResult().let {
        if (it is ValidationResult.Invalid) return it
    }

    this.source.validate("source") {
        it.isEnumValidated<LogSource>()
    }.toValidationResult().let {
        if (it is ValidationResult.Invalid) return it
    }

    return ValidationResult.Valid
}