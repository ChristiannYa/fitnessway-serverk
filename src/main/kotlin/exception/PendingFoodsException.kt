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
 * Thrown when a non-administrator user tries to review a pending food
 */
class NonAdministratorCannotReviewException(
    message: String = "only administrators are able to review foods",
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
 * Thrown when user tries to submit a food that is already pending
 */
class FoodAlreadyPendingException(
    message: String = "this food is already pending",
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