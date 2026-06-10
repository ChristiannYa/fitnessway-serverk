package com.example.mapping

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID

/**
 * Represents the scope of edible Dao objects
 */
abstract class EdibleDao(id: EntityID<Int>) : IntEntity(id)
