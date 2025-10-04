package com.trainig.shoppinglist.domain

import com.trainig.shoppinglist.data.Product
import kotlinx.coroutines.flow.Flow

interface ShoppingListRepository {
    fun getAllProducts(): Flow<List<Product>>
    fun getActiveProducts(): Flow<List<Product>>
    fun getCompletedProducts(): Flow<List<Product>>
    suspend fun addProduct(name: String, quantity: String? = null, note: String? = null): Result<Long>
    suspend fun updateProduct(product: Product): Result<Unit>
    suspend fun deleteProduct(product: Product): Result<Unit>
    suspend fun toggleProductDone(productId: Long): Result<Unit>
}
