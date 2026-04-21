package com.example.service

import com.example.domain.*
import com.example.dto.UserEdibleAddRequest
import com.example.exception.EdibleAlreadyExistsException
import com.example.mappers.toCategoryGroups
import com.example.mappers.toClientFilter
import com.example.mapping.toDto
import com.example.repository.edible.user.UserEdibleRepository
import com.example.utils.suspendTransaction
import com.example.utils.toEnum

class UserEdibleService(
    private val userEdibleRepository: UserEdibleRepository
) {

    suspend fun findPagination(
        paginationCriteria: PaginationCriteria<UserEdiblesPaginationCriteria>,
        isUserPremium: Boolean
    ): PaginationResult<UserEdible> {
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

    suspend fun add(
        req: UserEdibleAddRequest,
        userPrincipal: UserPrincipal
    ): UserEdible = suspendTransaction {

        if (userEdibleRepository.isBaseDuplicate(userPrincipal.id, req.base, req.nutrients)) {
            throw EdibleAlreadyExistsException(req.edibleType.toEnum())
        }

        val (ueDao, nutrientList) = userEdibleRepository.create(
            UserEdibleCreate(
                userId = userPrincipal.id,
                foodBase = req.base,
                nutrientList = req.nutrients,
                edibleType = req.edibleType.toEnum()
            )
        )

        ueDao.toDto(
            nutrientList
                .toClientFilter(isUserPremium = userPrincipal.isPremium)
                .toCategoryGroups()
        )
    }
}