package com.example.service

import com.example.domain.*
import com.example.dto.EdibleLogAddRequest
import com.example.dto.FoodInformationDto
import com.example.exception.EdibleNotFoundException
import com.example.exception.FoodLogNotFoundException
import com.example.exception.NutrientIntakesNotFoundException
import com.example.mappers.toCategory
import com.example.mappers.toCategoryGroups
import com.example.mapping.toDto
import com.example.repository.edible.log.IFoodLogRepository
import com.example.repository.nutrient.intake.INutrientIntakeRepository
import com.example.utils.date_time.TimeConverter
import com.example.utils.suspendTransaction
import com.example.utils.toEnum
import io.ktor.server.plugins.*
import java.util.*

class EdibleLogService(
    private val foodLogRepository: IFoodLogRepository,
    private val nutrientIntakeRepository: INutrientIntakeRepository,
    private val timeConverter: TimeConverter,
) {
    private fun EdibleLogBuildData.build(): FoodLog =
        this.dao.toDto(
            userEdibleSnapshotStatus = this.snapshotStatus,
            foodInformationDto = FoodInformationDto(
                base = this.edibleBase,
                nutrients = this.nutrientList.toCategoryGroups()
            )
        )

    suspend fun findById(
        id: Int,
        userId: UUID,
    ): FoodLog? = suspendTransaction {
        val buildData = foodLogRepository
            .findById(id, userId)
            ?: return@suspendTransaction null

        buildData.build()
    }

    suspend fun findByDate(userPrincipal: UserPrincipal, date: String): FoodLogsCategorized {
        val range = timeConverter
            .toUtcRangeResult(date, userPrincipal.timezone)
            .getOrElse { ex -> throw BadRequestException(ex.message ?: "user time convertion failed") }

        return foodLogRepository
            .findByDate(userPrincipal.id, userPrincipal.isPremium, range)
            .getOrElse { throw it }
            .map { it.build() }
            .toCategory()
    }

    suspend fun findLatest(
        criteria: PaginationCriteria<RecentlyLoggedFoodsPaginationCriteria>
    ): PaginationResult<FoodPreview> {
        val pagination = foodLogRepository.findLatest(criteria)

        return PaginationResult(
            data = pagination.data,
            totalCount = pagination.totalCount,
            pageCount = criteria.calcPageCount(pagination.totalCount.toDouble()),
            currentPage = criteria.calcCurrentPage()
        )
    }

    suspend fun add(userPrincipal: UserPrincipal, req: EdibleLogAddRequest): FoodLog = suspendTransaction {
        val time = timeConverter
            .toUtcResult(req.time, userPrincipal.timezone)
            .getOrElse { ex -> throw BadRequestException(ex.message ?: "invalid time") }

        val foodLogId = foodLogRepository.add(
            FoodLogAdd(
                userId = userPrincipal.id,
                foodId = req.edibleId,
                servings = req.servings,
                category = req.category.toEnum(),
                time = time,
                source = req.source.toEnum()
            )
        )

        val isNutrientInsertionSuccess = nutrientIntakeRepository.insertFromFood(
            NutrientIntakesFromFood(
                userId = userPrincipal.id,
                foodLogId = foodLogId,
                foodId = req.edibleId,
                servings = req.servings,
                source = req.source.toEnum()
            )
        )

        if (!isNutrientInsertionSuccess) {
            throw EdibleNotFoundException(
                "no nutrient data found for food (${req.edibleId})"
            )
        }

        val foodLogBuildData = foodLogRepository.findById(foodLogId, userPrincipal.id)
            ?: throw FoodLogNotFoundException(
                "food log ($foodLogId) not found after nutrient insertion when logging food"
            )

        foodLogBuildData.build()
    }

    suspend fun update(updateData: FoodLogUpdate): FoodLog = suspendTransaction {
        val baseData = foodLogRepository.getBase(updateData.userId, updateData.foodLogId)
            ?: throw IllegalStateException(
                "food log (${updateData.foodLogId}) base data not found"
            )

        val curIntakes = nutrientIntakeRepository.findByFoodLog(updateData.userId, updateData.foodLogId)

        nutrientIntakeRepository.deleteByFoodLog(updateData.userId, updateData.foodLogId)
            .takeIf { it }
            ?: throw FoodLogNotFoundException()

        foodLogRepository.updateServings(updateData.userId, updateData.foodLogId, updateData.servings)
            .takeIf { it }
            ?: throw IllegalStateException(
                "food log (${updateData.foodLogId}) when updating servings"
            )

        if (updateData.userFoodSnapshotId != null) {
            if (curIntakes.isEmpty()) throw NutrientIntakesNotFoundException()

            nutrientIntakeRepository.insertFromCurrent(
                NutrientIntakesFromCurrent(
                    userId = updateData.userId,
                    foodLogId = updateData.foodLogId,
                    curIntakes = curIntakes,
                    curServings = baseData.servings,
                    newServings = updateData.servings
                )
            )
        } else {
            val foodId = baseData.foodId
                ?: throw IllegalStateException(
                    "food id not found before attempting to insert intakes from food"
                )

            nutrientIntakeRepository.insertFromFood(
                NutrientIntakesFromFood(
                    userId = updateData.userId,
                    foodLogId = updateData.foodLogId,
                    foodId = foodId,
                    servings = updateData.servings,
                    source = baseData.source
                )
            ).takeIf { it } ?: throw IllegalStateException(
                "no nutrients found while attempting to insert intakes from food"
            )
        }

        val foodLogBuildData = foodLogRepository
            .findById(updateData.foodLogId, updateData.userId)
            ?: throw FoodLogNotFoundException(
                "food log (${updateData.foodLogId}) not found after inserting intakes"
            )

        foodLogBuildData.build()
    }
}