package com.example.service

import com.example.domain.PaginationCriteria
import com.example.domain.PaginationResult
import com.example.domain.UserEdiblesPaginationCriteria
import com.example.domain.UserFood
import com.example.mappers.toCategoryGroups
import com.example.mappers.toClientFilter
import com.example.mapping.toDto
import com.example.repository.edible.user.UserEdibleRepository

class UserEdibleService(
    private val userEdibleRepository: UserEdibleRepository
) {

    suspend fun findPagination(
        paginationCriteria: PaginationCriteria<UserEdiblesPaginationCriteria>,
        isUserPremium: Boolean
    ): PaginationResult<UserFood> {
        val pagination = userEdibleRepository
            .findPagination(paginationCriteria)
            .getOrElse { throw it }

        return PaginationResult(
            data = pagination.data.map { (ueDao, nutrientList) ->
                ueDao.toDto(
                    nutrientList
                        .toClientFilter(isUserPremium = isUserPremium)
                        .toCategoryGroups()
                )
            },
            totalCount = pagination.totalCount,
            pageCount = paginationCriteria.calcPageCount(pagination.totalCount.toDouble()),
            currentPage = paginationCriteria.calcCurrentPage()

        )
    }

}