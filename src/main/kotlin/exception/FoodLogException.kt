package com.example.exception

sealed class FoodLogException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

class FoodLogNotFoundException(
    message: String = "food log not found",
    cause: Throwable? = null
) : FoodLogException(message, cause)