package com.example.plugins

import com.example.dto.DtoRes
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
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

        handleException<BatchDataInconsistentException>(
            "internal server error",
            HttpStatusCode.BadRequest
        )

        handleException<RequestValidationException>(
            "request validation error",
            HttpStatusCode.BadRequest
        )
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