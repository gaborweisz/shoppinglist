package com.trainig.shoppinglist.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ShoppingListRepositoryImplTest {
    private lateinit var dao: FakeProductDao
    private lateinit var repository: ShoppingListRepositoryImpl

    @Before
    fun setup() {
        dao = FakeProductDao()
        repository = ShoppingListRepositoryImpl(dao)
    }

    @Test
    fun `adding product with valid name succeeds`() = runTest {
        // When
        val result = repository.addProduct("Milk", "1L", "Full fat")

        // Then
        assertTrue(result.isSuccess)
        val products = repository.getAllProducts().first()
        assertEquals(1, products.size)
        assertEquals("Milk", products[0].name)
        assertEquals("1L", products[0].quantity)
        assertEquals("Full fat", products[0].note)
    }

    @Test
    fun `deleting product removes it from list`() = runTest {
        // Given
        val result = repository.addProduct("Milk").getOrThrow()
        val products = repository.getAllProducts().first()
        val product = products[0]

        // When
        repository.deleteProduct(product)

        // Then
        val updatedProducts = repository.getAllProducts().first()
        assertTrue(updatedProducts.isEmpty())
    }

    @Test
    fun `toggling product done status changes isDone`() = runTest {
        // Given
        val id = repository.addProduct("Milk").getOrThrow()

        // When
        repository.toggleProductDone(id)

        // Then
        val products = repository.getAllProducts().first()
        assertTrue(products[0].isDone)
    }

    @Test
    fun `active products filter shows only undone items`() = runTest {
        // Given
        repository.addProduct("Milk")
        val breadId = repository.addProduct("Bread").getOrThrow()
        repository.addProduct("Eggs")
        repository.toggleProductDone(breadId)

        // When
        val activeProducts = repository.getActiveProducts().first()

        // Then
        assertEquals(2, activeProducts.size)
        assertTrue(activeProducts.none { it.isDone })
    }
}
