package com.example.config

import com.example.domain.UserType

object RewardConfig {
    const val FOOD_APPROVAL_REWARD = 4.00
    const val FOOD_LOGGED_REWARD = 0.01
    const val APP_EDIBLE_REPORT_REWARD = 1.0

    const val CONTRIBUTOR_MULTIPLIER = 1.25

    fun Double.applyMultiplier(userType: UserType): Double =
        when (userType) {
            UserType.CONTRIBUTOR -> this * CONTRIBUTOR_MULTIPLIER
            else -> this
        }
}