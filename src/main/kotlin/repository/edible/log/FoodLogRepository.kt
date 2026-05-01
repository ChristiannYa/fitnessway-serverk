package com.example.repository.edible.log

import com.example.domain.*
import com.example.mapping.*
import com.example.repository.edible.queryNutrientPreviews
import com.example.utils.suspendTransaction
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import java.time.Instant
import java.time.ZoneOffset
import java.util.*
import kotlin.time.toJavaInstant


class FoodLogRepository : IFoodLogRepository {
    override suspend fun findById(id: Int, userId: UUID): EdibleLogBuildData? = suspendTransaction {
        val uelDao = UELDao.find {
            (UEL.userId eq userId) and
            (UEL.id eq id)
        }
            .firstOrNull()
            ?: return@suspendTransaction null

        val (edibleBase, snapshotStatus) = uelDao
            .getEdibleBaseToSnapshotPairResult(userId)
            .getOrElse { return@suspendTransaction null }

        EdibleLogBuildData(
            dao = uelDao,
            edibleBase = edibleBase,
            nutrientList = uelDao.getNutrients(),
            snapshotStatus = snapshotStatus
        )
    }

    override suspend fun findByDate(
        userId: UUID,
        isUserPremium: Boolean,
        range: InstantRange
    ): Result<List<EdibleLogBuildData>> = suspendTransaction {
        val edibleLogBuildDataList = mutableListOf<EdibleLogBuildData>()

        UELDao.find {
            (UEL.userId eq userId) and
            (UEL.time greaterEq range.start.toJavaInstant().atOffset(ZoneOffset.UTC)) and
            (UEL.time less range.end.toJavaInstant().atOffset(ZoneOffset.UTC))
        }.forEach { uelDao ->
            val (edibleBase, snapshotStatus) = uelDao
                .getEdibleBaseToSnapshotPairResult(userId)
                .getOrElse {
                    return@suspendTransaction Result.failure(
                        IllegalStateException(it.message)
                    )
                }

            edibleLogBuildDataList.add(
                EdibleLogBuildData(
                    dao = uelDao,
                    edibleBase = edibleBase,
                    nutrientList = uelDao.getNutrients(),
                    snapshotStatus = snapshotStatus,
                )
            )
        }

        Result.success(edibleLogBuildDataList)
    }

    override suspend fun findLatest(
        criteria: PaginationCriteria<RecentlyLoggedFoodsPaginationCriteria>
    ): PaginationQuery<FoodPreview> = suspendTransaction {

        val foodIdsByType: Map<LogSource, List<Int>> = UEL
            .select(UEL.edibleId, UEL.logSource, UEL.time.max())
            .where { UEL.userId eq criteria.data.userId }
            .groupBy(UEL.edibleId, UEL.logSource)
            .orderBy(UEL.time.max(), SortOrder.DESC)
            .limit(criteria.limit)
            .offset(criteria.offset)
            .groupBy({ r -> r[UEL.logSource] }, { r -> r[UEL.edibleId] })
            .mapValues { it.value.filterNotNull() }

        val appFoodIds: List<Int> = foodIdsByType[LogSource.APP] ?: emptyList()
        val userEdibleIds: List<Int> = foodIdsByType[LogSource.USER] ?: emptyList()

        val appNutrientPreviews: Map<Int, NutrientPreview> =
            queryNutrientPreviews(AEN, appFoodIds, criteria.data.userId)

        val userNutrientPreviews: Map<Int, NutrientPreview> =
            queryNutrientPreviews(UEN, userEdibleIds, criteria.data.userId)

        val appFoodDaos: Map<Int, AEDao> = AEDao
            .forIds(appFoodIds)
            .associateBy { it.id.value }
            .filter { it.value.edibleType == criteria.data.edibleType }

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

        PaginationQuery(data, data.size.toLong())
    }

    override suspend fun getBase(userId: UUID, foodLogId: Int): FoodLogBase? = suspendTransaction {
        UELDao
            .find {
                (UEL.userId eq userId) and
                (UEL.id eq foodLogId)
            }
            .firstOrNull()
            ?.let { uflDao ->
                FoodLogBase(
                    foodId = uflDao.edibleId,
                    userFoodSnapshotId = uflDao.edibleSnapshotId?.value,
                    servings = uflDao.servings.toDouble(),
                    source = uflDao.logSource
                )
            }
    }

    override suspend fun add(data: FoodLogAdd): Int = suspendTransaction {
        UELDao.new {
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

    private fun UELDao.getEdibleBaseToSnapshotPairResult(
        userId: UUID
    ): Result<Pair<EdibleBase, UserEdibleSnapshotStatus?>> {
        val edibleId = this.edibleId
        val snapshotId = this.edibleSnapshotId?.value

        val (
            snapshotStatus: UserEdibleSnapshotStatus?,
            edibleBase: EdibleBase
        ) = when (this.logSource) {
            LogSource.APP -> {
                if (edibleId == null) return Result.failure(
                    IllegalStateException(
                        "missing app edible #$edibleId"
                    )
                )

                val aeDao = AEDao.findById(edibleId) ?: return Result.failure(
                    IllegalStateException(
                        "app edible dao #$edibleId not found"
                    )
                )
                null to aeDao.toBase()
            }

            LogSource.USER -> when {
                snapshotId == null && edibleId != null -> {
                    val ueDao = UEDao.find {
                        (UE.userId eq userId) and
                        (UE.id eq edibleId)
                    }.firstOrNull() ?: return Result.failure(
                        IllegalStateException(
                            "user edible dao #$edibleId not found"
                        )
                    )
                    UserEdibleSnapshotStatus.PRESENT to ueDao.toBase()
                }

                snapshotId != null && edibleId != null -> {
                    val uesDao = UESDao.find {
                        (UES.userId eq userId) and
                        (UES.id eq snapshotId)
                    }.firstOrNull() ?: return Result.failure(
                        IllegalStateException(
                            "user edible snapshot dao #$snapshotId not found"
                        )
                    )
                    UserEdibleSnapshotStatus.UPDATED to uesDao.toBase()
                }

                snapshotId != null && edibleId == null -> {
                    val uesDao = UESDao.find {
                        (UES.userId eq userId) and
                        (UES.id eq snapshotId)
                    }.firstOrNull() ?: return Result.failure(
                        IllegalStateException(
                            "user edible snapshot dao #$snapshotId not found"
                        )
                    )
                    UserEdibleSnapshotStatus.DELETED to uesDao.toBase()
                }

                else -> return Result.failure(
                    IllegalStateException(
                        "unknown snapshot status"
                    )
                )
            }
        }

        return Result.success(edibleBase to snapshotStatus)
    }

    private fun UELDao.getNutrients(): List<NutrientDataAmount> = (UNI innerJoin N)
        .join(
            joinType = JoinType.LEFT,
            otherTable = UNP,
            onColumn = N.id,
            otherColumn = UNP.nutrientId,
            additionalConstraint = { UNP.userId eq userId }
        )
        .selectAll()
        .where {
            (UNI.edibleLogId eq this@getNutrients.id.value) and
            (UNI.userId eq userId)
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
}