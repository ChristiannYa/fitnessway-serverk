package com.example.utils


import org.mindrot.jbcrypt.BCrypt
import java.security.MessageDigest

/**
 * Hash a password using BCrypt
 * @param password The plaintext literal
 * @return The BCrypt password hash
 */
fun hashPassword(password: String): String =
    BCrypt.hashpw(password, BCrypt.gensalt())

/**
 * Verify a password against a BCrypt hash
 * @param password The password literal
 * @param passwordHash The BCrypt password hash
 * @return true if password matches, false otherwise
 */
fun verifyPassword(password: String, passwordHash: String): Boolean =
    BCrypt.checkpw(password, passwordHash)

/**
 * Hash a token using SHA-256
 * Used for storing refresh tokens securely
 * @param token The token to hash
 * @return 64-character hex string
 */
fun hashToken(token: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hashBytes = digest.digest(token.toByteArray())

    return hashBytes.joinToString("") { "%02x".format(it) }
}

/**
 * Generate a secure random token
 * Useful for email verification, password reset, etc.
 * // @param length Number of bytes (default 32 = 64 hex chars)
 * // @return Random hex string
 */
/*
fun generateSecureToken(length: Int = 32): String {
    val random = java.security.SecureRandom()
    val bytes = ByteArray(length)

    random.nextBytes(bytes)

    return bytes.joinToString("") { "%02x".format(it) }
}

 */