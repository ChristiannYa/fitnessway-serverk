package com.example.exception

/**
 * Base exception for pending foods
 */
sealed class PendingFoodsException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Thrown when a pending food is not found
 */
class PendingFoodNotFoundException(
    message: String = "pending food not found",
    cause: Throwable? = null
) : PendingFoodsException(message, cause)


/**
 * Thrown when user exceeds daily submission limit
 */
class DailySubmissionLimitExceededException(
    message: String = "you have reached the maximum of 5 food submissions per day",
    cause: Throwable? = null
) : PendingFoodsException(message, cause)

/**
 * Thrown when user tries to submit a duplicate food
 */
class DuplicateFoodSubmissionException(
    message: String = "you have already submitted this exact food",
    cause: Throwable? = null
) : PendingFoodsException(message, cause)

/**
 * Thrown when the reviewer tries to review a pending food that has already been
 * reviewed
 */
class PendingFoodAlreadyReviewedException(
    message: String = "This pending food has already been reviewed",
    cause: Throwable? = null
) : PendingFoodsException(message, cause)