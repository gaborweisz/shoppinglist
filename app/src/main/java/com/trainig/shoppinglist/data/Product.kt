package com.trainig.shoppinglist.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [Index("is_done"), Index("is_active"), Index("remote_id")]
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
    val isDone: Boolean = false,

    @ColumnInfo(name = "is_active", defaultValue = "0")
    val isActive: Boolean = false,

    // Stable id shared across devices, assigned when the item is first shared
    @ColumnInfo(name = "remote_id")
    val remoteId: String? = null,

    // Id of the shared list this item was received from, null for own items
    @ColumnInfo(name = "shared_list_id")
    val sharedListId: String? = null
)
