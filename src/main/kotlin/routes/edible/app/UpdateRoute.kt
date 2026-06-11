package com.example.routes.edible.app

import com.example.config.AppEdibleServiceKey
import com.example.config.UserPrincipalKey
import com.example.dto.AppEdibleWriteRequest
import com.example.dto.DtoRes
import com.example.exception.InvalidIdException
import com.example.utils.extensions.extractQueryParamOrThrow
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.update() {
    put {
        val req = call.receive<AppEdibleWriteRequest>()
        val appEdibleService = application.attributes[AppEdibleServiceKey]
        val userPrincipal = call.attributes[UserPrincipalKey]

        val edibleId = call
            .extractQueryParamOrThrow("edibleId")
            .toIntOrNull()
            ?: throw InvalidIdException("app edible")

        appEdibleService.update(
            userId = userPrincipal.id,
            edibleId = edibleId,
            updateInfo = req
        )

        call.respond(
            HttpStatusCode.OK,
            DtoRes.success<Unit>("app edible #$edibleId updated successfully")
        )
    }
}