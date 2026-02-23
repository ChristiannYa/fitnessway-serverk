package com.example.repository.foods.app

import com.example.domain.FoodInformation
import com.example.domain.NutrientIdWithAmount
import com.example.mapping.AppFoodDao
import com.example.repository.AF
import com.example.repository.AFN
import com.example.utils.suspendTransaction
import org.jetbrains.exposed.sql.and

class AppFoodRepository : IAppFoodRepository {
    override suspend fun isDuplicate(food: FoodInformation<NutrientIdWithAmount>): Boolean = suspendTransaction {
        val appFoodBaseDaos = AppFoodDao.find {
            (AF.name eq food.base.name) and
            (AF.brand eq food.base.brand.toString()) and
            (AF.amountPerServing eq food.base.amountPerServing.toBigDecimal()) and
            (AF.servingUnit eq food.base.servingUnit)
        }

        appFoodBaseDaos.any { appFoodDao ->
            val appFoodDaoNutrients = AFN
                .select(AFN.nutrientId, AFN.amount)
                .where { AFN.appFoodId eq appFoodDao.id }
                .map { row ->
                    NutrientIdWithAmount(
                        nutrientId = row[AFN.nutrientId].value,
                        amount = row[AFN.amount].toDouble()
                    )
                }

            food.nutrients == appFoodDaoNutrients
        }
    }
}