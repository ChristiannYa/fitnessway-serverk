package com.example.utils

// Generic type information is erased at runtime due to Java's type erasure.
// `reified` preserves the type at runtime, but only works with inline functions.
// By making the function `inline`, the compiler knows the actual type at each call site.
inline fun <reified T : Enum<T>> enumContains(name: String, ignoreCase: Boolean = true): Boolean =
    enumValues<T>().any { it.name.equals(name, ignoreCase = ignoreCase) }
/*
// Without reified
fun <T : Enum<T>> enumContainsIgnoreCaseWrong(name: String): Boolean =
    // ERROR: Cannot access T::class at runtime!
    enumValues<T>().any {  it.name.equals(name, ignoreCase = true) }
 */

inline fun <reified T : Enum<T>> listEnumValues() =
    enumValues<T>().toList().joinToString()
