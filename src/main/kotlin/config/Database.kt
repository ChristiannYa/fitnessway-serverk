package com.example.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

private fun minToMs(min: Double): Long = (min * 60_000).toLong()

fun Application.configureDatabase() {
    val config = environment.config

    val dbDriver = config.property("storage.driverClassName").getString()
    val dbUrl = config.property("storage.jdbcURL").getString()
    val dbUser = config.property("storage.user").getString()
    val dbPassword = config.property("storage.password").getString()

    val hikariConfig = HikariConfig().apply {
        jdbcUrl = dbUrl
        username = dbUser
        password = dbPassword
        driverClassName = dbDriver
        initializationFailTimeout = -1
        maximumPoolSize = 5
        minimumIdle = 0
        keepaliveTime = 0
        idleTimeout = minToMs(1.0)
        maxLifetime = minToMs(5.0)
        connectionTimeout = minToMs(0.5)
    }

    val dataSource = HikariDataSource(hikariConfig)

    // Run Flyway migrations before connecting Exposed
    Flyway
        .configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration")
        .load()
        .migrate()

    Database.connect(dataSource)
}
