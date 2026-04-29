package com.example.routes.edible.pending

import com.example.config.PendingFoodServiceKey
import com.example.config.UserPrincipalKey
import com.example.domain.EdibleType
import com.example.domain.PaginationCriteria
import com.example.domain.PendingFoodsPaginationCriteria
import com.example.domain.UserScope
import com.example.dto.DtoRes
import com.example.exception.InvalidEdibleTypeException
import com.example.utils.extensions.extractPaginationOrThrow
import com.example.utils.extensions.extractPathParamOrThrow
import com.example.utils.toEnumOrThrow
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.findMyOwn() {
    get("/me/{edibleType}") {
        val userPrincipal = call.attributes[UserPrincipalKey]
        val pendingFoodsService = application.attributes[PendingFoodServiceKey]

        val (limit, offset) = call.extractPaginationOrThrow()

        val edibleType: EdibleType = call
            .extractPathParamOrThrow("edibleType")
            .toEnumOrThrow { InvalidEdibleTypeException() }

        val pendingFoodsPagination = pendingFoodsService.findPaginated(
            PaginationCriteria(
                data = PendingFoodsPaginationCriteria(
                    userId = userPrincipal.id,
                    userScope = UserScope.Id(userPrincipal.id),
                    edibleType = edibleType
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