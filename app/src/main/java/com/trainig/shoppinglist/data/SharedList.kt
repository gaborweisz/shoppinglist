package com.trainig.shoppinglist.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

/**
 * A shopping list received from another user, with the last version the user accepted.
 */
@Entity(tableName = "shared_lists")
data class SharedList(
    @PrimaryKey
    @ColumnInfo(name = "list_id")
    val listId: String,

    @ColumnInfo(name = "applied_version")
    val appliedVersion: Int
)

@Dao
interface SharedListDao {
    @Query("SELECT applied_version FROM shared_lists WHERE list_id = :listId")
    suspend fun getAppliedVersion(listId: String): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(sharedList: SharedList)
}
