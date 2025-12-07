package com.trainig.shoppinglist.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeProductDao : ProductDao {
    private val products = MutableStateFlow<List<Product>>(emptyList())
    private var idCounter = 1L

    override fun getAllProducts(): Flow<List<Product>> =
        products.map { list ->
            list.sortedWith(compareBy(
                { if (it.category.isEmpty()) 1 else 0 },
                { it.category },
                { it.name }
            ))
        }

    override fun getActiveProducts(): Flow<List<Product>> =
        products.map { list ->
            list.filter { it.isActive && !it.isDone }
                .sortedWith(compareBy(
                    { if (it.category.isEmpty()) 1 else 0 },
                    { it.category },
                    { it.name }
                ))
        }

    override fun getCompletedProducts(): Flow<List<Product>> =
        products.map { list ->
            list.filter { it.isDone }
                .sortedWith(compareBy(
                    { if (it.category.isEmpty()) 1 else 0 },
                    { it.category },
                    { it.name }
                ))
        }

    override fun getDistinctCategories(): Flow<List<String>> =
        products.map { list ->
            list.map { it.category }
                .filter { it.isNotEmpty() }
                .distinct()
                .sorted()
        }

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

    override suspend fun updateCategory(oldCategory: String, newCategory: String) {
        products.update { currentList ->
            currentList.map { product ->
                if (product.category == oldCategory) {
                    product.copy(category = newCategory)
                } else {
                    product
                }
            }
        }
    }

    override suspend fun startNewShopping() {
        products.update { currentList ->
            currentList.map { product ->
                product.copy(isActive = false, isDone = false)
            }
        }
    }

    override suspend fun addToActiveList(productId: Long) {
        products.update { currentList ->
            currentList.map { product ->
                if (product.id == productId) {
                    product.copy(isActive = true, isDone = false)
                } else {
                    product
                }
            }
        }
    }

    override suspend fun removeFromActiveList(productId: Long) {
        products.update { currentList ->
            currentList.map { product ->
                if (product.id == productId) {
                    product.copy(isActive = false, isDone = false)
                } else {
                    product
                }
            }
        }
    }

    override suspend fun markAsCompleted(productId: Long) {
        products.update { currentList ->
            currentList.map { product ->
                if (product.id == productId) {
                    product.copy(isActive = false, isDone = true)
                } else {
                    product
                }
            }
        }
    }

    override suspend fun moveBackToActive(productId: Long) {
        products.update { currentList ->
            currentList.map { product ->
                if (product.id == productId) {
                    product.copy(isActive = true, isDone = false)
                } else {
                    product
                }
            }
        }
    }
}
