package com.trainig.shoppinglist.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Product::class],
    version = 2,
    exportSchema = true
)
abstract class ShoppingListDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE products ADD COLUMN category TEXT NOT NULL DEFAULT ''")
    }
}
