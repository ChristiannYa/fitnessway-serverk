package com.example.routes.edible.app

import com.example.config.AppEdibleServiceKey
import com.example.config.UserPrincipalKey
import com.example.domain.AppEdibleReport
import com.example.dto.DtoRes
import com.example.exception.InvalidAppEdibleReportStatusException
import com.example.utils.extensions.extractPaginationOrThrow
import com.example.utils.extensions.extractPathParamOrThrow
import com.example.utils.toEnumOrThrow
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.getReports() {
    get("/reports/{status}") {
        val userPrincipal = call.attributes[UserPrincipalKey]
        val appEdibleService = application.attributes[AppEdibleServiceKey]

        val (limit, offset) = call.extractPaginationOrThrow()
        val status: AppEdibleReport.Status = call
            .extractPathParamOrThrow("status")
            .toEnumOrThrow { InvalidAppEdibleReportStatusException() }

        val reports = appEdibleService.getReports(
            adminId = userPrincipal.id,
            status = status,
            limit = limit,
            offset = offset
        )

        call.respond(
            HttpStatusCode.OK,
            DtoRes.success(
                "app edible reports retrieved successfully",
                mapOf("app_edible_reports" to reports)
            )
        )
    }
}