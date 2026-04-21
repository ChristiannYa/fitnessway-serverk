package com.example.routes.foods.pending

import com.example.config.PendingFoodServiceKey
import com.example.config.UserPrincipalKey
import com.example.dto.DtoRes
import com.example.utils.extensions.extractPathParamOrThrow
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.dismissReview() {
    delete("/dismiss-review/{pendingFoodId}") {
        val userPrincipal = call.attributes[UserPrincipalKey]
        val pendingFoodService = application.attributes[PendingFoodServiceKey]

        val pendingFoodId = call.extractPathParamOrThrow("pendingFoodId").toIntOrNull()
        pendingFoodService.dismissReview(pendingFoodId ?: 0, userPrincipal.id)

        call.respond(
            HttpStatusCode.NoContent,
            DtoRes.success<Unit>("review successfully dismissed")
        )
    }
}