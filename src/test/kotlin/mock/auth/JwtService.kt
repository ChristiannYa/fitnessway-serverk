package mock.auth

import com.auth0.jwt.interfaces.DecodedJWT
import com.example.service.JwtService
import io.mockk.every
import io.mockk.mockk
import java.util.*

fun createJwtService(): JwtService {
    return mockk<JwtService>(relaxed = true).apply {
        // Generate unique tokens for each call
        every { generateJwtToken(any(), any(), any()) } answers { "fake-token-${UUID.randomUUID()}" }

        // Default token verification - returns a mock DecodedJWT
        every { verifyToken(any(), any()) } answers {
            mockk<DecodedJWT>()
        }

        // Default userId extraction - returns a random UUID
        every { extractUserId(any(), any()) } returns UUID.randomUUID()
    }
}