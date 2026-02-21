package com.example.mapping

import org.jetbrains.exposed.sql.Table

object UserWalletsTable : Table("user_wallets") {
    val userId = reference("user_id", UsersTable)
    val amount = decimal("amount", 12, 2)

    override val primaryKey = PrimaryKey(userId)
}