package com.example.exception

/**
 * Base exception for token-related errors
 */
sealed class TokenException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Thrown when a JWT token is invalid, expired, or revoked
 */
class InvalidTokenException(
    message: String,
    cause: Throwable? = null
) : TokenException(message, cause)

/**
 * Thrown when a JWT token failed to verify
 */
class TokenVerificationException(
    message: String,
    cause: Throwable? = null
) : TokenException(message, cause)

/**
 * Thrown when token generation fails
 */
class TokenGenerationException(
    message: String,
    cause: Throwable? = null
) : TokenException(message, cause)