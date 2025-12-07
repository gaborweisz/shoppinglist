package com.trainig.shoppinglist.data

import com.trainig.shoppinglist.domain.ShoppingListRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShoppingListRepositoryImpl @Inject constructor(
    private val productDao: ProductDao
) : ShoppingListRepository {
    override fun getAllProducts(): Flow<List<Product>> = productDao.getAllProducts()

    override fun getActiveProducts(): Flow<List<Product>> = productDao.getActiveProducts()

    override fun getCompletedProducts(): Flow<List<Product>> = productDao.getCompletedProducts()

    override fun getDistinctCategories(): Flow<List<String>> = productDao.getDistinctCategories()

    override suspend fun addProduct(name: String, quantity: String?, note: String?, category: String): Result<Long> {
        return kotlin.runCatching {
            val product = Product(
                name = name.trim(),
                quantity = quantity?.trim(),
                note = note?.trim(),
                category = category.trim()
            )
            productDao.insertProduct(product)
        }
    }

    override suspend fun updateProduct(product: Product): Result<Unit> {
        return kotlin.runCatching {
            productDao.updateProduct(product)
        }
    }

    override suspend fun deleteProduct(product: Product): Result<Unit> {
        return kotlin.runCatching {
            productDao.deleteProduct(product)
        }
    }

    override suspend fun toggleProductDone(productId: Long): Result<Unit> {
        return kotlin.runCatching {
            val products = productDao.getAllProducts().first()
            products.firstOrNull { it.id == productId }?.let { product ->
                productDao.updateProduct(product.copy(isDone = !product.isDone))
            }
        }
    }

    override suspend fun updateCategory(oldCategory: String, newCategory: String): Result<Unit> {
        return kotlin.runCatching {
            productDao.updateCategory(oldCategory.trim(), newCategory.trim())
        }
    }
}
