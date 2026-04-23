package com.example.routes.edible.log

import com.example.config.EdibleLogServiceKey
import com.example.config.UserPrincipalKey
import com.example.domain.EdibleType
import com.example.domain.PaginationCriteria
import com.example.domain.RecentlyLoggedFoodsPaginationCriteria
import com.example.dto.DtoRes
import com.example.exception.InvalidEdibleException
import com.example.utils.extensions.extractPaginationOrThrow
import com.example.utils.extensions.extractPathParamOrThrow
import com.example.utils.toEnumOrThrow
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.findLatest() {
    get("/latest/{edibleType}") {
        val userPrincipal = call.attributes[UserPrincipalKey]
        val foodLogService = application.attributes[EdibleLogServiceKey]

        val (limit, offset) = call.extractPaginationOrThrow()

        val edibleType: EdibleType = call
            .extractPathParamOrThrow("edibleType")
            .toEnumOrThrow { InvalidEdibleException() }

        val pagination = foodLogService.findLatest(
            PaginationCriteria(
                data = RecentlyLoggedFoodsPaginationCriteria(
                    userId = userPrincipal.id,
                    edibleType = edibleType
                ),
                limit = limit,
                offset = offset
            )
        )

        call.respond(
            HttpStatusCode.OK,
            DtoRes.success(
                "recently logged ${edibleType.name.lowercase()}s pagination retrieved successfully",
                mapOf("recently_logged_edibles_pagination" to pagination)
            )
        )
    }
}