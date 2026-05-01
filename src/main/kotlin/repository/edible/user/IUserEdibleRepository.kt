package com.example.repository.edible.user

import com.example.domain.*
import com.example.mapping.UEDao
import java.util.*

interface IUserEdibleRepository {
    suspend fun findById(id: Int, userId: UUID, edibleType: EdibleType): Pair<UEDao, List<NutrientDataAmount>>?

    suspend fun findPagination(
        paginationCriteria: PaginationCriteria<UserEdiblesPaginationCriteria>
    ): Result<PaginationQuery<Pair<UEDao, List<NutrientDataAmount>>>>

    suspend fun isBaseDuplicate(
        userId: UUID,
        edibleBase: EdibleBase,
        nutrientList: List<NutrientIdWithAmount>
    ): Boolean

    suspend fun create(createData: UserEdibleCreate): Pair<UEDao, List<NutrientDataAmount>>
}