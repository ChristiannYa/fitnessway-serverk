package com.example.routes.edible.app

import com.example.config.AppEdibleServiceKey
import com.example.config.UserPrincipalKey
import com.example.dto.DtoRes
import com.example.exception.InvalidIdException
import com.example.utils.extensions.extractPathParamOrThrow
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.reviewReport() {
    patch("{reportId}") {
        val userId = call.attributes[UserPrincipalKey].id
        val service = application.attributes[AppEdibleServiceKey]

        val reportId = call
            .extractPathParamOrThrow("reportId")
            .toIntOrNull()
            ?: throw InvalidIdException("report")

        val report = service.reviewReport(reportId, userId)

        call.respond(
            HttpStatusCode.OK,
            DtoRes.success(
                "report reviewed successfully",
                mapOf("report" to report)
            )
        )
    }
}