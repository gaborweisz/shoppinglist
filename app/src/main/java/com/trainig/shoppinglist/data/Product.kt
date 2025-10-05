package com.trainig.shoppinglist.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [Index("is_done")]
)
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "quantity", defaultValue = "NULL")
    val quantity: String? = null,

    @ColumnInfo(name = "note", defaultValue = "NULL")
    val note: String? = null,

    @ColumnInfo(name = "category", defaultValue = "''")
    val category: String = "",

    @ColumnInfo(name = "is_done", defaultValue = "0")
    val isDone: Boolean = false
)
