package com.example.repository.foods.log

import com.example.domain.*
import com.example.dto.FoodInformationDto
import com.example.mappers.toType
import com.example.mapping.*
import com.example.utils.suspendTransaction
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant
import java.util.*
import kotlin.time.toJavaInstant

class FoodLogRepository : IFoodLogRepository {
    override suspend fun findById(id: Int): FoodLog? = suspendTransaction {
        val foodLogDao = UFLDao.findById(id) ?: return@suspendTransaction null
        foodLogDao.toFoodLogDto()
    }

    override suspend fun findByDate(userId: UUID, range: InstantRange): FoodLogResult = suspendTransaction {
        val foodLogs = mutableListOf<FoodLog>()

        UFLDao.find {
            (UFL.userId eq userId) and
            (UFL.time greaterEq range.start.toJavaInstant()) and
            (UFL.time less range.end.toJavaInstant())
        }.forEach { flDao ->
            val foodLog = flDao.toFoodLogDto()
                ?: return@suspendTransaction FoodLogResult.Error(flDao.id.value)

            foodLogs.add(foodLog)
        }

        FoodLogResult.Success(foodLogs)
    }

    override suspend fun add(data: FoodLogAdd): Int = suspendTransaction {
        UFLDao.new {
            userId = EntityID(data.userId, U)
            foodId = data.foodId
            foodSnapshotId = null
            servings = data.servings.toBigDecimal()
            category = data.category
            time = data.time.toJavaInstant()
            loggedAt = Instant.now()
            foodSource = data.source
        }.id.value
    }

    private fun UFLDao.toFoodLogDto(): FoodLog? {
        val foodLogFoodId = this.foodId
        val foodLogFoodSnapshotId = this.foodSnapshotId?.value

        val (
            foodSnapshotStatus: UserFoodSnapshotStatus?,
            foodBase: FoodBase
        ) = when (this.foodSource) {
            FoodSource.APP -> {
                if (foodLogFoodId == null) return null

                val afDao = AFDao.findById(foodLogFoodId) ?: return null
                null to afDao.toBase()
            }

            FoodSource.USER -> when {
                foodLogFoodSnapshotId == null && foodLogFoodId != null -> {
                    val ufDao = UFDao.findById(foodLogFoodId) ?: return null
                    UserFoodSnapshotStatus.PRESENT to ufDao.toBase()
                }

                foodLogFoodSnapshotId != null && foodLogFoodId != null -> {
                    val ufsDao = UFSDao.findById(foodLogFoodSnapshotId) ?: return null
                    UserFoodSnapshotStatus.UPDATED to ufsDao.toBase()
                }

                foodLogFoodSnapshotId != null && foodLogFoodId == null -> {
                    val ufsDao = UFSDao.findById(foodLogFoodSnapshotId) ?: return null
                    UserFoodSnapshotStatus.DELETED to ufsDao.toBase()
                }

                else -> return null
            }
        }

        val nutrients = (UNI innerJoin N)
            .join(
                joinType = JoinType.LEFT,
                otherTable = UNP,
                onColumn = N.id,
                otherColumn = UNP.nutrientId,
                additionalConstraint = { UNP.userId eq this@toFoodLogDto.userId }
            )
            .selectAll()
            .where {
                (UNI.foodLogId eq this@toFoodLogDto.id.value) and
                (UNI.userId eq this@toFoodLogDto.userId.value)
            }
            .map { row ->
                NutrientInFood(
                    nutrientData = NutrientData(
                        base = N.toBase(row),
                        preferences = UNP.toNutrientPreferences(row)
                    ),
                    amount = row[UNI.intakeAmount].toDouble()
                )
            }

        return this.toDto(
            userFoodSnapshotStatus = foodSnapshotStatus,
            foodId = foodLogFoodId,
            foodInformationDto = FoodInformationDto(
                base = foodBase,
                nutrients = nutrients.toType()
            )
        )
    }
}