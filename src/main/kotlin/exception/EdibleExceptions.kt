package com.example.exception

import com.example.domain.EdibleType

sealed class EdibleExceptions(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

class EdibleNotFoundException(
    message: String,
    cause: Throwable? = null
) : EdibleExceptions(message, cause)

class EdibleAlreadyExistsException(
    edibleType: EdibleType,
    message: String = "this ${edibleType.name.lowercase()} already exists",
    cause: Throwable? = null
) : EdibleExceptions(message, cause)

class InvalidEdibleTypeException(
    message: String = "invalid edible type",
    cause: Throwable? = null
) : EdibleExceptions(message, cause)
