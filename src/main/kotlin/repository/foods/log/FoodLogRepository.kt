package com.example.repository.foods.log

import com.example.domain.*
import com.example.dto.FoodInformationDto
import com.example.mappers.toCategoryGroups
import com.example.mappers.toClientFilter
import com.example.mapping.*
import com.example.repository.foods.queryNutrientPreviews
import com.example.utils.suspendTransaction
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import java.time.Instant
import java.time.ZoneOffset
import java.util.*
import kotlin.time.toJavaInstant


class FoodLogRepository : IFoodLogRepository {
    override suspend fun findById(userId: UUID, id: Int, isUserPremium: Boolean): FoodLog? = suspendTransaction {
        UFLDao.find {
            (UFL.userId eq userId) and (UFL.id eq id)
        }.firstOrNull()?.toFoodLogDto(isUserPremium)
    }

    override suspend fun findByDate(
        userId: UUID,
        isUserPremium: Boolean,
        range: InstantRange
    ): Result<List<FoodLog>> = suspendTransaction {
        val foodLogs = mutableListOf<FoodLog>()

        UFLDao.find {
            (UFL.userId eq userId) and
            (UFL.time greaterEq range.start.toJavaInstant().atOffset(ZoneOffset.UTC)) and
            (UFL.time less range.end.toJavaInstant().atOffset(ZoneOffset.UTC))
        }.forEach { flDao ->
            val foodLog = flDao.toFoodLogDto(isUserPremium)
                ?: return@suspendTransaction Result.failure(
                    IllegalStateException(
                        "food log dao #${flDao.id.value} not found"
                    )
                )

            foodLogs.add(foodLog)
        }

        Result.success(foodLogs)
    }

    override suspend fun findLatest(
        criteria: PaginationCriteria<RecentlyLoggedFoodsPaginationCriteria>
    ): PaginationQuery<FoodPreview> = suspendTransaction {
        val recentFoodIds = UFL
            .select(UFL.foodId, UFL.foodSource, UFL.time.max())
            .where { UFL.userId eq criteria.data.userId }
            .groupBy(UFL.foodId, UFL.foodSource)
            .orderBy(UFL.time.max(), SortOrder.DESC)
            .limit(criteria.limit)
            .offset(criteria.offset)
            .mapNotNull { row -> row[UFL.foodId] to row[UFL.foodSource] }

        val recentAppFoodIds = recentFoodIds
            .filter { it.second == FoodSource.APP }
            .mapNotNull { it.first }

        val recentUserFoodIds = recentFoodIds
            .filter { it.second == FoodSource.USER }
            .mapNotNull { it.first }

        val appNutrientPreviews = queryNutrientPreviews(AFN, recentAppFoodIds, criteria.data.userId)
        val userNutrientPreviews = queryNutrientPreviews(UFN, recentUserFoodIds, criteria.data.userId)

        val appFoods = AFDao
            .forIds(recentAppFoodIds)
            .associateBy { it.id.value }

        val userFoods = UFDao
            .forIds(recentUserFoodIds)
            .associateBy { it.id.value }

        val data: List<FoodPreview> = recentFoodIds.mapNotNull { (foodId, source) ->
            when (source) {
                FoodSource.APP -> {
                    val afDao = appFoods[foodId] ?: return@mapNotNull null
                    FoodPreview(
                        id = afDao.id.value,
                        base = afDao.toBase(),
                        nutrientPreview = appNutrientPreviews[foodId] ?: NutrientPreview(),
                        source = FoodSource.APP
                    )
                }

                FoodSource.USER -> {
                    val ufDao = userFoods[foodId] ?: return@mapNotNull null
                    FoodPreview(
                        id = ufDao.id.value,
                        base = ufDao.toBase(),
                        nutrientPreview = userNutrientPreviews[foodId] ?: NutrientPreview(),
                        source = FoodSource.USER
                    )
                }
            }
        }

        PaginationQuery(data, recentFoodIds.size.toLong())
    }

    override suspend fun getBaseData(userId: UUID, foodLogId: Int): FoodLogBase? = suspendTransaction {
        UFLDao.find {
            (UFL.userId eq userId) and (UFL.id eq foodLogId)
        }.firstOrNull()?.let { uflDao ->
            FoodLogBase(
                foodId = uflDao.foodId,
                userFoodSnapshotId = uflDao.foodSnapshotId?.value,
                servings = uflDao.servings.toDouble(),
                source = uflDao.foodSource
            )
        }
    }

    override suspend fun add(data: FoodLogAdd): Int = suspendTransaction {
        UFLDao.new {
            userId = EntityID(data.userId, U)
            foodId = data.foodId
            foodSnapshotId = null
            servings = data.servings.toBigDecimal()
            category = data.category
            time = data.time.toJavaInstant().atOffset(ZoneOffset.UTC)
            loggedAt = Instant.now().atOffset(ZoneOffset.UTC)
            foodSource = data.source
        }.id.value
    }

    override suspend fun updateServings(
        userId: UUID,
        foodLogId: Int,
        servings: Double
    ): Boolean = suspendTransaction {
        val updateCount = UFL.update(
            where = { (UFL.userId eq userId) and (UFL.id eq foodLogId) }
        ) { it[UFL.servings] = servings.toBigDecimal() }

        updateCount > 0
    }

    private fun UFLDao.toFoodLogDto(isUserPremium: Boolean): FoodLog? {
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
                NutrientDataAmount(
                    nutrientData = NutrientData(
                        base = N.toBase(row),
                        preferences = UNP.toNutrientPreferences(row)
                    ),
                    amount = row[UNI.intakeAmount].toDouble()
                )
            }
            .toClientFilter(isUserPremium = isUserPremium)

        return this.toDto(
            userFoodSnapshotStatus = foodSnapshotStatus,
            foodId = foodLogFoodId,
            foodInformationDto = FoodInformationDto(
                base = foodBase,
                nutrients = nutrients.toCategoryGroups()
            )
        )
    }
}