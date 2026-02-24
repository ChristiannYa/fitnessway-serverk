package com.example.mapping

import org.jetbrains.exposed.sql.Table

object UW : Table("user_wallets") {
    val userId = reference("user_id", U)
    val amount = decimal("amount", 12, 2)

    override val primaryKey = PrimaryKey(userId)
}