package com.example.config

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabase() {
    val config = environment.config

    val driver = config.property("storage.driverClassName").getString()
    val url = config.property("storage.jdbcURL").getString()
    val user = config.property("storage.user").getString()
    val password = config.property("storage.password").getString()

    /*
    // Run Flyway migrations before connecting Exposed
    val flyway = Flyway
        .configure()
        .dataSource(url, user, password)
        .locations("classpath:db/migration")
        .load()

    flyway.migrate()

     */

    Database.connect(
        url = url,
        driver = driver,
        user = user,
        password = password
    )
}