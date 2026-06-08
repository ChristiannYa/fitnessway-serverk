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
        idleTimeout = 180_000      // 3 minutes
        maxLifetime = 600_000      // 10 minutes
        keepaliveTime = 60_000     // 1 minute
        connectionTimeout = 30_000 // 30 seconds
    }

    val dataSource = HikariDataSource(hikariConfig)

    // Run Flyway migrations before connecting Exposed
    val flyway = Flyway
        .configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration")

    flyway
        .load()
        .migrate()

    Database.connect(dataSource)
}