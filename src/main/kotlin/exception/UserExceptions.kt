package com.example.exception

/**
 * Base exception for user-related errors
 */
sealed class UserException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Thrown when a user is not found
 */
class UserNotFoundException(
    message: String = "User not found",
    cause: Throwable? = null
) : UserException(message, cause)

/**
 * Thrown when trying to create a user that already exists
 */
class UserAlreadyExistsException(
    message: String = "User with this email already exists",
    cause: Throwable? = null
) : UserException(message, cause)