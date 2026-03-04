package com.example.routes.foods.pending

import com.example.config.PendingFoodServiceKey
import com.example.domain.PaginationCriteria
import com.example.domain.PendingFoodsPaginationCriteria
import com.example.domain.UserType
import com.example.domain.extractPaginationOrThrow
import com.example.dto.DtoRes
import com.example.exception.InvalidUserTypeException
import com.example.utils.asEnum
import com.example.utils.enumContains
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.findByUserType() {
    get("/user-type") {
        val pendingFoodService = application.attributes[PendingFoodServiceKey]

        val (limit, offset) = call.extractPaginationOrThrow()

        val userType = call.request.queryParameters["userType"]
            ?.takeIf { enumContains<UserType>(it) }
            ?.asEnum<UserType>()
            ?: throw InvalidUserTypeException()

        val pendingFoodsPagination = pendingFoodService.findPaginated(
            PaginationCriteria(
                data = PendingFoodsPaginationCriteria.ByUserType(userType),
                limit = limit,
                offset = offset
            )
        )

        call.respond(
            HttpStatusCode.OK,
            DtoRes.success(
                "pending foods pagination by user type retrieved successfully",
                mapOf("pending_foods_pagination" to pendingFoodsPagination)
            )
        )
    }
}