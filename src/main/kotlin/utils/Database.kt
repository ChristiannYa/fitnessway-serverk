package com.example.utils

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.postgresql.util.PGobject

suspend fun <T> suspendTransaction(
    block: suspend Transaction.() -> T
): T = newSuspendedTransaction(Dispatchers.IO, statement = block)

class PgEnum<T : Enum<T>>(
    private val enumClass: Class<T>,
    private val dbTypeName: String
) : ColumnType<T>() {
    override fun sqlType(): String = dbTypeName

    override fun valueFromDB(value: Any): T {
        if (enumClass.isInstance(value)) {
            @Suppress("UNCHECKED_CAST")
            return value as T
        }

        return when (value) {
            // From database as PGobject
            is PGobject -> enumClass.enumConstants.first {
                it.name.lowercase() == value.value
            }

            // From database as String
            is String -> enumClass.enumConstants.first {
                it.name.lowercase() == value
            }

            else -> {
                error("Unexpected value: $value of type ${value::class.qualifiedName}")
            }
        }
    }

    // Kotlin Enum -> Db type
    override fun notNullValueToDB(value: T): Any {
        return PGobject().apply {
            type = dbTypeName
            this.value = value.name.lowercase()
        }
    }
}

/**
 * `pgEnum` allows to use Kotlin enums in code while storing
 * them as Postgres enum types in the database
 *
 * - `T`: Kotlin enum class
 * - `columnName`: Column name from the Postgres table
 * - `pgTypeName`: Postgres enum type name
 */
inline fun <reified T : Enum<T>> Table.pgEnum(
    columnName: String,
    pgTypeName: String
): Column<T> = registerColumn(columnName, PgEnum(T::class.java, pgTypeName))