package utils

import com.example.domain.TokenStrings
import com.example.domain.User
import com.example.domain.UserRegisterData
import com.example.repository.user.UserRepository
import com.example.service.AuthService

suspend fun createUserAndGetData(
    authService: AuthService,
    userRepository: UserRepository,
    userRegisterData: UserRegisterData = mock.user.buildUserRegisterData(),
    deviceName: String = "HP Envy x360 TEST"
): Pair<User, TokenStrings> {
    val userTokens = authService.register(userRegisterData, deviceName)
    val user = userRepository.findByEmail(userRegisterData.email)!!
    return user to userTokens
}