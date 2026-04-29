package com.example.routes.edible.pending

import com.example.config.PendingFoodServiceKey
import com.example.config.UserPrincipalKey
import com.example.domain.*
import com.example.dto.DtoRes
import com.example.exception.InvalidEdibleTypeException
import com.example.exception.InvalidIdException
import com.example.exception.InvalidPendingFoodStatusException
import com.example.utils.extensions.extractPaginationOrThrow
import com.example.utils.extensions.extractPathParamOrThrow
import com.example.utils.extensions.extractQueryParamOrNull
import com.example.utils.toEnumOrThrow
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.findByUserId() {
    get("/user-id") {
        val userPrincipal = call.attributes[UserPrincipalKey]
        val pendingFoodService = application.attributes[PendingFoodServiceKey]

        val (limit, offset) = call.extractPaginationOrThrow()

        val userId = call.extractPathParamOrThrow("userId").let {
            UUID.fromString(it)
        } ?: throw InvalidIdException("user")

        val pendingStatus: PendingFoodStatus? = call
            .extractQueryParamOrNull("pendingStatus")
            ?.toEnumOrThrow { InvalidPendingFoodStatusException() }

        val edibleType: EdibleType? = call
            .extractQueryParamOrNull("edibleType")
            ?.toEnumOrThrow { InvalidEdibleTypeException() }

        val pendingFoodsPagination = pendingFoodService.findPaginated(
            PaginationCriteria(
                data = PendingFoodsPaginationCriteria(
                    userId = userPrincipal.id,
                    userScope = UserScope.Id(userId),
                    status = pendingStatus,
                    edibleType = edibleType
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