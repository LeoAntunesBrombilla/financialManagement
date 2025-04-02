package com.lbDeveloper.plugins.database

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*

object Users : UUIDTable("users") {
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val name = varchar("name", 100)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
}

object Wallet : UUIDTable("wallet") {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE).uniqueIndex()
    val balance = decimal("balance",19,4).default(0.toBigDecimal())
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
}

object Categories : UUIDTable("categories") {
    val name = varchar("name", 100)
    val type = varchar("type", 50)
    val description = text("description").nullable()
    val isSystem = bool("is_system").default(true)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
}

object AllocationRules : UUIDTable("allocation_rules") {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val categoryId = reference("category_id", Categories, onDelete = ReferenceOption.CASCADE)
    val percentage = decimal("percentage", 5, 2)
    val priority = integer("priority").default(0)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
}

object Transactions : UUIDTable("transactions") {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val categoryId = reference("category_id", Categories, onDelete = ReferenceOption.RESTRICT)
    val amount = decimal("amount", 19, 4)
    val description = text("description").nullable()
    val transactionDate = datetime("transaction_date").default(LocalDateTime.now())
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
}

object DatabaseMigration {
    fun up() {
        transaction {
            SchemaUtils.create(
                Users,
                Wallet,
                Categories,
                AllocationRules,
                Transactions
            )

            createDefaultCategories()
        }
    }

    fun down() {
        transaction {
            SchemaUtils.drop(
                Transactions,
                AllocationRules,
                Categories,
                Wallet,
                Users
            )
        }
    }

    private fun createDefaultCategories() {
        val incomeCategories = listOf(
            "Salario",
            "Investimentos",
            "Freelance",
            "Outros"
        )

        val expenseCategories = listOf(
            "Investimentos a longo prazo",
            "Investimentos a curto prazo",
            "Gastos obrigatórios",
            "Saúde",
            "Estudo"
        )

        for (name in incomeCategories) {
            Categories.insert {
                it[id] = UUID.randomUUID()
                it[Categories.name] = name
                it[type] = "INCOME"
                it[isSystem] = true
            }
        }

        for (name in expenseCategories) {
            Categories.insert {
                it[id] = UUID.randomUUID()
                it[Categories.name] = name
                it[type] = "EXPENSE"
                it[isSystem] = true
            }
        }
    }
}