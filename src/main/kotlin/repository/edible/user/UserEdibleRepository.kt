package com.example.repository.edible.user

import com.example.domain.*
import com.example.mapping.UE
import com.example.mapping.UEDao
import com.example.mapping.UEN
import com.example.repository.foods.queryNutrientsForFood
import com.example.repository.foods.queryNutrientsForFoods
import com.example.utils.suspendTransaction
import org.jetbrains.exposed.sql.and
import java.util.*

class UserEdibleRepository : IUserEdibleRepository {

    override suspend fun findById(
        id: Int,
        userId: UUID,
        edibleType: EdibleType
    ): Pair<UEDao, List<NutrientDataAmount>>? = suspendTransaction {
        val ueDao = UEDao.find {
            (UE.userId eq userId) and
            (UE.id eq id) and
            (UE.edibleType eq edibleType)
        }.firstOrNull() ?: return@suspendTransaction null

        val nutrients = queryNutrientsForFood(UEN, ueDao.id.value, userId)

        ueDao to nutrients
    }

    override suspend fun findPagination(
        paginationCriteria: PaginationCriteria<UserEdiblesPaginationCriteria>
    ): Result<PaginationQuery<Pair<UEDao, List<NutrientDataAmount>>>> = suspendTransaction {
        val ueDaos = UEDao.find {
            (UE.userId eq paginationCriteria.data.userId) and
            (UE.edibleType eq paginationCriteria.data.edibleType)
        }

        val queryCount = ueDaos.count()

        val ueDaosPaginationList = ueDaos
            .limit(paginationCriteria.limit)
            .offset(paginationCriteria.offset)
            .toList()

        val nutrients = queryNutrientsForFoods(
            UEN,
            ueDaos.map { it.id.value },
            paginationCriteria.data.userId
        )

        val foods = ueDaosPaginationList
            .associateBy { it.id.value }
            .map { (id, dao) ->
                nutrients[id]
                    ?.let { dao to it }
                    ?: return@suspendTransaction Result.failure(
                        IllegalStateException(
                            "user edible (${paginationCriteria.data.edibleType}) dao #$id's nutrients not found"
                        )
                    )
            }

        Result.success(PaginationQuery(foods, queryCount))
    }
}