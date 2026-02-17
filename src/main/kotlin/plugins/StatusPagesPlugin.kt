package com.example.plugins

import com.example.dto.DtoRes
import com.example.exception.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.statements.BatchDataInconsistentException

fun Application.configureStatusPages() {
    install(StatusPages) {
        // ------------
        // STATUS CODES
        // ------------
        status(HttpStatusCode.NotFound) { call, code ->
            call.respond(code, DtoRes.error("404 not found"))
        }

        status(HttpStatusCode.UnsupportedMediaType) { call, code ->
            call.respond(code, DtoRes.error("unsupported media type"))
        }

        // ------------------
        // DEFAULT EXCEPTIONS
        // ------------------
        handleException<BadRequestException>(
            "invalid request body",
            HttpStatusCode.BadRequest
        )

        handleException<IllegalStateException>(
            "internal server error",
            HttpStatusCode.BadRequest
        )

        handleException<IllegalArgumentException>(
            "internal server error",
            HttpStatusCode.BadRequest
        )

        handleException<RequestValidationException>(
            "request validation error",
            HttpStatusCode.BadRequest
        )

        handleException<ExposedSQLException>(
            "internal server error",
            HttpStatusCode.InternalServerError
        )

        handleException<BatchDataInconsistentException>(
            "internal server error",
            HttpStatusCode.BadRequest
        )

        // -----------------
        // TOKEN EXCEPTIONS
        // ----------------
        handleExceptions<TokenException> { cause ->
            when (cause) {
                is InvalidTokenException
                    -> "token is invalid" to HttpStatusCode.Unauthorized

                is TokenVerificationException
                    -> "token verification failed" to HttpStatusCode.Unauthorized

                is TokenGenerationException
                    -> "token generation error" to HttpStatusCode.InternalServerError
            }
        }

        // ---------------
        // AUTH EXCEPTIONS
        // ---------------
        handleExceptions<AuthException> { cause ->
            when (cause) {
                is InvalidCredentialsException ->
                    "invalid credentials" to HttpStatusCode.Unauthorized

                is UnauthorizedException ->
                    "authentication required" to HttpStatusCode.Unauthorized

                is ForbiddenException ->
                    "no permission to access this resource" to HttpStatusCode.Forbidden
            }
        }

        // ---------------
        // USER EXCEPTIONS
        // ---------------
        handleExceptions<UserException> { cause ->
            when (cause) {
                is UserNotFoundException
                    -> "user not found" to HttpStatusCode.NotFound

                is UserAlreadyExistsException
                    -> "user already exists" to HttpStatusCode.Conflict
            }
        }
    }
}

private inline fun <reified T : Throwable> StatusPagesConfig.handleException(
    message: String,
    statusCode: HttpStatusCode
) {
    exception<T> { call, cause ->
        cause.logDetails()
        call.respond(statusCode, DtoRes.error(message))
    }
}

private inline fun <reified T : Throwable> StatusPagesConfig.handleExceptions(
    crossinline mapper: (T) -> Pair<String, HttpStatusCode>
) {
    exception<T> { call, cause ->
        cause.logDetails()
        val (message, statusCode) = mapper(cause)
        call.respond(statusCode, DtoRes.error(message))
    }
}

private fun Throwable.logDetails() {
    val exceptionName = this::class.simpleName ?: "Unknown Exception"

    println("> $exceptionName")
    println("> Message: ${this.message}")
    println("> Cause: ${this.cause}")
    println("> At: ${this.traceMethods()}")

    this.printStackTrace()
}

private fun Throwable.traceMethods(): String {
    return this.stackTrace
        .reversed()
        .drop(1)
        .joinToString(" -> ") { "${it.methodName}()" }
}