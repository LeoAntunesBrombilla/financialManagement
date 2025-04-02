package com.lbDeveloper.plugins.database

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class User(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<User>(Users)

    var email by Users.email
    var passwordHash by Users.passwordHash
    var name by Users.name
    var createdAt by Users.createdAt
    var updatedAt by Users.updatedAt

    val wallet by UserWallet referencedOn Wallet.userId

    val allocationRules by AllocationRule referrersOn AllocationRules.userId
    val transactions by Transaction referrersOn Transactions.userId
}

class UserWallet(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserWallet>(Wallet)

    var user by User referencedOn Wallet.userId
    var balance by Wallet.balance
    var createdAt by Wallet.createdAt
    var updatedAt by Wallet.updatedAt
}

class Category(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Category>(Categories)

    var name by Categories.name
    var type by Categories.type
    var description by Categories.description
    var isSystem by Categories.isSystem
    var createdAt by Categories.createdAt
    var updatedAt by Categories.updatedAt

    val allocationRules by AllocationRule referrersOn AllocationRules.categoryId
    val transactions by Transaction referrersOn Transactions.categoryId
}

class AllocationRule(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<AllocationRule>(AllocationRules)

    var user by User referencedOn AllocationRules.userId
    var category by Category referencedOn AllocationRules.categoryId
    var percentage by AllocationRules.percentage
    var priority by AllocationRules.priority
    var createdAt by AllocationRules.createdAt
    var updatedAt by AllocationRules.updatedAt
}

class Transaction(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Transaction>(Transactions)

    var user by User referencedOn Transactions.userId
    var category by Category referencedOn Transactions.categoryId
    var amount by Transactions.amount
    var description by Transactions.description
    var transactionDate by Transactions.transactionDate
    var createdAt by Transactions.createdAt
    var updatedAt by Transactions.updatedAt
}

enum class TransactionType {
    INCOME, EXPENSE
}