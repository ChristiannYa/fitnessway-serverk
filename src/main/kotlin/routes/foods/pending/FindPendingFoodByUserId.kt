package com.example.routes.foods.pending

import com.example.config.PendingFoodServiceKey
import com.example.domain.PaginationCriteria
import com.example.domain.PendingFoodsPaginationCriteria
import com.example.domain.extractPaginationOrThrow
import com.example.dto.DtoRes
import com.example.exception.InvalidUserIdException
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.findByUserId() {
    get("/user-id") {
        val pendingFoodService = application.attributes[PendingFoodServiceKey]

        val (limit, offset) = call.extractPaginationOrThrow()

        val userId = call.request.queryParameters["userId"]?.let {
            UUID.fromString(it)
        } ?: throw InvalidUserIdException()

        val pendingFoodsPagination = pendingFoodService.findPaginated(
            PaginationCriteria(
                data = PendingFoodsPaginationCriteria.ByUserId(userId),
                limit = limit,
                offset = offset
            )
        )

        call.respond(
            HttpStatusCode.OK,
            DtoRes.success(
                "pending foods pagination by user id retrieved successfully",
                mapOf("pending_foods_pagination" to pendingFoodsPagination)
            )
        )
    }
}