package com.example.plugins

import com.example.service.JwtService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureJwt(jwtService: JwtService) {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtService.jwtPayload.realm
            verifier(jwtService.jwtVerifier)
            validate { jwtService.validate(it) }
            challenge { _, _ -> jwtService.challenge() }
        }
    }
}