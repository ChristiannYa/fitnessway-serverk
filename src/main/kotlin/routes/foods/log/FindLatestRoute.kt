package com.example.routes.foods.log

import com.example.config.FoodLogServiceKey
import com.example.config.UserPrincipalKey
import com.example.domain.PaginationCriteria
import com.example.domain.RecentlyLoggedFoodsPaginationCriteria
import com.example.dto.DtoRes
import com.example.utils.extensions.extractPaginationOrThrow
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.findLatest() {
    get("/latest") {
        val userPrincipal = call.attributes[UserPrincipalKey]
        val foodLogService = application.attributes[FoodLogServiceKey]

        val (limit, offset) = call.extractPaginationOrThrow()

        val pagination = foodLogService.findLatest(
            PaginationCriteria(
                data = RecentlyLoggedFoodsPaginationCriteria(
                    userId = userPrincipal.id
                ),
                limit = limit,
                offset = offset
            )
        )

        call.respond(
            HttpStatusCode.OK,
            DtoRes.success(
                "recently logged foods pagination retrieved successfully",
                mapOf("recent_logged_foods_pagination" to pagination)
            )
        )
    }
}