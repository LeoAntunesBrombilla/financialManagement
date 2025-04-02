package com.lbDeveloper.plugins.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement

object DatabaseFactory {
    fun init(environment: ApplicationEnvironment ) {
        ensureDatabaseExists(environment)

        val database = Database.connect(hikari(environment))

        transaction(database) {
            DatabaseMigration.up()
        }

        environment.log.info("Database initialized successfully")
    }

    private fun checkDatabaseExists(connection: Connection, dbName: String): Boolean {
        connection.createStatement().use { statement: Statement ->  statement.executeQuery("SELECT 1 FROM pg_database WHERE datname = '$dbName'").use { resultSet -> return resultSet.next() } }
    }

    private fun createDatabase(connection: Connection, dbName: String) {
        connection.createStatement().use { statement: Statement -> statement.execute("CREATE DATABASE $dbName")
        }
    }

    private fun ensureDatabaseExists(environment: ApplicationEnvironment) {
        val dbUrl = environment.config.property("database.jdbcUrl").getString()
        val username = environment.config.property("database.username").getString()
        val password = environment.config.property("database.password").getString()
        val dbName = extractDatabaseName(dbUrl)

        val serverUrl = dbUrl.replace("/$dbName", "/postgres")

        try {
            DriverManager.getConnection(serverUrl, username,password).use {
                connection -> if(!checkDatabaseExists(connection, dbName)) {
                    createDatabase(connection, dbName)
                }
            }
        } catch (e: Exception) {
            environment.log.error("Failed to check/create DB: ${e.message}")
            throw e
        }
    }

    private fun extractDatabaseName(jdbcUrl: String): String {
        return jdbcUrl.substringAfterLast("/")
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

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}