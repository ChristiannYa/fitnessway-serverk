package com.example.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserTimezoneSetRequest(val timezone: String)