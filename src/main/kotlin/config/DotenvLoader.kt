package com.example.config

import io.github.cdimascio.dotenv.dotenv

fun loadDotenv() {
    val dotenv = dotenv {
        ignoreIfMissing = false
    }

    dotenv.entries().forEach { entry ->
        System.setProperty(entry.key, entry.value)
    }
}