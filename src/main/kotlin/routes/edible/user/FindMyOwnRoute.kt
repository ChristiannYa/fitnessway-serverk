package com.example.routes.edible.user

import com.example.config.UserEdibleServiceKey
import com.example.config.UserPrincipalKey
import com.example.domain.EdibleType
import com.example.domain.PaginationCriteria
import com.example.domain.UserEdiblesPaginationCriteria
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
        val userEdibleService = application.attributes[UserEdibleServiceKey]

        val (limit, offset) = call.extractPaginationOrThrow()

        val edibleType = call
            .extractPathParamOrThrow("edibleType")
            .toEnumOrThrow<EdibleType> { InvalidEdibleTypeException() }

        val userEdiblesPagination = userEdibleService.findPagination(
            PaginationCriteria(
                data = UserEdiblesPaginationCriteria(
                    userId = userPrincipal.id,
                    edibleType = edibleType
                ),
                limit = limit,
                offset = offset
            ),
            isUserPremium = userPrincipal.isPremium
        )

        call.respond(
            HttpStatusCode.OK,
            DtoRes.success(
                "user own ${edibleType.name.lowercase()}s pagination retrieved successfully",
                mapOf("user_edibles_pagination" to userEdiblesPagination)
            )
        )
    }
}