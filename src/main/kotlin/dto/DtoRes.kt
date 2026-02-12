package com.example.dto

import kotlinx.serialization.Serializable

object DtoRes {
    @Serializable
    data class Response<T>(
        val success: Boolean,
        val message: String,
        val data: Map<String, T>?
    )

    fun <T> success(message: String, data: Map<String, T>? = null) = Response(true, message, data)
    fun error(message: String) = Response<Nothing>(false, message, data = null)
}