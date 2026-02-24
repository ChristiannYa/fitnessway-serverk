package com.example.repository.foods.app

import com.example.domain.AppFoodCreate
import com.example.domain.FoodInformation
import com.example.domain.NutrientIdWithAmount
import com.example.mapping.AppFoodDao
import com.example.repository.AF
import com.example.repository.AFN
import com.example.repository.U
import com.example.utils.suspendTransaction
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert

class AppFoodRepository : IAppFoodRepository {
    override suspend fun create(foodToCreate: AppFoodCreate): Int = suspendTransaction {
        val appFoodDao = foodToCreate.food.base.let { foodBase ->
            AppFoodDao.new {
                this.name = foodBase.name
                this.brand = foodBase.brand.toString()
                this.amountPerServing = foodBase.amountPerServing.toBigDecimal()
                this.servingUnit = foodBase.servingUnit
                this.createdBy = EntityID(foodToCreate.createdBy, U)
            }
        }

        AFN.batchInsert(foodToCreate.food.nutrients) { nutrient ->
            this[AFN.foodId] = appFoodDao.id.value
            this[AFN.nutrientId] = nutrient.nutrientId
            this[AFN.amount] = nutrient.amount.toBigDecimal()
        }

        appFoodDao.id.value
    }

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
                .where { AFN.foodId eq appFoodDao.id }
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