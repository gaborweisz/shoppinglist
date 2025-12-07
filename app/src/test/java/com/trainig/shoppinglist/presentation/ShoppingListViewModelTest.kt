package com.trainig.shoppinglist.presentation

import com.trainig.shoppinglist.data.FakeProductDao
import com.trainig.shoppinglist.data.ShoppingListRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ShoppingListViewModelTest {
    private lateinit var viewModel: ShoppingListViewModel
    private lateinit var repository: ShoppingListRepositoryImpl
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = ShoppingListRepositoryImpl(FakeProductDao())
        viewModel = ShoppingListViewModel(
            repository,
            themePreferences = FakeThemePreferences()
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `adding product with empty name shows error`() = runTest(testDispatcher) {
        // When
        viewModel.addProduct("")
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is UiState.Error)
        assertEquals("Product name cannot be empty", (state as UiState.Error).message)
    }

    @Test
    fun `adding valid product succeeds`() = runTest(testDispatcher) {
        // Start collecting from products flow
        val collectJob = launch {
            viewModel.products.collect { }
        }

        // When
        viewModel.addProduct("Milk", "1L")
        advanceUntilIdle()

        // Then - switch to ALL filter to see the product
        viewModel.setFilter(ProductFilter.ALL)
        advanceUntilIdle()

        val products = viewModel.products.value
        assertEquals(1, products.size)
        assertEquals("Milk", products[0].name)
        assertEquals("1L", products[0].quantity)

        collectJob.cancel()
    }

    @Test
    fun `filter changes update visible products`() = runTest(testDispatcher) {
        // Start collecting from products flow
        val collectJob = launch {
            viewModel.products.collect { }
        }

        // Given - add products to ALL list first
        viewModel.setFilter(ProductFilter.ALL)
        advanceUntilIdle()

        viewModel.addProduct("Milk")
        advanceUntilIdle()
        viewModel.addProduct("Bread")
        advanceUntilIdle()

        // Get all products and add Bread to active list
        val allProducts = viewModel.products.value
        val breadProduct = allProducts.first { it.name == "Bread" }

        // Add Bread to active list
        viewModel.addToActiveList(breadProduct.id)
        advanceUntilIdle()

        // When - switch to ACTIVE filter
        viewModel.setFilter(ProductFilter.ACTIVE)
        advanceUntilIdle()

        // Then - Bread should be the only active product
        val activeProducts = viewModel.products.value
        assertEquals(1, activeProducts.size)
        assertEquals("Bread", activeProducts[0].name)
        assertTrue(activeProducts[0].isActive)

        collectJob.cancel()
    }

    @Test
    fun `start new shopping clears active and completed lists`() = runTest(testDispatcher) {
        val collectJob = launch {
            viewModel.products.collect { }
        }

        // Given - add products and activate them
        viewModel.setFilter(ProductFilter.ALL)
        advanceUntilIdle()

        viewModel.addProduct("Milk")
        advanceUntilIdle()
        viewModel.addProduct("Bread")
        advanceUntilIdle()

        var allProducts = viewModel.products.value
        assertEquals(2, allProducts.size) // Verify we have 2 products

        allProducts.forEach { product ->
            viewModel.addToActiveList(product.id)
        }
        advanceUntilIdle()

        // Get fresh list after activating
        viewModel.setFilter(ProductFilter.ALL)
        advanceUntilIdle()
        allProducts = viewModel.products.value

        // Mark one as completed
        viewModel.markAsCompleted(allProducts[0].id)
        advanceUntilIdle()

        // When - start new shopping
        viewModel.startNewShopping()
        advanceUntilIdle()

        // Then - active and completed should be empty
        viewModel.setFilter(ProductFilter.ACTIVE)
        advanceUntilIdle()
        assertEquals(0, viewModel.products.value.size)

        viewModel.setFilter(ProductFilter.COMPLETED)
        advanceUntilIdle()
        assertEquals(0, viewModel.products.value.size)

        // But ALL should still have the products
        viewModel.setFilter(ProductFilter.ALL)
        advanceUntilIdle()
        assertEquals(2, viewModel.products.value.size)

        collectJob.cancel()
    }

    @Test
    fun `mark as completed moves from active to completed`() = runTest(testDispatcher) {
        val collectJob = launch {
            viewModel.products.collect { }
        }

        // Given - add product to active list
        viewModel.setFilter(ProductFilter.ALL)
        advanceUntilIdle()

        viewModel.addProduct("Milk")
        advanceUntilIdle()

        val product = viewModel.products.value.firstOrNull()
        assertNotNull("Product should be added", product)

        viewModel.addToActiveList(product!!.id)
        advanceUntilIdle()

        // When - mark as completed
        viewModel.markAsCompleted(product.id)
        advanceUntilIdle()

        // Then - should be in completed, not in active
        viewModel.setFilter(ProductFilter.ACTIVE)
        advanceUntilIdle()
        assertEquals(0, viewModel.products.value.size)

        viewModel.setFilter(ProductFilter.COMPLETED)
        advanceUntilIdle()
        assertEquals(1, viewModel.products.value.size)
        assertTrue(viewModel.products.value[0].isDone)

        collectJob.cancel()
    }

    @Test
    fun `move back to active from completed`() = runTest(testDispatcher) {
        val collectJob = launch {
            viewModel.products.collect { }
        }

        // Given - add product and mark as completed
        viewModel.setFilter(ProductFilter.ALL)
        advanceUntilIdle()

        viewModel.addProduct("Milk")
        advanceUntilIdle()

        val product = viewModel.products.value.firstOrNull()
        assertNotNull("Product should be added", product)

        viewModel.addToActiveList(product!!.id)
        advanceUntilIdle()
        viewModel.markAsCompleted(product.id)
        advanceUntilIdle()

        // When - move back to active
        viewModel.moveBackToActive(product.id)
        advanceUntilIdle()

        // Then - should be in active, not in completed
        viewModel.setFilter(ProductFilter.COMPLETED)
        advanceUntilIdle()
        assertEquals(0, viewModel.products.value.size)

        viewModel.setFilter(ProductFilter.ACTIVE)
        advanceUntilIdle()
        assertEquals(1, viewModel.products.value.size)
        assertFalse(viewModel.products.value[0].isDone)
        assertTrue(viewModel.products.value[0].isActive)

        collectJob.cancel()
    }
}
