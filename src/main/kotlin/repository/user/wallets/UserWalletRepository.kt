package com.example.repository.user.wallets

import com.example.domain.UserAddCurrency
import com.example.mapping.U
import com.example.mapping.UCTDao
import com.example.mapping.UW
import com.example.utils.suspendTransaction
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.util.*

class UserWalletRepository : IUserWalletRepository {
    override suspend fun getBalance(userId: UUID): Double? = suspendTransaction {
        UW.select(UW.amount)
            .where { UW.userId eq userId }
            .singleOrNull()
            ?.get(UW.amount)
            ?.toDouble()
    }

    override suspend fun createWallet(userId: UUID) {
        UW.insert {
            it[UW.userId] = userId
            it[UW.amount] = 0.00.toBigDecimal()
        }
    }

    override suspend fun addCurrency(currencyToAdd: UserAddCurrency): Unit = suspendTransaction {
        currencyToAdd.let {
            // Update user existing balance
            UW.update(where = { UW.userId eq it.userId }) { t ->
                t[UW.amount] = UW.amount plus it.amount.toBigDecimal()
            }

            // Record the transaction
            UCTDao.new {
                this.userId = EntityID(it.userId, U)
                this.amount = it.amount.toBigDecimal()
                this.transactionType = it.transactionType
                this.createdAt = Instant.now()
            }
        }
    }
}