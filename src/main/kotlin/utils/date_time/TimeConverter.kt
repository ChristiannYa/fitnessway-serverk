package com.example.utils.date_time

import com.example.domain.InstantRange
import java.time.ZoneId
import java.time.format.DateTimeParseException
import java.time.zone.ZoneRulesException
import kotlin.time.Instant
import kotlin.time.toKotlinInstant

class TimeConverter(private val parser: DateTimeParser) {
    /**
     * Converts a user's local date-time string to a UTC [Instant] for storage.
     *
     * @param dateTime The date-time string in the parser's expected format (e.g. "03-31-2026 10:45 AM")
     * @param timeZone The user's IANA timezone
     * @return A UTC [Instant] representing the absolute moment in time
     */
    fun toUtc(dateTime: String, timeZone: String): Instant {
        val zoneId = ZoneId.of(timeZone)
        val localDateTime = parser.parseDateTime(dateTime)

        return localDateTime
            .atZone(zoneId)
            .toInstant()
            .toKotlinInstant()
    }

    /**
     * Converts a user's local date string to a UTC [InstantRange] for querying.
     *
     * The range spans from the start to the end of the given day in the
     * user's timezone, converted to UTC. Use [InstantRange.start] as inclusive
     * and [InstantRange.end] as exclusive in queries.
     *
     * @param date The date string in the parser's expected format (e.g. "03-31-2026")
     * @param timezone The user's IANA timezone
     * @return An [InstantRange] result representing the full day in UTC
     */
    fun toUtcRangeRes(date: String, timezone: String): Result<InstantRange> {
        val zoneId = try {
            ZoneId.of(timezone)
        } catch (_: ZoneRulesException) {
            null
        } catch (_: DateTimeParseException) {
            null
        } ?: return Result.failure(
            IllegalArgumentException("invalid zone id")
        )

        val localDate = try {
            parser.parseDate(date)
        } catch (_: DateTimeParseException) {
            null
        } ?: return Result.failure(
            IllegalArgumentException(
                "invalid date format, expected ${parser.datePattern}"
            )
        )

        val start = localDate
            .atStartOfDay(zoneId)
            .toInstant()
            .toKotlinInstant()

        val end = localDate
            .plusDays(1)
            .atStartOfDay(zoneId)
            .toInstant()
            .toKotlinInstant()

        return Result.success(InstantRange(start, end))
    }
}