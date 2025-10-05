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
        viewModel = ShoppingListViewModel(repository)
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

        // Then
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

        // Given - add products (they will be sorted alphabetically: Bread, Milk)
        viewModel.addProduct("Milk")
        advanceUntilIdle()
        viewModel.addProduct("Bread")
        advanceUntilIdle()

        // After sorting, products are: [Bread, Milk]
        val allProducts = viewModel.products.value
        // Find Milk and toggle it as done
        val milkProduct = allProducts.first { it.name == "Milk" }
        viewModel.toggleProductDone(milkProduct.id)
        advanceUntilIdle()

        // When
        viewModel.setFilter(ProductFilter.ACTIVE)
        advanceUntilIdle()

        // Then - Bread should be the only active product
        val activeProducts = viewModel.products.value
        assertEquals(1, activeProducts.size)
        assertEquals("Bread", activeProducts[0].name)

        collectJob.cancel()
    }
}
