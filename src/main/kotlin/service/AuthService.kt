package com.example.service

import com.example.config.TokenDuration
import com.example.domain.*
import com.example.exception.InvalidCredentialsException
import com.example.exception.UserAlreadyExistsException
import com.example.exception.UserNotFoundException
import com.example.repository.refresh.IRefreshRepository
import com.example.repository.user.IUserRepository
import com.example.repository.user.wallets.UserWalletRepository
import com.example.utils.hashPassword
import com.example.utils.hashToken
import com.example.utils.suspendTransaction
import com.example.utils.verifyPassword
import mu.KotlinLogging
import java.time.Instant
import java.util.*

private val logger = KotlinLogging.logger {}

class AuthService(
    private val userRepository: IUserRepository,
    private val refreshRepository: IRefreshRepository,
    private val jwtService: JwtService,
    private val userWalletRepository: UserWalletRepository
) {
    suspend fun login(userLoginData: UserLoginData, deviceName: String): TokenStrings = suspendTransaction {
        // Find user by email
        val user = userRepository.findByEmail(userLoginData.email)
            ?: throw InvalidCredentialsException()

        // Verify raw password matches user's password
        if (!verifyPassword(userLoginData.password, user.passwordHash))
            throw InvalidCredentialsException()

        // Generate tokens
        val tokens = generateTokens(user.toPrincipal())

        // Save refresh token in database
        saveRefreshTokenInDb(user.id, tokens.refreshToken, deviceName)

        // Return token pair
        tokens
    }

    suspend fun register(userRegisterData: UserRegisterData, deviceName: String): TokenStrings {
        // Check if user exists
        if (userRepository.findByEmail(userRegisterData.email) != null)
            throw UserAlreadyExistsException()

        // Hash password for secure database storage
        val passwordHash = hashPassword(userRegisterData.password)

        return suspendTransaction {
            // Create user
            val user = userRepository.create(
                UserCreate(
                    userRegisterData.name,
                    userRegisterData.email,
                    passwordHash,
                    userRegisterData.userType
                )
            )

            // Create user wallet
            userWalletRepository.createWallet(user.id)

            // Generate tokens
            val tokens = generateTokens(user.toPrincipal())

            // Save refresh token in database
            saveRefreshTokenInDb(user.id, tokens.refreshToken, deviceName)

            // Return token pair
            tokens
        }
    }

    suspend fun refreshAccessToken(refreshTokenString: String): String {
        // Verify JWT signature
        val decodedJwt = jwtService.verifyToken(refreshTokenString, TokenType.REFRESH)

        // Extract user id from the decoded JWT
        val userId = jwtService.extractUserId(decodedJwt, TokenType.REFRESH)

        // Hash refresh token
        val refreshTokenHash = hashToken(refreshTokenString)

        // Obtain token validation result
        val validationResult = refreshRepository.validate(refreshTokenHash, userId)

        // Obtain refresh token if validation result passed
        val refreshToken = validationResult.getTokenOrThrow()

        // Get current user data
        val user = userRepository.findById(userId)
            ?: throw UserNotFoundException("user not found while refreshing access token")

        // Update last used time before generating access token, that way if generation
        // fails the timestamps will show that an attempt was made
        refreshRepository.updateLastUsedTime(refreshToken.hash)

        // Generate new access token
        return jwtService.generateAccessToken(user.toPrincipal())
    }

    private fun generateTokens(userPrincipal: UserPrincipal): TokenStrings {
        val accessToken = jwtService.generateAccessToken(userPrincipal)
        val refreshToken = jwtService.generateRefreshToken(userPrincipal.id)

        return TokenStrings(accessToken, refreshToken)
    }

    private suspend fun saveRefreshTokenInDb(userId: UUID, refreshToken: String, deviceName: String) {
        // Hash the token for secure database storage
        val refreshTokenHash = hashToken(refreshToken)

        // Set refresh token expiration date
        val expiresAt = Instant.now().plus(TokenDuration.REFRESH_TOKEN)

        // Save refresh token in the database
        refreshRepository.save(
            RefreshTokenCreate(
                hash = refreshTokenHash,
                userId = userId,
                expiresAt = expiresAt,
                deviceName = deviceName
            )
        )
    }

    suspend fun logout(refreshToken: String) {
        val refreshTokenHash = hashToken(refreshToken)
        val isRefreshTokenRevoked = refreshRepository.revokeByHash(refreshTokenHash)

        if (!isRefreshTokenRevoked) {
            logger.warn("Attempted to revoke non-existent refresh token")
        }
    }
}