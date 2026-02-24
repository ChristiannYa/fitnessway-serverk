package com.example.routes.foods.pending

import com.example.config.PendingFoodServiceKey
import com.example.config.UserPrincipalKey
import com.example.dto.DtoRes
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.dismissReview() {
    delete("/dismiss-review/{pendingFoodId}") {
        val pendingFoodId = call.parameters["pendingFoodId"]?.toIntOrNull()
        val userPrincipal = call.attributes[UserPrincipalKey]
        val pendingFoodService = application.attributes[PendingFoodServiceKey]

        pendingFoodService.dismissReview(pendingFoodId, userPrincipal.id)

        call.respond(
            HttpStatusCode.NoContent,
            DtoRes.success<Unit>("review successfully dismissed")
        )
    }
}