package com.example.utils

fun <T : Enum<T>> Enum<T>.prettify(capitalized: Boolean = false): String {
    return this.name
        .lowercase()
        .replace("_", " ")
        .let {
            if (capitalized) it.replaceFirstChar { s -> s.uppercase() }
            else it
        }
}