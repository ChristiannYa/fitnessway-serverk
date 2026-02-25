package com.example.domain

/**
 * Wraps the criteria and pagination parameters needed to perform a paginated query.
 * @param T the type of the filter/criteria data
 */
data class PaginationCriteria<T>(
    val data: T,
    val limit: Int,
    val offset: Long
)

/**
 * Represents the raw result of a paginated database query.
 * @param T the type of the queried items
 */
data class PaginationQuery<T>(
    val data: List<T>,
    val totalCount: Long
)

/**
 * Represents the final pagination result returned to the client,
 * including computed page metadata.
 * @param T the type of the result items
 */
data class PaginationResult<T>(
    val data: List<T>,
    val totalCount: Long,
    val pageCount: Int,
    val currentPage: Int
)

