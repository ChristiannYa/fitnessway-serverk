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
import kotlin.time.toJavaInstant

class FoodLogRepository : IFoodLogRepository {
    override suspend fun findById(id: Int): FoodLog? = suspendTransaction {
        val foodLogDao = UFLDao.findById(id) ?: return@suspendTransaction null

        val foodLogUserId = foodLogDao.userId
        val foodLogFoodId = foodLogDao.foodId
        val foodLogFoodSnapshotId = foodLogDao.foodSnapshotId?.value

        val (
            foodSnapshotStatus: UserFoodSnapshotStatus?,
            foodBase: FoodBase
        ) = when (foodLogDao.foodSource) {
            FoodSource.APP -> {
                if (foodLogFoodId == null) return@suspendTransaction null

                val afDao = AFDao.findById(foodLogFoodId) ?: return@suspendTransaction null
                null to afDao.toBase()
            }

            FoodSource.USER -> when {
                foodLogFoodSnapshotId == null && foodLogFoodId != null -> {
                    val ufDao = UFDao.findById(foodLogFoodId) ?: return@suspendTransaction null
                    UserFoodSnapshotStatus.PRESENT to ufDao.toBase()
                }

                foodLogFoodSnapshotId != null && foodLogFoodId != null -> {
                    val ufsDao = UFSDao.findById(foodLogFoodSnapshotId) ?: return@suspendTransaction null
                    UserFoodSnapshotStatus.UPDATED to ufsDao.toBase()
                }

                foodLogFoodSnapshotId != null && foodLogFoodId == null -> {
                    val ufsDao = UFSDao.findById(foodLogFoodSnapshotId) ?: return@suspendTransaction null
                    UserFoodSnapshotStatus.DELETED to ufsDao.toBase()
                }

                else -> return@suspendTransaction null
            }
        }

        val nutrients = (UNI innerJoin N)
            .join(
                joinType = JoinType.LEFT,
                otherTable = UNP,
                onColumn = N.id,
                otherColumn = UNP.nutrientId,
                additionalConstraint = { UNP.userId eq foodLogUserId }
            )
            .selectAll()
            .where { (UNI.foodLogId eq id) and (UNI.userId eq foodLogUserId.value) }
            .map { row ->
                NutrientInFood(
                    nutrientData = NutrientData(
                        base = N.toBase(row),
                        preferences = UNP.toNutrientPreferences(row)
                    ),
                    amount = row[UNI.intakeAmount].toDouble()
                )
            }

        foodLogDao.toDto(
            userFoodSnapshotStatus = foodSnapshotStatus,
            foodId = foodLogFoodId,
            foodInformationDto = FoodInformationDto(
                base = foodBase,
                nutrients = nutrients.toType()
            )
        )
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
}