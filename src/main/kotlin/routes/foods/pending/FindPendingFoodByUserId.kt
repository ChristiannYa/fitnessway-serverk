package com.example.routes.foods.pending

import com.example.config.PendingFoodServiceKey
import com.example.domain.*
import com.example.dto.DtoRes
import com.example.exception.InvalidPendingFoodStatusException
import com.example.exception.InvalidUserIdException
import com.example.utils.toEnumOrThrow
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

        val pendingStatus = call.request.queryParameters["pendingStatus"]
            ?.toEnumOrThrow<PendingFoodStatus> { InvalidPendingFoodStatusException() }

        val pendingFoodsPagination = pendingFoodService.findPaginated(
            PaginationCriteria(
                data = PendingFoodsPaginationCriteria(
                    userScope = UserScope.Id(userId),
                    status = pendingStatus
                ),
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