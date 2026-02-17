package com.example.repository.refresh

import com.example.domain.RefreshToken
import com.example.domain.RefreshTokenCreate
import com.example.domain.TokenValidationResult
import java.util.*

interface IRefreshRepository {
    /**
     * Save a new refresh token
     *
     * @return `RefreshToken`
     */
    suspend fun save(refreshTokenCreate: RefreshTokenCreate): RefreshToken

    /**
     * Checks token validity (exists, not revoked, not expired, belongs to user)
     *
     * @return `TokenValidationResult` resulting in one of the following:
     * - `Valid`
     * - `NotFound`
     * - `Revoked`
     * - `Expired`
     */
    suspend fun validate(tokenHash: String, userId: UUID): TokenValidationResult

    /**
     * Find a specific token
     *
     * @return `RefreshToken` **if** found
     */
    suspend fun findByHash(tokenHash: String): RefreshToken?

    /**
     * Revoke a specific token
     *
     * @return `true` if revoked, `false` if it didn't exist
     */
    suspend fun revokeByHash(tokenHash: String): Boolean

    /**
     * Revoke **all** tokens by user Id
     *
     * @return tokens revoked count
     */
    suspend fun revokeByUserId(userId: UUID): Int

    /**
     * Update last used timestamp
     *
     * *Not critical if it fails*
     */
    suspend fun updateLastUsedTime(tokenHash: String)

    /**
     * Delete expired tokens (cleanup job)
     *
     * @return tokens deleted count
     */
    suspend fun deleteExpired(): Int
}