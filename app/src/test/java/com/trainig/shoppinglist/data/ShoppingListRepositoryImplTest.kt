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
    fun `active products filter shows only active and undone items`() = runTest {
        // Given
        val milkId = repository.addProduct("Milk").getOrThrow()
        val breadId = repository.addProduct("Bread").getOrThrow()
        val eggsId = repository.addProduct("Eggs").getOrThrow()

        // Add milk and bread to active list
        repository.addToActiveList(milkId)
        repository.addToActiveList(breadId)

        // Mark bread as completed (moves from active to completed)
        repository.markAsCompleted(breadId)

        // When
        val activeProducts = repository.getActiveProducts().first()

        // Then
        assertEquals(1, activeProducts.size)
        assertEquals("Milk", activeProducts[0].name)
        assertTrue(activeProducts[0].isActive)
        assertTrue(activeProducts.none { it.isDone })
    }

    @Test
    fun `completed products filter shows only done items`() = runTest {
        // Given
        val milkId = repository.addProduct("Milk").getOrThrow()
        val breadId = repository.addProduct("Bread").getOrThrow()

        // Add to active list first
        repository.addToActiveList(milkId)
        repository.addToActiveList(breadId)

        // Mark milk as completed
        repository.markAsCompleted(milkId)

        // When
        val completedProducts = repository.getCompletedProducts().first()

        // Then
        assertEquals(1, completedProducts.size)
        assertEquals("Milk", completedProducts[0].name)
        assertTrue(completedProducts[0].isDone)
    }

    @Test
    fun `start new shopping clears active and completed flags`() = runTest {
        // Given
        val milkId = repository.addProduct("Milk").getOrThrow()
        repository.addToActiveList(milkId)
        repository.markAsCompleted(milkId)

        // When
        repository.startNewShopping()

        // Then
        val allProducts = repository.getAllProducts().first()
        assertEquals(1, allProducts.size)
        assertEquals(false, allProducts[0].isActive)
        assertEquals(false, allProducts[0].isDone)

        val activeProducts = repository.getActiveProducts().first()
        assertEquals(0, activeProducts.size)

        val completedProducts = repository.getCompletedProducts().first()
        assertEquals(0, completedProducts.size)
    }
}
