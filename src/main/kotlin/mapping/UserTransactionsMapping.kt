package com.example.mapping

import com.example.domain.UserTransactionType
import com.example.utils.pgEnum
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object UserCurrencyTransactionsTable : IntIdTable("user_currency_transactions") {
    val userId = reference("user_id", UsersTable)
    val amount = decimal("amount", 12, 4)
    val transactionType = pgEnum<UserTransactionType>("transaction_type", "user_currency_transaction_type")
    val createdAt = timestamp("created_at")
}

class UserCurrencyTransactionDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserCurrencyTransactionDao>(UserCurrencyTransactionsTable)

    var userId by UserCurrencyTransactionsTable.userId
    var amount by UserCurrencyTransactionsTable.amount
    var transactionType by UserCurrencyTransactionsTable.transactionType
    var createdAt by UserCurrencyTransactionsTable.createdAt
}