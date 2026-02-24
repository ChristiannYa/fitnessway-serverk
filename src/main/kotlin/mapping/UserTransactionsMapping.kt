package com.example.mapping

import com.example.domain.UserTransactionType
import com.example.utils.pgEnum
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object UCT : IntIdTable("user_currency_transactions") {
    val userId = reference("user_id", U)
    val amount = decimal("amount", 12, 4)
    val transactionType = pgEnum<UserTransactionType>("transaction_type", "user_currency_transaction_type")
    val createdAt = timestamp("created_at")
}

class UCTDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UCTDao>(UCT)

    var userId by UCT.userId
    var amount by UCT.amount
    var transactionType by UCT.transactionType
    var createdAt by UCT.createdAt
}