package com.example.validation

import com.example.domain.NutrientIdWithAmount
import io.ktor.server.plugins.requestvalidation.*

fun List<NutrientIdWithAmount>.validate(): ValidationResult {
    if (this.isEmpty()) {
        return ValidationResult.Invalid("at least 1 nutrient must be provided")
    }

    this.forEach {
        if (it.nutrientId <= 0) {
            return ValidationResult.Invalid("nutrient id is invalid")
        }

        if (it.amount <= 0.0) {
            return ValidationResult.Invalid("nutrient amount must be greater than 0")
        }
    }

    return ValidationResult.Valid
}