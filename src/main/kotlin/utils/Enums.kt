package com.example.utils

inline fun <reified T : Enum<T>> String.isValidEnum(): Boolean =
    enumValues<T>().any { it.name.equals(this, ignoreCase = true) }


inline fun <reified T : Enum<T>> String.toEnum(): T =
    enumValues<T>().first { it.name.equals(this, ignoreCase = true) }

inline fun <reified T : Enum<T>> String.toEnumOrNull(): T? =
    this
        .takeIf { it.isValidEnum<T>() }
        ?.toEnum<T>()

inline fun <reified T : Enum<T>> String.toEnumOrThrow(exception: () -> Throwable): T =
    this.toEnumOrNull() ?: throw exception()

inline fun <reified T : Enum<T>> listEnumValues() =
    enumValues<T>().toList().joinToString()

inline fun <reified T : Enum<T>> T.toList() =
    enumValues<T>().toList().joinToString()
