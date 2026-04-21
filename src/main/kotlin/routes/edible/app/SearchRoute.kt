package com.example.routes.foods.app

import com.example.config.AppFoodServiceKey
import com.example.config.UserPrincipalKey
import com.example.domain.AppFoodSearchPaginationCriteria
import com.example.domain.PaginationCriteria
import com.example.dto.DtoRes
import com.example.exception.MissingSearchQueryException
import com.example.utils.extensions.extractPaginationOrThrow
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.search() {
    get("/search") {
        val userPrincipal = call.attributes[UserPrincipalKey]
        val appFoodService = application.attributes[AppFoodServiceKey]

        val (limit, offset) = call.extractPaginationOrThrow()

        val query = call.request.queryParameters["q"]?.trim()
            ?: throw MissingSearchQueryException()

        val pagination = appFoodService.search(
            PaginationCriteria(
                data = AppFoodSearchPaginationCriteria(
                    query = query,
                    userId = userPrincipal.id
                ),
                limit = limit,
                offset = offset
            )
        )

        call.respond(
            HttpStatusCode.OK,
            DtoRes.success(
                "app foods pagination retrieved successfully",
                mapOf("app_foods_pagination" to pagination)
            )
        )
    }
}