package com.example.utils.date_time

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DateTimeParser(
    val datePattern: String = "MM-dd-yyyy",
    val timePattern: String = "hh:mm a"
) {
    private val dateFormatter = DateTimeFormatter.ofPattern(datePattern)
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("$datePattern $timePattern")

    fun parseDate(date: String): LocalDate = LocalDate.parse(date, dateFormatter)
    fun parseDateTime(dateTime: String): LocalDateTime = LocalDateTime.parse(dateTime, dateTimeFormatter)
}