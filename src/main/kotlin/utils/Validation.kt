package com.example.utils

import io.ktor.server.plugins.requestvalidation.*

private val EMAIL_ADDRESS_PATTERN = Regex(
    "[a-zA-Z0-9+_.-]{1,256}@[a-zA-Z0-9][a-zA-Z0-9-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9-]{0,25})+"
)
private val err_min_len = { len: Int -> "must be at least $len characters long" }
private val err_max_len = { len: Int -> "cannot have more than $len characters" }
private val err_len_in = { minLen: Int, maxLen: Int -> "must have between $minLen and $maxLen characters" }
private val err_eq = { target: String -> "does not match $target" }
private const val ERR_EMPTY = "must be provided"
private const val ERR_WHITESPACE = "cannot contain whitespace"
private const val ERR_UPPERCASE = "must contain an uppercase letter"
private const val ERR_LOWERCASE = "must contain a lowercase letter"
private const val ERR_DIGIT = "must contain at least one digit"
private const val ERR_SPECIAL = "must contain a special character"
private const val ERR_NO_SPECIAL = "cannot contain special characters"

fun Result<Unit>.andThen(next: () -> Result<Unit>): Result<Unit> =
    if (isSuccess) next() else this

class ValidationScope(private val fieldName: String, private val value: String) {
    fun String.hasMinLen(len: Int): Result<Unit> =
        this.let { runCatching { require(it.length >= len) { "$fieldName ${err_min_len(len)}" } } }

    fun String.hasMaxLen(len: Int): Result<Unit> =
        runCatching { require(this.length <= len) { "$fieldName ${err_max_len(len)}" } }

    fun String.hasLenIn(minLen: Int, maxLen: Int): Result<Unit> =
        runCatching { require(this.length in minLen..maxLen) { "$fieldName ${err_len_in(minLen, maxLen)}" } }

    fun String.isEqualTo(target: String, property: String): Result<Unit> =
        runCatching { require(this == target) { "$fieldName ${err_eq(property)}" } }

    fun String.isProvided(): Result<Unit> =
        runCatching { require(this.isNotEmpty()) { "$fieldName $ERR_EMPTY" } }

    fun String.hasNoWhitespace(): Result<Unit> =
        runCatching { require(this.none { it.isWhitespace() }) { "$fieldName $ERR_WHITESPACE" } }

    fun String.hasUpperCase(): Result<Unit> =
        runCatching { require(this.any { it.isUpperCase() }) { "$fieldName $ERR_UPPERCASE" } }

    fun String.hasLowerCase(): Result<Unit> =
        runCatching { require(this.any { it.isLowerCase() }) { "$fieldName $ERR_LOWERCASE" } }

    fun String.hasDigit(): Result<Unit> =
        runCatching { require(this.any { it.isDigit() }) { "$fieldName $ERR_DIGIT" } }

    fun String.hasSpecialChar(): Result<Unit> =
        runCatching { require(this.any { !it.isLetterOrDigit() }) { "$fieldName $ERR_SPECIAL" } }

    fun String.hasNoSpecialChar(): Result<Unit> =
        runCatching { require(this.all { it.isLetterOrDigit() || it.isWhitespace() }) { "$fieldName $ERR_NO_SPECIAL" } }
}

fun String.validate(fieldName: String, block: ValidationScope.(String) -> Result<Unit>): Result<Unit> {
    val scope = ValidationScope(fieldName, this)
    return scope.block(this)
}

fun Result<Unit>.toValidationResult(): ValidationResult =
    if (isSuccess) ValidationResult.Valid
    else ValidationResult.Invalid(exceptionOrNull()?.message ?: "Validation failed")

// Generic type information is erased at runtime due to Java's type erasure.
// `reified` preserves the type at runtime, but only works with inline functions.
// By making the function `inline`, the compiler knows the actual type at each call site.
inline fun <reified T : Enum<T>> enumContainsIgnoreCase(name: String): Boolean =
    enumValues<T>().any { it.name.equals(name, ignoreCase = true) }
/*
// Without reified
fun <T : Enum<T>> enumContainsIgnoreCaseWrong(name: String): Boolean =
    // ERROR: Cannot access T::class at runtime!
    enumValues<T>().any {  it.name.equals(name, ignoreCase = true) }
 */

// -----------
// Validations
// -----------
fun String.isValidEmail(): Boolean = this.isNotEmpty() && EMAIL_ADDRESS_PATTERN.matches(this)

fun validatePassword(password: String): Result<Unit> = password.validate("password") {
    it.isProvided()
        .andThen { it.hasNoWhitespace() }
        .andThen { it.hasLenIn(8, 64) }
        .andThen { it.hasDigit() }
        .andThen { it.hasUpperCase() }
        .andThen { it.hasLowerCase() }
        .andThen { it.hasSpecialChar() }
        .andThen { it.hasNoWhitespace() }
}

fun validateName(name: String): Result<Unit> = name.validate("name") {
    it.isProvided()
        .andThen { it.hasNoWhitespace() }
        .andThen { it.hasLenIn(2, 50) }
        .andThen { it.hasNoSpecialChar() }
}

