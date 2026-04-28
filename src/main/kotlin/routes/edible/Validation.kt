package com.example.routes.edible

import com.example.domain.EdibleType
import com.example.dto.EdibleAddRequest
import com.example.validation.toValidationResult
import com.example.validation.validate
import io.ktor.server.plugins.requestvalidation.*

fun EdibleAddRequest.validate(): ValidationResult {
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