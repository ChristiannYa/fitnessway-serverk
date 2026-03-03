package com.example.config

import java.time.Duration

object TokenDuration {
    val ACCESS_TOKEN: Duration = Duration.ofMinutes(15)
    val REFRESH_TOKEN: Duration = Duration.ofDays(90)
}

data class JwtPayload(
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String
)