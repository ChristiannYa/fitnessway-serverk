package com.example.exception

sealed class FoodExceptions(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

class FoodNotFoundException(
    message: String,
    cause: Throwable? = null
) : FoodExceptions(message, cause)