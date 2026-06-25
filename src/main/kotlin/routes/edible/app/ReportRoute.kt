package com.example.routes.edible.app

import com.example.config.AppEdibleServiceKey
import com.example.config.UserPrincipalKey
import com.example.domain.AppEdibleReport
import com.example.dto.AppEdibleReportRequest
import com.example.dto.DtoRes
import com.example.validation.toValidationResult
import com.example.validation.validate
import io.ktor.http.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.report() {
    post("/report") {
        val req = call.receive<AppEdibleReportRequest>()
        val userId = call.attributes[UserPrincipalKey].id
        val service = application.attributes[AppEdibleServiceKey]

        val report = service.report(req, userId)

        call.respond(
            HttpStatusCode.OK,
            DtoRes.success(
                "report submitted successfully",
                mapOf("report" to report)
            )
        )
    }
}

fun AppEdibleReportRequest.validate(): ValidationResult {
    this.reason
        .validate("reason") { it.isEnumValidated<AppEdibleReport.Reason>() }
        .toValidationResult()
        .let { if (it is ValidationResult.Invalid) return it }

    this.notes
        ?.validate("notes") { it.hasMaxLen(100) }
        ?.toValidationResult()
        ?.let { if (it is ValidationResult.Invalid) return it }

    return ValidationResult.Valid
}