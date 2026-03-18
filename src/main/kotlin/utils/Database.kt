package com.example.utils

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.experimental.withSuspendTransaction
import org.postgresql.util.PGobject

suspend fun <T> suspendTransaction(
    block: suspend Transaction.() -> T
): T = TransactionManager
    .currentOrNull()?.withSuspendTransaction(statement = block)
    ?: newSuspendedTransaction(Dispatchers.IO, statement = block)

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
 * Allows to use Kotlin `Enum`s in code while storing
 * them as **Postgres Enum** types in the database
 *
 * - `T`: Kotlin enum class
 * - `columnName`: Column name from the Postgres table
 * - `pgTypeName`: Postgres enum type name
 */
inline fun <reified T : Enum<T>> Table.pgEnum(
    columnName: String,
    pgTypeName: String
): Column<T> = registerColumn(columnName, PgEnum(T::class.java, pgTypeName))

/**
 * Creates a custom SQL expression for the "similarity" function from the
 * `pg_trgm` extension.
 * @return Score between 0.0 and 1.0 indicating how similar the column value is to
 * the search query.
 *
 * @param col The column or expression to compare against
 * @param value The search query to compare with
 */
fun similarity(col: Expression<String>, value: String) = CustomFunction<Double>(
    functionName = "similarity",
    columnType = DoubleColumnType(),
    col.lowerCase(),
    stringLiteral(value)
)