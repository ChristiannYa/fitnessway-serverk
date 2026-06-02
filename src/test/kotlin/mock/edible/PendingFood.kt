package mock.edible

import com.example.domain.*
import com.example.dto.EdibleAddRequest
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
                nutrientId = 1,
                amount = 220.0
            ),
            NutrientIdWithAmount(
                nutrientId = 2,
                amount = 12.0
            ),
            NutrientIdWithAmount(
                nutrientId = 4,
                amount = 6.2
            ),
            NutrientIdWithAmount(
                nutrientId = 6,
                amount = 4.24
            ),
            NutrientIdWithAmount(
                nutrientId = 16,
                amount = 128.8
            )
        ),
        edibleType = EdibleType.FOOD
    )
}

fun buildEdibleRequestData(name: String): EdibleAddRequest {
    return EdibleAddRequest(
        base = EdibleBase(
            name = name,
            brand = "Edibly",
            amountPerServing = 42.0,
            servingUnit = ServingUnit.G
        ),
        nutrients = listOf(
            NutrientIdWithAmount(
                nutrientId = 1,
                amount = 220.0
            ),
            NutrientIdWithAmount(
                nutrientId = 2,
                amount = 12.0
            ),
            NutrientIdWithAmount(
                nutrientId = 4,
                amount = 6.2
            ),
            NutrientIdWithAmount(
                nutrientId = 6,
                amount = 4.24
            ),
            NutrientIdWithAmount(
                nutrientId = 16,
                amount = 128.8
            )
        ),
        edibleType = EdibleType.FOOD.toString()
    )
}