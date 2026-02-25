package com.example.routes.foods.pending

import com.example.config.PendingFoodServiceKey
import com.example.config.UserPrincipalKey
import com.example.domain.PendingFoodsPaginationCriteria
import com.example.dto.DtoRes
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.findByUserType() {
    get("/user-type") {
        val userPrincipal = call.attributes[UserPrincipalKey]
        val pendingFoodService = application.attributes[PendingFoodServiceKey]
        val limit = call.request.queryParameters["limit"]?.toIntOrNull()
        val offset = call.request.queryParameters["offset"]?.toLongOrNull()

        val paginationCriteria = PendingFoodsPaginationCriteria(userPrincipal.type)
        val pendingFoodsPagination = pendingFoodService.findByUserType(paginationCriteria, limit, offset)

        call.respond(
            HttpStatusCode.OK,
            DtoRes.success(
                "pending foods pagination retrieved successfully",
                mapOf("pending_foods_pagination" to pendingFoodsPagination)
            )
        )
    }
}