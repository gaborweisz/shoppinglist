package com.trainig.shoppinglist.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeProductDao : ProductDao {
    private val products = MutableStateFlow<List<Product>>(emptyList())
    private var idCounter = 1L

    override fun getAllProducts(): Flow<List<Product>> = products

    override fun getActiveProducts(): Flow<List<Product>> =
        products.map { list -> list.filter { !it.isDone } }

    override fun getCompletedProducts(): Flow<List<Product>> =
        products.map { list -> list.filter { it.isDone } }

    override suspend fun insertProduct(product: Product): Long {
        val id = idCounter++
        products.update { currentList ->
            currentList + product.copy(id = id)
        }
        return id
    }

    override suspend fun updateProduct(product: Product) {
        products.update { currentList ->
            currentList.map { if (it.id == product.id) product else it }
        }
    }

    override suspend fun deleteProduct(product: Product) {
        products.update { currentList ->
            currentList.filter { it.id != product.id }
        }
    }

    override suspend fun deleteProductById(productId: Long) {
        products.update { currentList ->
            currentList.filter { it.id != productId }
        }
    }
}
