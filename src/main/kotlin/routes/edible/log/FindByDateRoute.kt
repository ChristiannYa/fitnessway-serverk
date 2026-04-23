package com.example.routes.edible.log

import com.example.config.EdibleLogServiceKey
import com.example.config.UserPrincipalKey
import com.example.dto.DtoRes
import com.example.utils.extensions.extractPathParamOrThrow
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.findByDate() {
    get("/date/{date}") {
        val userPrincipal = call.attributes[UserPrincipalKey]
        val foodLogService = application.attributes[EdibleLogServiceKey]

        val date = call.extractPathParamOrThrow("date")
        val foodLogs = foodLogService.findByDate(userPrincipal, date)

        call.respond(
            HttpStatusCode.OK,
            DtoRes.success(
                "food logs retrieved successfully",
                mapOf("food_logs" to foodLogs)
            )
        )
    }
}