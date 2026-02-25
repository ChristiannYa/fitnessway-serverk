package com.example.exception

/**
 * Base exception throughout the app
 */
sealed class AppException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Thrown when a user sets an invalid pagination value
 */
class InvalidPaginationLimitException(
    message: String = "pagination limit cannot be less than or equal to 0",
    cause: Throwable? = null
) : AppException(message, cause)

class InvalidPaginationOffsetException(
    message: String = "pagination offset cannot be less than 0",
    cause: Throwable? = null
) : AppException(message, cause)