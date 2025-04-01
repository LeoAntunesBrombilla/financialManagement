package com.lbDeveloper.plugins.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init(environment: ApplicationEnvironment ) {
        val database = Database.connect(hikari(environment))


        // Initialize your database schema here if needed
        transaction(database) {
            // SchemaUtils.create(tables...)
        }
    }

    private fun hikari(environment: ApplicationEnvironment): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName = environment.config.property("database.driverClassName").getString()
        config.jdbcUrl = environment.config.property("database.jdbcUrl").getString()
        config.username = environment.config.property("database.username").getString()
        config.password = environment.config.property("database.password").getString()
        config.maximumPoolSize = environment.config.property("database.maximumPoolSize").getString().toInt()
        config.isAutoCommit = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        config.validate()
        return HikariDataSource(config)
    }

    // Helper function for database transactions
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}