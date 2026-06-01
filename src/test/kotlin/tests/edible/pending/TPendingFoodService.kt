package tests.edible.pending

import com.example.domain.PendingFood
import com.example.domain.UserPrincipal
import com.example.mappers.toAddRequest
import mock.food.buildPendingFoodCreateData
import tests.TAppTest

abstract class TPendingFoodService : TAppTest() {

    protected suspend fun submitPendingFood(
        userPrincipal: UserPrincipal,
        name: String = "food number ${(1000..9999).random()}"
    ): PendingFood = buildPendingFoodCreateData(userPrincipal.id, name)
        .let {
            pendingFoodService.add(
                req = it.toAddRequest(),
                userPrincipal = userPrincipal
            )
        }
}
