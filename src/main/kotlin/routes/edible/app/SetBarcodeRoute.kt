package com.example.routes.edible.app

import com.example.config.AppEdibleServiceKey
import com.example.dto.AddBarcodeRequest
import com.example.dto.DtoRes
import com.example.utils.toEnum
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.setBarcode() {
    post("/barcode") {
        val req = call.receive<AddBarcodeRequest>()
        val appFoodService = application.attributes[AppEdibleServiceKey]

        appFoodService.setBarcode(
            req.barcode,
            req.edibleId,
            req.edibleType.toEnum()
        )

        call.respond(
            HttpStatusCode.OK,
            DtoRes.success<Unit>("barcode added successfully")
        )
    }
}
