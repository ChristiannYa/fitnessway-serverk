package com.example.utils

inline fun <reified T : Enum<T>> enumContains(name: String, ignoreCase: Boolean = true): Boolean =
    enumValues<T>().any { it.name.equals(name, ignoreCase = ignoreCase) }

inline fun <reified T : Enum<T>> String.asEnum(): T =
    enumValues<T>().first { it.name.equals(this, ignoreCase = true) }

inline fun <reified T : Enum<T>> String.toEnumOrNull(): T? =
    this
        .takeIf { enumContains<T>(it) }
        ?.asEnum<T>()

inline fun <reified T : Enum<T>> String.toEnumOrThrow(exception: () -> Throwable): T =
    this.toEnumOrNull() ?: throw exception()

inline fun <reified T : Enum<T>> listEnumValues() =
    enumValues<T>().toList().joinToString()
