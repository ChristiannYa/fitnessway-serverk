package com.example.mappers

import com.example.domain.User
import com.example.domain.UserPrincipal

fun User.toPrincipal() = UserPrincipal(
    this.id,
    this.type,
    this.isPremium,
    this.timezone
)

