package config

import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.testcontainers.containers.PostgreSQLContainer

object TestDatabase {
    private var pgContainer: PostgreSQLContainer<*>? = null

    fun setUp(): Database {
        // Create and start container if it doesn't exist
        if (pgContainer == null) {
            pgContainer = PostgreSQLContainer("postgres:15-alpine").apply { start() }
        }

        val db = Database.connect(
            url = pgContainer!!.jdbcUrl,
            driver = "org.postgresql.Driver",
            user = pgContainer!!.username,
            password = pgContainer!!.password
        )

        // Run Flyway migrations
        pgContainer!!.configureFlyWay().load().migrate()

        return db
    }

    // Cleans up database after each test
    fun tearDown() {
        pgContainer?.configureFlyWay()?.load()?.clean()
    }

    fun shutDown() {
        pgContainer?.stop()
        pgContainer = null
    }

    private fun PostgreSQLContainer<*>.configureFlyWay() = Flyway
        .configure()
        .dataSource(this.jdbcUrl, this.username, this.password)
        .locations("classpath:db/migration")
        .cleanDisabled(false)
}