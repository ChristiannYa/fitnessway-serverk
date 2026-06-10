package mock.edible

import com.example.domain.*
import com.example.dto.EdibleWriteRequest
import java.util.*

fun buildPendingFoodCreateData(
    userId: UUID,
    name: String = "food number ${(1000..9999).random()}"
): PendingFoodCreate {
    // println("[buildPendingFoodCreateData] name: $name")

    return PendingFoodCreate(
        userId = userId,
        base = EdibleBase(
            name = name,
            brand = "kotlinized",
            amountPerServing = 42.0,
            servingUnit = ServingUnit.G
        ),
        nutrientList = listOf(
            NutrientIdWithAmount(
                id = 1,
                amount = 220.0
            ),
            NutrientIdWithAmount(
                id = 2,
                amount = 12.0
            ),
            NutrientIdWithAmount(
                id = 4,
                amount = 6.2
            ),
            NutrientIdWithAmount(
                id = 6,
                amount = 4.24
            ),
            NutrientIdWithAmount(
                id = 16,
                amount = 128.8
            )
        ),
        edibleType = EdibleType.FOOD
    )
}

fun buildEdibleRequestData(name: String): EdibleWriteRequest {
    return EdibleWriteRequest(
        base = EdibleBase(
            name = name,
            brand = "Edibly",
            amountPerServing = 42.0,
            servingUnit = ServingUnit.G
        ),
        nutrients = listOf(
            NutrientIdWithAmount(
                id = 1,
                amount = 220.0
            ),
            NutrientIdWithAmount(
                id = 2,
                amount = 12.0
            ),
            NutrientIdWithAmount(
                id = 4,
                amount = 6.2
            ),
            NutrientIdWithAmount(
                id = 6,
                amount = 4.24
            ),
            NutrientIdWithAmount(
                id = 16,
                amount = 128.8
            )
        ),
        edibleType = EdibleType.FOOD.toString()
    )
}