package com.example.utils.extensions

import com.example.exception.InvalidPaginationLimitException
import com.example.exception.InvalidPaginationOffsetException
import com.example.exception.MissingPathParameterException
import io.ktor.server.application.*

/**
 * Extracts and validates the `limit` and `offset` pagination query parameters from the request.
 *
 * @return A [Pair] where the first value is the `limit` and the second is the `offset`
 * @throws InvalidPaginationLimitException if `limit` is missing or <= 0
 * @throws InvalidPaginationOffsetException if `offset` is missing or < 0
 */
fun ApplicationCall.extractPaginationOrThrow(): Pair<Int, Long> {
    val limit = request.queryParameters["limit"]?.toIntOrNull()
    val offset = request.queryParameters["offset"]?.toLongOrNull()

    if (limit == null || limit <= 0) throw InvalidPaginationLimitException()
    if (offset == null || offset < 0) throw InvalidPaginationOffsetException()

    return limit to offset
}

/**
 * Etxracts and validates a path parameter by [name] from the request
 *
 * @return The path parameter value as a [String]
 * @throws MissingPathParameterException if the parameter is missing or blank
 */
fun ApplicationCall.extractPathParamOrThrow(name: String) =
    this.parameters[name]?.takeIf { it.isNotBlank() }
        ?: throw MissingPathParameterException(name)