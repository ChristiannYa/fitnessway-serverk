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

/**
 * Thrown when a user sets an offset less than 0
 */
class InvalidPaginationOffsetException(
    message: String = "pagination offset cannot be less than 0",
    cause: Throwable? = null
) : AppException(message, cause)

/**
 * Thrown when an invalid id is received.
 *
 * Builds message: "invalid `subject` id"
 *
 * @param subject represents who the invalid id belongs to
 */
class InvalidIdException(
    subject: String,
    cause: Throwable? = null
) : AppException("invalid $subject id", cause)

/**
 * Thrown when a user does not provide a search query
 */
class MissingSearchQueryException(
    message: String = "search query must be provided",
    cause: Throwable? = null
) : AppException(message, cause)

/**
 * Thrown when a user does not provide a query parameter
 */
class MissingQueryParameterException(
    queryParamName: String,
    cause: Throwable? = null
) : AppException("'$queryParamName' query parameter must be provided", cause)

/**
 * Thrown when a user does not provide a path parameter
 */
class MissingPathParameterException(
    pathParameterName: String,
    cause: Throwable? = null
) : AppException("'$pathParameterName' path parameter must be provided", cause)