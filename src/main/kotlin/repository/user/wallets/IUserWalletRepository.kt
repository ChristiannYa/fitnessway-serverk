package com.example.repository.user.wallets

import com.example.domain.UserAddCurrency
import java.util.*

interface IUserWalletRepository {
    /**
     * Gets the current balance for a user
     * Returns null if wallet doesn't exist
     */
    suspend fun getBalance(userId: UUID): Double?

    /**
     * Adds currency to a user's wallet
     * Creates wallet if it doesn't exist
     */
    suspend fun addCurrency(currencyToAdd: UserAddCurrency)

    /**
     * Creates a wallet for a new user
     */
    suspend fun createWallet(userId: UUID)
}