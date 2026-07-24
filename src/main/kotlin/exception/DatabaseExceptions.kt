package com.example.exception

sealed class DatabaseException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * @param item builds the following error message: [item] already exists
 */
class AlreadyExistsException(
    item: String,
    cause: Throwable? = null
) : DatabaseException("$item already exists", cause)

/**
 * @param item builds the following error message: [item] not found
 */
class ItemNotFoundException(
    item: String,
    cause: Throwable? = null
) : DatabaseException("$item not found", cause)

class UnexpectedInsertCountException(
    message: String = "insert count is not 1",
    cause: Throwable? = null
) : DatabaseException(message, cause)

class UnexpectedErrorException(
    message: String = "Unexpected database error",
    cause: Throwable? = null
) : DatabaseException(message, cause)