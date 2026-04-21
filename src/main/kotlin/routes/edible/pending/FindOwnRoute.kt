package com.example.routes.foods.pending

import com.example.config.PendingFoodServiceKey
import com.example.config.UserPrincipalKey
import com.example.domain.PaginationCriteria
import com.example.domain.PendingFoodsPaginationCriteria
import com.example.domain.UserScope
import com.example.dto.DtoRes
import com.example.utils.extensions.extractPaginationOrThrow
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.findMyOwn() {
    // @TODO: Rename path to '/me'
    get("/my-own") {
        val userPrincipal = call.attributes[UserPrincipalKey]
        val pendingFoodsService = application.attributes[PendingFoodServiceKey]

        val (limit, offset) = call.extractPaginationOrThrow()

        val pendingFoodsPagination = pendingFoodsService.findPaginated(
            PaginationCriteria(
                data = PendingFoodsPaginationCriteria(
                    userId = userPrincipal.id,
                    userScope = UserScope.Id(userPrincipal.id)
                ),
                limit = limit,
                offset = offset
            )
        )

        call.respond(
            HttpStatusCode.OK,
            DtoRes.success(
                "my own pending foods pagination retrieved successfully",
                mapOf("pending_foods_pagination" to pendingFoodsPagination)
            )
        )
    }
}