package com.example.routes.edible.app

import com.example.config.AppEdibleServiceKey
import com.example.config.UserPrincipalKey
import com.example.dto.DtoRes
import com.example.utils.extensions.extractPaginationOrThrow
import com.example.utils.extensions.extractQueryParamOrNull
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.findPagination() {
    get("/pagination") {
        val userPrincipal = call.attributes[UserPrincipalKey]
        val appEdibleService = application.attributes[AppEdibleServiceKey]

        val (limit, offset) = call.extractPaginationOrThrow()
        val createdAt = call.extractQueryParamOrNull("createdAt")

        val pagination = appEdibleService.findPagination(
            userPrincipal = userPrincipal,
            createdAt = createdAt,
            limit = limit,
            offset = offset
        )

        call.respond(
            HttpStatusCode.OK,
            DtoRes.success(
                "admin submitted app edibles retrieved successfully",
                mapOf("submitted_app_edibles" to pagination)
            )
        )
    }
}