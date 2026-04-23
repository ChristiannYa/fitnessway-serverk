package com.example.repository.edible.log

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
            (UEL.userId eq userId) and (UEL.id eq id)
        }.firstOrNull()?.toFoodLogDto(isUserPremium)
    }

    override suspend fun findByDate(
        userId: UUID,
        isUserPremium: Boolean,
        range: InstantRange
    ): Result<List<FoodLog>> = suspendTransaction {
        val foodLogs = mutableListOf<FoodLog>()

        UFLDao.find {
            (UEL.userId eq userId) and
            (UEL.time greaterEq range.start.toJavaInstant().atOffset(ZoneOffset.UTC)) and
            (UEL.time less range.end.toJavaInstant().atOffset(ZoneOffset.UTC))
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

        var count: Long

        val foodIdsByType: Map<LogSource, List<Int>> = UEL
            .select(UEL.edibleId, UEL.logSource, UEL.time.max())
            .where { UEL.userId eq criteria.data.userId }
            .groupBy(UEL.edibleId, UEL.logSource)
            .orderBy(UEL.time.max(), SortOrder.DESC)
            .limit(criteria.limit)
            .offset(criteria.offset)
            .also { count = it.count() }
            .groupBy({ r -> r[UEL.logSource] }, { r -> r[UEL.edibleId] })
            .mapValues { it.value.filterNotNull() }

        val appFoodIds: List<Int> = foodIdsByType[LogSource.APP] ?: emptyList()
        val userEdibleIds: List<Int> = foodIdsByType[LogSource.USER] ?: emptyList()

        val appNutrientPreviews: Map<Int, NutrientPreview> =
            queryNutrientPreviews(AFN, appFoodIds, criteria.data.userId)

        val userNutrientPreviews: Map<Int, NutrientPreview> =
            queryNutrientPreviews(UEN, userEdibleIds, criteria.data.userId)

        val appFoodDaos: Map<Int, AFDao> = AFDao
            .forIds(appFoodIds)
            .associateBy { it.id.value }
            // @TODO: Update to its actual `edibleType` value
            .filter { criteria.data.edibleType == EdibleType.FOOD }

        val userEdibleDaos: Map<Int, UEDao> = UEDao
            .forIds(userEdibleIds)
            .associateBy { it.id.value }
            .filter { it.value.edibleType == criteria.data.edibleType }

        val data: List<FoodPreview> = foodIdsByType.flatMap { (source, ids) ->
            ids.mapNotNull { foodId ->
                when (source) {
                    LogSource.APP -> appFoodDaos[foodId]?.let { afDao ->
                        FoodPreview(
                            id = afDao.id.value,
                            base = afDao.toBase(),
                            nutrientPreview = appNutrientPreviews[foodId] ?: NutrientPreview(),
                            source = LogSource.APP
                        )
                    }

                    LogSource.USER -> userEdibleDaos[foodId]?.let { ueDao ->
                        FoodPreview(
                            id = ueDao.id.value,
                            base = ueDao.toBase(),
                            nutrientPreview = userNutrientPreviews[foodId] ?: NutrientPreview(),
                            source = LogSource.USER
                        )
                    }
                }
            }
        }

        PaginationQuery(data, count)
    }

    override suspend fun getBaseData(userId: UUID, foodLogId: Int): FoodLogBase? = suspendTransaction {
        UFLDao.find {
            (UEL.userId eq userId) and (UEL.id eq foodLogId)
        }.firstOrNull()?.let { uflDao ->
            FoodLogBase(
                foodId = uflDao.edibleId,
                userFoodSnapshotId = uflDao.edibleSnapshotId?.value,
                servings = uflDao.servings.toDouble(),
                source = uflDao.logSource
            )
        }
    }

    override suspend fun add(data: FoodLogAdd): Int = suspendTransaction {
        UFLDao.new {
            userId = EntityID(data.userId, U)
            edibleId = data.foodId
            edibleSnapshotId = null
            servings = data.servings.toBigDecimal()
            category = data.category
            time = data.time.toJavaInstant().atOffset(ZoneOffset.UTC)
            loggedAt = Instant.now().atOffset(ZoneOffset.UTC)
            logSource = data.source
        }.id.value
    }

    override suspend fun updateServings(
        userId: UUID,
        foodLogId: Int,
        servings: Double
    ): Boolean = suspendTransaction {
        val updateCount = UEL.update(
            where = { (UEL.userId eq userId) and (UEL.id eq foodLogId) }
        ) { it[UEL.servings] = servings.toBigDecimal() }

        updateCount > 0
    }

    private fun UFLDao.toFoodLogDto(isUserPremium: Boolean): FoodLog? {
        val foodLogFoodId = this.edibleId
        val foodLogFoodSnapshotId = this.edibleSnapshotId?.value

        val (
            foodSnapshotStatus: UserEdibleSnapshotStatus?,
            edibleBase: EdibleBase
        ) = when (this.logSource) {
            LogSource.APP -> {
                if (foodLogFoodId == null) return null

                val afDao = AFDao.findById(foodLogFoodId) ?: return null
                null to afDao.toBase()
            }

            LogSource.USER -> when {
                foodLogFoodSnapshotId == null && foodLogFoodId != null -> {
                    val ufDao = UEDao.findById(foodLogFoodId) ?: return null
                    UserEdibleSnapshotStatus.PRESENT to ufDao.toBase()
                }

                foodLogFoodSnapshotId != null && foodLogFoodId != null -> {
                    val ufsDao = UFSDao.findById(foodLogFoodSnapshotId) ?: return null
                    UserEdibleSnapshotStatus.UPDATED to ufsDao.toBase()
                }

                foodLogFoodSnapshotId != null && foodLogFoodId == null -> {
                    val ufsDao = UFSDao.findById(foodLogFoodSnapshotId) ?: return null
                    UserEdibleSnapshotStatus.DELETED to ufsDao.toBase()
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
                (UNI.edibleLogId eq this@toFoodLogDto.id.value) and
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
            userEdibleSnapshotStatus = foodSnapshotStatus,
            foodId = foodLogFoodId,
            foodInformationDto = FoodInformationDto(
                base = edibleBase,
                nutrients = nutrients.toCategoryGroups()
            )
        )
    }
}