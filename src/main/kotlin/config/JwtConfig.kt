package com.example.config

data class JwtPayload(
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String
)