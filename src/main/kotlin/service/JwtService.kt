package com.example.service

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTCreator
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import com.example.config.JwtPayload
import com.example.config.TokenDuration
import com.example.domain.*
import com.example.exception.InvalidTokenException
import com.example.exception.TokenGenerationException
import com.example.exception.TokenVerificationException
import com.example.exception.UnauthorizedException
import com.example.utils.prettify
import io.ktor.server.application.*
import io.ktor.server.auth.jwt.*
import java.time.Duration
import java.time.Instant
import java.util.*

class JwtService(application: Application) {
    val jwtPayload = JwtPayload(
        secret = application.environment.config.property("jwt.secret").getString(),
        issuer = application.environment.config.property("jwt.issuer").getString(),
        audience = application.environment.config.property("jwt.audience").getString(),
        realm = application.environment.config.property("jwt.realm").getString()
    )

    val jwtVerifier: JWTVerifier = JWT
        .require(Algorithm.HMAC256(jwtPayload.secret))
        .withAudience(jwtPayload.audience)
        .withIssuer(jwtPayload.issuer)
        .build()

    fun validate(credential: JWTCredential): UserPrincipal? {
        return try {
            val sessionId = credential.payload.getClaim("sessionId").asString()
            val userId = credential.payload.getClaim("userId").asString()
            val userType = credential.payload.getClaim("type").asString()
            val isPremium = credential.payload.getClaim("isPremium").asBoolean()

            val payloadList = listOf(sessionId, userId, userType, isPremium)

            if (!payloadList.any { it === null }) {
                UserPrincipal(
                    id = UUID.fromString(userId),
                    type = UserType.valueOf(userType),
                    isPremium = isPremium
                )
            } else null
        } catch (_: Exception) { // Triggers the challenge block
            null
        }
    }

    fun challenge() {
        throw UnauthorizedException("token is not valid or has expired")
    }

    fun verifyToken(token: String, tokenType: TokenType): DecodedJWT {
        return try {
            jwtVerifier.verify(token)
        } catch (ex: JWTVerificationException) {
            throw TokenVerificationException(
                "Error verifying ${tokenType.prettify()} token",
                ex
            )
        }
    }

    fun extractUserId(decodedJwt: DecodedJWT, tokenType: TokenType): UUID {
        val userId = decodedJwt.getClaim("userId")
            ?: throw InvalidTokenException("${tokenType.prettify()} token missing userId claim")

        return try {
            UUID.fromString(userId.asString())
        } catch (ex: IllegalArgumentException) {
            throw InvalidTokenException(
                "${tokenType.prettify()} token has invalid userId format",
                ex.cause
            )
        }
    }

    fun generateAccessToken(claims: AccessTokenClaims): String = generateJwtToken(
        tokenType = TokenType.ACCESS,
        claims = claims.toMap(),
        duration = TokenDuration.ACCESS_TOKEN
    )

    fun generateRefreshToken(claims: RefreshTokenClaims) = generateJwtToken(
        tokenType = TokenType.REFRESH,
        claims = claims.toMap(),
        duration = TokenDuration.REFRESH_TOKEN
    )

    private fun generateJwtToken(
        tokenType: TokenType,
        claims: Map<String, Any>,
        duration: Duration
    ): String {
        val errorMessage = "Error generating ${tokenType.prettify()} token"

        return try {
            JWT
                .create()
                .withAudience(jwtPayload.audience)
                .withIssuer(jwtPayload.issuer)
                .withClaims(claims)
                .withExpiresAt(Date.from(Instant.now().plus(duration)))
                .sign(Algorithm.HMAC256(jwtPayload.secret))
        } catch (ex: JWTCreationException) {
            throw TokenGenerationException(errorMessage, ex.cause)
        } catch (ex: IllegalArgumentException) {
            throw TokenGenerationException(errorMessage, ex.cause)
        }
    }

    private fun JWTCreator.Builder.withClaims(
        claims: Map<String, Any>
    ): JWTCreator.Builder {
        claims.forEach { (key, value) ->
            when (value) {
                is String -> this.withClaim(key, value)
                is Boolean -> this.withClaim(key, value)
                is Int -> this.withClaim(key, value)
                is Long -> this.withClaim(key, value)
                else -> this.withClaim(key, value.toString())
            }
        }
        return this
    }
}