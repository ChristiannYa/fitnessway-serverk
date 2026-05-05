package com.example.constants

object SupportedTimezones {
    val unitedStates = listOf(
        "America/New_York",
        "America/Chicago",
        "America/Denver",
        "America/Phoenix",
        "America/Los_Angeles",
        "America/Anchorage",
        "Pacific/Honolulu",
    )

    val canada = listOf(
        "America/Toronto",
        "America/Winnipeg",
        "America/Edmonton",
        "America/Vancouver",
        "America/Halifax",
        "America/St_Johns",
    )

    val mexico = listOf(
        "America/Mexico_City",
        "America/Tijuana",
        "America/Monterrey",
    )

    val all = listOf(unitedStates, canada, mexico).flatten()
}