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

        handleExceptionWithContext<RequestValidationException>(
            HttpStatusCode.BadRequest
        ) { _, ex ->
            if (ex.reasons.isNotEmpty()) ex.reasons.joinToString(", ")
            else "request validation error"
        }

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
        handleExceptions<TokenException> { ex ->
            when (ex) {
                is InvalidTokenException
                    -> ex.message.toString() to HttpStatusCode.Unauthorized

                is TokenVerificationException
                    -> ex.message.toString() to HttpStatusCode.Unauthorized

                is TokenGenerationException
                    -> ex.message.toString() to HttpStatusCode.InternalServerError
            }
        }

        // ---------------
        // AUTH EXCEPTIONS
        // ---------------
        handleExceptions<AuthException> { ex ->
            when (ex) {
                is InvalidCredentialsException
                    -> ex.message.toString() to HttpStatusCode.Unauthorized

                is UnauthorizedException
                    -> ex.message.toString() to HttpStatusCode.Unauthorized

                is ForbiddenException
                    -> ex.message.toString() to HttpStatusCode.Forbidden
            }
        }

        // ---------------
        // USER EXCEPTIONS
        // ---------------
        handleExceptions<UserException> { ex ->
            when (ex) {
                is UserNotFoundException
                    -> ex.message.toString() to HttpStatusCode.NotFound

                is UserAlreadyExistsException
                    -> ex.message.toString() to HttpStatusCode.Conflict
            }
        }

        // ------------------------
        // PENDING FOOD EXCEPTIONS
        // ------------------------
        handleExceptions<PendingFoodsException> { ex ->
            when (ex) {
                is PendingFoodNotFoundException
                    -> ex.message.toString() to HttpStatusCode.NotFound

                is DailySubmissionLimitExceededException
                    -> ex.message.toString() to HttpStatusCode.TooManyRequests

                is DuplicateFoodSubmissionException
                    -> ex.message.toString() to HttpStatusCode.Conflict

                is PendingFoodAlreadyReviewedException
                    -> ex.message.toString() to HttpStatusCode.Conflict
            }
        }
    }
}

private inline fun <reified T : Throwable> StatusPagesConfig.handleExceptionWithContext(
    statusCode: HttpStatusCode,
    crossinline message: suspend (ApplicationCall, T) -> String
) {
    exception<T> { call, cause ->
        cause.logDetails()
        call.respond(statusCode, DtoRes.error(message(call, cause)))
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

    this.printStackTrace()
}