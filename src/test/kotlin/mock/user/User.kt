package mock.user

import com.example.domain.User
import com.example.domain.UserRegisterData
import com.example.domain.UserType
import java.time.Instant
import java.util.*

fun randomUUIDAndChop(takeLast: Int = 8): Pair<UUID, String> {
    val userId = UUID.randomUUID()
    return userId to userId.toString().takeLast(takeLast)
}

fun buildUser(
    isPremium: Boolean = false,
    userType: UserType = UserType.USER
): User {
    val (userId, userIdChop) = randomUUIDAndChop()
    
    return User(
        id = userId,
        name = "user-$userIdChop",
        email = "user.${userIdChop}@example.com",
        passwordHash = "",
        isPremium = isPremium,
        createdAt = Instant.now(),
        updatedAt = null,
        type = userType
    )
}

fun buildUserRegisterData(
    userDiv: String = randomUUIDAndChop().second,
    password: String = "Pas123!!",
    userType: UserType = UserType.USER
) = UserRegisterData(
    name = "user-$userDiv",
    email = "user.${userDiv}@gmail.com",
    password = password,
    userType = userType
)