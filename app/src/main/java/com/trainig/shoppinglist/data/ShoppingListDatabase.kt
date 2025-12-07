package com.trainig.shoppinglist.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Product::class],
    version = 3,
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

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE products ADD COLUMN is_active INTEGER NOT NULL DEFAULT 0")
        db.execSQL("CREATE INDEX index_products_is_active ON products(is_active)")
    }
}
