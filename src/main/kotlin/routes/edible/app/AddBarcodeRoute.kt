package com.example.routes.edible.app

import com.example.config.AppFoodServiceKey
import com.example.dto.AddBarcodeRequest
import com.example.dto.DtoRes
import com.example.validation.toValidationResult
import com.example.validation.validate
import io.ktor.http.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.addBarcode() {
    post("/barcode") {
        val req = call.receive<AddBarcodeRequest>()
        val appFoodService = application.attributes[AppFoodServiceKey]

        appFoodService.addBarcode(req.barcode, req.edibleId)

        call.respond(
            HttpStatusCode.OK,
            DtoRes.success<Unit>("barcode added successfully")
        )
    }
}

fun AddBarcodeRequest.validate(): ValidationResult {

    this.barcode
        .validate("barcode") {
            it.isProvided()
        }
        .toValidationResult()
        .let { if (it is ValidationResult.Invalid) return it }

    this.edibleId
        .toString()
        .validate("edible id") {
            it.isPositiveDouble()
        }
        .toValidationResult()
        .let { if (it is ValidationResult.Invalid) return it }

    return ValidationResult.Valid
}