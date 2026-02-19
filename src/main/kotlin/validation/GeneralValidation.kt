package com.example.validation

private val EMAIL_ADDRESS_PATTERN = Regex(
    "[a-zA-Z0-9+_.-]{1,256}@[a-zA-Z0-9][a-zA-Z0-9-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9-]{0,25})+"
)

fun String.isValidEmail(): Boolean = this.isNotEmpty() && EMAIL_ADDRESS_PATTERN.matches(this)

fun String.validateAsPassword() = this.validate("password") {
    it.isProvided()
        .andThen { it.hasNoWhitespace() }
        .andThen { it.hasLenIn(8, 64) }
        .andThen { it.hasDigit() }
        .andThen { it.hasUpperCase() }
        .andThen { it.hasLowerCase() }
        .andThen { it.hasSpecialChar() }
        .andThen { it.hasNoWhitespace() }
}

fun String.validateAsName(
    fieldName: String = "name",
    whiteSpace: Boolean = false
) = this.validate(fieldName) {
    it.isProvided()
        .andThen { if (!whiteSpace) it.hasNoWhitespace() else Result.success(Unit) }
        .andThen { it.hasLenIn(2, 50) }
        .andThen { it.hasNoSpecialChar() }
}
