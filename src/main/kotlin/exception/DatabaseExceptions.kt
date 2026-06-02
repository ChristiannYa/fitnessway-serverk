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

class UnexpectedInsertCountException(
    message: String = "insert count is not 1",
    cause: Throwable? = null
) : DatabaseException(message, cause)

class UnexpectedErrorException(
    message: String = "Unexpected database error",
    cause: Throwable? = null
) : DatabaseException(message, cause)