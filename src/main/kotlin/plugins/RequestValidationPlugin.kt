package com.example.plugins

import com.example.dto.LoginRequestDto
import com.example.dto.LogoutRequestDto
import com.example.dto.RefreshRequestDto
import com.example.dto.RegisterRequestDto
import com.example.routes.auth.validateLoginRequest
import com.example.routes.auth.validateLogoutRequest
import com.example.routes.auth.validateRefreshRequest
import com.example.routes.auth.validateRegisterRequest
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*

fun Application.configureRequestValidation() {
    install(RequestValidation) {
        // --------------
        // Authentication
        // --------------
        validate<RegisterRequestDto> { validateRegisterRequest(it) }
        validate<RefreshRequestDto> { validateRefreshRequest(it) }
        validate<LoginRequestDto> { validateLoginRequest(it) }
        validate<LogoutRequestDto> { validateLogoutRequest(it) }
    }
}