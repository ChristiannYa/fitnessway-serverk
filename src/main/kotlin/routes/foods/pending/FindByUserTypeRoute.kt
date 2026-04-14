package com.example.routes.foods.pending

import com.example.config.PendingFoodServiceKey
import com.example.config.UserPrincipalKey
import com.example.domain.*
import com.example.dto.DtoRes
import com.example.exception.InvalidPendingFoodStatusException
import com.example.exception.InvalidUserTypeException
import com.example.utils.extensions.extractPaginationOrThrow
import com.example.utils.extensions.extractQueryParamOrNull
import com.example.utils.extensions.extractQueryParamOrThrow
import com.example.utils.toEnumOrThrow
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.findByUserType() {
    get("/user-type") {
        val userPrincipal = call.attributes[UserPrincipalKey]
        val pendingFoodService = application.attributes[PendingFoodServiceKey]

        val (limit, offset) = call.extractPaginationOrThrow()

        val userType = call.extractQueryParamOrThrow("userType")
            .toEnumOrThrow<UserType> { InvalidUserTypeException() }

        val pendingStatus = call.extractQueryParamOrNull("pendingStatus")
            ?.toEnumOrThrow<PendingFoodStatus> { InvalidPendingFoodStatusException() }

        val pendingFoodsPagination = pendingFoodService.findPaginated(
            PaginationCriteria(
                data = PendingFoodsPaginationCriteria(
                    userId = userPrincipal.id,
                    userScope = UserScope.Type(userType),
                    status = pendingStatus
                ),
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