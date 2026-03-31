package com.example.exception

/**
 * Base exception for pending foods
 */
sealed class PendingFoodsException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

// @TODO: Replace with more general `FoodNotFoundException`
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
    limit: Int,
    cause: Throwable? = null
) : PendingFoodsException("you have reached the maximum of $limit food submissions per day", cause)

/**
 * Thrown when user tries to submit a food that is already pending
 */
class FoodAlreadyPendingException(
    message: String = "this food is already pending",
    cause: Throwable? = null
) : PendingFoodsException(message, cause)

/**
 * Thrown when a user tries to submit a food what is already in the app's table
 */
class FoodAlreadyInAppException(
    message: String = "this food is already in the app",
    cause: Throwable? = null
) : PendingFoodsException(message, cause)

/**
 * Thrown when the reviewer tries to review a pending food that has already been
 * reviewed
 */
class PendingFoodAlreadyReviewedException(
    message: String = "this pending food has already been reviewed",
    cause: Throwable? = null
) : PendingFoodsException(message, cause)

/**
 * Thrown when a user tries to dismiss (delete) a pending food with a PENDING
 * status
 */
class CannotDismissPendingFoodException(
    message: String = "you cannot dismiss a pending food",
    cause: Throwable? = null
) : PendingFoodsException(message, cause)

/**
 * Thrown when a user tries to submit an invalid pending food status
 */
class InvalidPendingFoodStatusException(
    message: String = "invalid pending food status",
    cause: Throwable? = null
) : PendingFoodsException(message, cause)