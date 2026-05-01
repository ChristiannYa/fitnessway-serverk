package com.example.routes.edible.app

import com.example.config.AppFoodServiceKey
import com.example.config.UserPrincipalKey
import com.example.domain.AppFoodSearchPaginationCriteria
import com.example.domain.EdibleType
import com.example.domain.PaginationCriteria
import com.example.dto.DtoRes
import com.example.exception.InvalidEdibleTypeException
import com.example.utils.extensions.extractPaginationOrThrow
import com.example.utils.extensions.extractQueryParamOrThrow
import com.example.utils.toEnumOrThrow
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.search() {
    get("/search") {
        val userPrincipal = call.attributes[UserPrincipalKey]
        val appFoodService = application.attributes[AppFoodServiceKey]

        val (limit, offset) = call.extractPaginationOrThrow()

        val query = call.extractQueryParamOrThrow("q")
        val edibleType: EdibleType = call
            .extractQueryParamOrThrow("edibleType")
            .toEnumOrThrow { InvalidEdibleTypeException() }

        val pagination = appFoodService.search(
            PaginationCriteria(
                data = AppFoodSearchPaginationCriteria(
                    query = query,
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
                "app foods pagination retrieved successfully",
                mapOf("app_foods_pagination" to pagination)
            )
        )
    }
}