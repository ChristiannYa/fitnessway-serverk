package com.example.validation

import com.example.domain.EdibleBase
import io.ktor.server.plugins.requestvalidation.*

fun String.validateAsFoodName() = this.validateAsName("food name", whiteSpace = true)
fun String.validateAsFoodBrand() = this.validateAsName("food brand", whiteSpace = true)

fun EdibleBase.validate(): ValidationResult {
    this.name.validateAsFoodName().toValidationResult().let {
        if (it is ValidationResult.Invalid) return it
    }

    this.brand?.let {
        it.validateAsFoodBrand().toValidationResult().let { result ->
            if (result is ValidationResult.Invalid) return result
        }
    } ?: return ValidationResult.Invalid("food brand must be provided")

    if (this.amountPerServing <= 0.0) {
        return ValidationResult.Invalid("food amount per serving must be greater than 0")
    }

    return ValidationResult.Valid
}