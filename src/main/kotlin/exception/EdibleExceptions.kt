package com.example.exception

sealed class EdibleExceptions(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

class EdibleNotFoundException(
    message: String,
    cause: Throwable? = null
) : EdibleExceptions(message, cause)

class InvalidEdibleException(
    message: String = "invalid edible type",
    cause: Throwable? = null
) : EdibleExceptions(message, cause)
