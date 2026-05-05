package com.example.exception

/**
 * Base exception for user-related errors
 */
sealed class UserException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

class UserNotFoundException(
    message: String = "User not found",
    cause: Throwable? = null
) : UserException(message, cause)

class UserAlreadyExistsException(
    message: String = "User with this email already exists",
    cause: Throwable? = null
) : UserException(message, cause)

class InvalidUserTypeException(
    message: String = "Invalid user type",
    cause: Throwable? = null
) : UserException(message, cause)

class InvalidUserTimezoneException(
    message: String = "Invalid user timezone",
    cause: Throwable? = null
) : UserException(message, cause)