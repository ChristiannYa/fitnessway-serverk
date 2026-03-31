package com.example.exception

sealed class FoodLogExceptions(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

class FoodLogNotFoundException(
    message: String = "food log not found",
    cause: Throwable? = null
) : FoodLogExceptions(message, cause)