package mock.auth

import com.auth0.jwt.interfaces.DecodedJWT
import com.example.service.JwtService
import io.mockk.every
import io.mockk.mockk
import java.util.*

fun createJwtService(): JwtService {
    return mockk<JwtService>(relaxed = true).apply {
        // Generate unique tokens for each call
        every { generateAccessToken(any()) } answers { "fake-access-token-${UUID.randomUUID()}" }
        every { generateRefreshToken(any()) } answers { "fake-refresh-token-${UUID.randomUUID()}" }

        // Default token verification - returns a mock DecodedJWT
        every { verifyToken(any(), any()) } answers {
            mockk<DecodedJWT>()
        }

        // Default userId extraction - returns a random UUID
        // Can be overridden per test with specific UUIDs
        every { extractUserId(any(), any()) } returns UUID.randomUUID()
    }
}