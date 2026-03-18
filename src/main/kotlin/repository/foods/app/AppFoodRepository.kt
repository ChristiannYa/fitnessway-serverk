package com.example.repository.foods.app

import com.example.domain.*
import com.example.mapping.*
import com.example.repository.foods.queryNutrientsForFood
import com.example.utils.similarity
import com.example.utils.suspendTransaction
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.lowerCase
import java.util.*

class AppFoodRepository : IAppFoodRepository {
    override suspend fun findById(id: Int, userId: UUID): AppFood? = suspendTransaction {
        val afDao = AFDao.findById(id)
            ?: return@suspendTransaction null

        val nutrients = queryNutrientsForFood(UPFN, afDao.id.value, userId)
        afDao.toDomain(nutrients)
    }

    override suspend fun create(foodToCreate: AppFoodCreate): Int = suspendTransaction {
        val afDao = foodToCreate.food.base.let { foodBase ->
            AFDao.new {
                this.name = foodBase.name
                this.brand = foodBase.brand.toString()
                this.amountPerServing = foodBase.amountPerServing.toBigDecimal()
                this.servingUnit = foodBase.servingUnit
                this.createdBy = EntityID(foodToCreate.createdBy, U)
            }
        }

        AFN.batchInsert(foodToCreate.food.nutrients) { nutrient ->
            this[AFN.foodId] = afDao.id.value
            this[AFN.nutrientId] = nutrient.nutrientId
            this[AFN.amount] = nutrient.amount.toBigDecimal()
        }

        afDao.id.value
    }

    override suspend fun isDuplicate(food: FoodInformation<NutrientIdWithAmount>): Boolean = suspendTransaction {
        val appFoodBaseDaos = AFDao.find {
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

    /*
    override suspend fun search(query: String): List<FoodSearchResult> = suspendTransaction {
        AFDao.find { AF.name.lowerCase() like "%${query.lowercase()}%" }
            .orderBy(similarity(AF.name, query) to SortOrder.DESC)
            .map { dao ->
                FoodSearchResult(
                    id = dao.id.value,
                    base = FoodBase(
                        name = dao.name,
                        brand = dao.brand,
                        amountPerServing = dao.amountPerServing.toDouble(),
                        servingUnit = dao.servingUnit
                    )
                )
            }
    }

     */

    override suspend fun search(
        criteria: PaginationCriteria<AppFoodSearchPaginationCriteria>
    ): PaginationQuery<FoodSearchResult> = suspendTransaction {
        val query = criteria.data.query
        val matched = AFDao.find {
            AF.name.lowerCase() like "%${query.lowercase()}%"
        }

        val data = matched
            .orderBy(similarity(AF.name, query) to SortOrder.DESC)
            .map { dao ->
                FoodSearchResult(
                    id = dao.id.value,
                    base = FoodBase(
                        name = dao.name,
                        brand = dao.brand,
                        amountPerServing = dao.amountPerServing.toDouble(),
                        servingUnit = dao.servingUnit
                    )
                )
            }

        PaginationQuery(data, matched.count())
    }
}