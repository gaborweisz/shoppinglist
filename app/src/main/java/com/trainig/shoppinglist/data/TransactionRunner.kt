package com.trainig.shoppinglist.data

import androidx.room.withTransaction

interface TransactionRunner {
    suspend fun <R> run(block: suspend () -> R): R
}

class RoomTransactionRunner(private val database: ShoppingListDatabase) : TransactionRunner {
    override suspend fun <R> run(block: suspend () -> R): R = database.withTransaction { block() }
}
