package com.example.repository.edible.app

import com.example.domain.*
import com.example.mapping.AEDao
import com.example.mapping.AERDao
import com.example.mapping.AERUDao
import com.example.repository.edible.*
import java.util.*

interface IAppFoodRepository {
    suspend fun findById(id: Int, userId: UUID): Pair<AppEdibleRepoResult, String>?
    suspend fun findByBarcode(barcode: String, userId: UUID): Pair<AppEdibleRepoResult, String>?
    suspend fun findAdminSubmissions(paginationCriteria: PaginationCriteria<AppEdiblePaginationCriteria>): Result<PaginationQuery<Pair<AppEdibleRepoResult, String>>>
    suspend fun findReportById(id: Int): Pair<AERDao, List<AppEdibleReport.Reason>>?
    suspend fun findReportUpdateById(id: Int, userId: UUID): EdibleRepoResult<AERUDao, NutrientDataAmount>?
    suspend fun getReports(paginationCriteria: PaginationCriteria<AppEdibleReportListPaginationCriteria>): Result<PaginationQuery<AppEdibleReportQuery>>
    suspend fun submit(foodToCreate: AppFoodCreate): Pair<AEDao, List<NutrientDataAmount>>
    suspend fun submitEdibleInReport(reportId: Int, writeData: AppEdibleRepoWrite)
    suspend fun setBarcode(barcode: String, edibleId: Int): DatabaseResult
    suspend fun updateBase(edibleId: Int, base: EdibleBase)
    suspend fun updateType(edibleId: Int, type: EdibleType)
    suspend fun updateNutrients(edibleId: Int, nutrients: List<NutrientIdWithAmount>)
    suspend fun updateBarcode(edibleId: Int, old: String, new: String)
    suspend fun isDuplicate(base: EdibleBase, nutrientList: List<NutrientIdWithAmount>): Boolean
    suspend fun search(criteria: PaginationCriteria<AppFoodSearchPaginationCriteria>): PaginationQuery<FoodPreview>
    suspend fun report(report: AppEdibleReportWrite): AERDao
    suspend fun reviewReport(reportReview: AppEdibleReportReview): AERDao
}