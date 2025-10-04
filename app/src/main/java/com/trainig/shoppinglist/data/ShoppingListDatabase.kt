package com.trainig.shoppinglist.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Product::class],
    version = 1,
    exportSchema = true
)
abstract class ShoppingListDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
}
