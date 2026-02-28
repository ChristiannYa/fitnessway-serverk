package com.example.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

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
        maximumPoolSize = 10
        minimumIdle = 2
        idleTimeout = 600_000      // 10 minutes
        connectionTimeout = 30_000 // 30 seconds
        maxLifetime = 1_800_000    // 30 minutes
    }

    val dataSource = HikariDataSource(hikariConfig)

    // Run Flyway migrations before connecting Exposed
    val flyway = Flyway
        .configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration")
        .baselineOnMigrate(true)

    flyway.load().migrate()

    Database.connect(dataSource)
}