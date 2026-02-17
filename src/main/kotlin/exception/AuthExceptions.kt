package com.example.exception

/**
 * Base exception for authentication-related errors
 */
sealed class AuthException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Thrown when login credentials are invalid
 */
class InvalidCredentialsException(
    message: String = "Invalid email or password",
    cause: Throwable? = null
) : AuthException(message, cause)

/**
 * Thrown when a user tries to access a resource without proper authentication
 */
class UnauthorizedException(
    message: String = "Authentication required",
    cause: Throwable? = null
) : AuthException(message, cause)

/**
 * Thrown when a user doesn't have permission to access a resource
 */
class ForbiddenException(
    message: String = "You don't have permission to access this resource",
    cause: Throwable? = null
) : AuthException(message, cause)