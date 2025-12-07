package com.trainig.shoppinglist.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY CASE WHEN category = '' THEN 1 ELSE 0 END, category ASC, name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE is_active = 1 AND is_done = 0 ORDER BY CASE WHEN category = '' THEN 1 ELSE 0 END, category ASC, name ASC")
    fun getActiveProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE is_done = 1 ORDER BY CASE WHEN category = '' THEN 1 ELSE 0 END, category ASC, name ASC")
    fun getCompletedProducts(): Flow<List<Product>>

    @Query("SELECT DISTINCT category FROM products WHERE category != '' ORDER BY category ASC")
    fun getDistinctCategories(): Flow<List<String>>

    @Insert
    suspend fun insertProduct(product: Product): Long

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteProductById(productId: Long)

    @Query("UPDATE products SET category = :newCategory WHERE category = :oldCategory")
    suspend fun updateCategory(oldCategory: String, newCategory: String)

    @Query("UPDATE products SET is_active = 0, is_done = 0")
    suspend fun startNewShopping()

    @Query("UPDATE products SET is_active = 1, is_done = 0 WHERE id = :productId")
    suspend fun addToActiveList(productId: Long)

    @Query("UPDATE products SET is_active = 0, is_done = 0 WHERE id = :productId")
    suspend fun removeFromActiveList(productId: Long)

    @Query("UPDATE products SET is_active = 0, is_done = 1 WHERE id = :productId")
    suspend fun markAsCompleted(productId: Long)

    @Query("UPDATE products SET is_active = 1, is_done = 0 WHERE id = :productId")
    suspend fun moveBackToActive(productId: Long)
}
