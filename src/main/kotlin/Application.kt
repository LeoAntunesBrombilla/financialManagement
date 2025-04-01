package com.lbDeveloper

import com.lbDeveloper.plugins.database.DatabaseFactory
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    DatabaseFactory.init(environment)

    routing {
        get("/test-db") {
            val result = DatabaseFactory.dbQuery {
                // Simple query to test connection
                org.jetbrains.exposed.sql.transactions.transaction {
                    exec("SELECT 1") { rs ->
                        rs.next()
                        rs.getInt(1)
                    }
                } ?: -1
            }
            call.respondText("Database connection test: $result")
        }
    }
}
