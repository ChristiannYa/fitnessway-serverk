package com.example.domain

import com.example.exception.InvalidPaginationLimitException
import com.example.exception.InvalidPaginationOffsetException
import io.ktor.server.application.*
import kotlinx.serialization.Serializable

/**
 * Represents the final pagination result returned to the client,
 * including computed page metadata.
 *
 * @param T the type of the result items
 */
@Serializable
data class PaginationResult<T>(
    val data: List<T>,
    val totalCount: Long,
    val pageCount: Int,
    val currentPage: Int
)

/**
 * Holds the criteria and pagination parameters needed to perform a paginated query.
 *
 * @param T the type of the criteria
 */
data class PaginationCriteria<T>(
    val data: T,
    val limit: Int,
    val offset: Long
)

/**
 * Represents the raw result of a paginated database query.
 *
 * @param T the type of the queried items
 */
data class PaginationQuery<T>(
    val data: List<T>,
    val totalCount: Long
)

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

