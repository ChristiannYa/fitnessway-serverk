package mock.food

import com.example.domain.*
import java.util.*

fun buildPendingFoodCreateData(
    author: UUID = UUID.randomUUID(),
    name: String = "food number ${(1000..9999).random()}"
) = PendingFoodCreate(
    foodInformation = FoodInformation(
        base = FoodBase(
            name = name,
            brand = "kotlinized",
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
        )
    ),
    author = author
)