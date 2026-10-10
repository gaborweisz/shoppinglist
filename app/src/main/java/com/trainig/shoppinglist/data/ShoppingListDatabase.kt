package com.trainig.shoppinglist.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Product::class, SharedList::class],
    version = 4,
    exportSchema = true
)
abstract class ShoppingListDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun sharedListDao(): SharedListDao
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

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE products ADD COLUMN remote_id TEXT")
        db.execSQL("ALTER TABLE products ADD COLUMN shared_list_id TEXT")
        db.execSQL("CREATE INDEX index_products_remote_id ON products(remote_id)")
        db.execSQL("CREATE TABLE IF NOT EXISTS shared_lists (list_id TEXT NOT NULL, applied_version INTEGER NOT NULL, PRIMARY KEY(list_id))")
    }
}
