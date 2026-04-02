package com.example.exception

sealed class NutrientIntakeException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

class NutrientIntakesNotFoundException(
    message: String = "nutrient intakes not found",
    cause: Throwable? = null
) : NutrientIntakeException(message, cause)
