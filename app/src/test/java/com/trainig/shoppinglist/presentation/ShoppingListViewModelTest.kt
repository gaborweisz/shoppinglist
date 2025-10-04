package com.trainig.shoppinglist.presentation

import com.trainig.shoppinglist.data.FakeProductDao
import com.trainig.shoppinglist.data.ShoppingListRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
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
    private val testDispatcher = StandardTestDispatcher()

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
    fun `adding product with empty name shows error`() = runTest {
        // When
        viewModel.addProduct("")

        // Then
        val state = viewModel.uiState.first()
        assertTrue(state is UiState.Error)
        assertEquals("Product name cannot be empty", (state as UiState.Error).message)
    }

    @Test
    fun `adding valid product succeeds`() = runTest {
        // When
        viewModel.addProduct("Milk", "1L")

        // Then
        val products = viewModel.products.first()
        assertEquals(1, products.size)
        assertEquals("Milk", products[0].name)
        assertEquals("1L", products[0].quantity)
    }

    @Test
    fun `filter changes update visible products`() = runTest {
        // Given
        viewModel.addProduct("Milk")
        viewModel.addProduct("Bread")
        val products = viewModel.products.first()
        viewModel.toggleProductDone(products[0].id)

        // When
        viewModel.setFilter(ProductFilter.ACTIVE)

        // Then
        val activeProducts = viewModel.products.first()
        assertEquals(1, activeProducts.size)
        assertEquals("Bread", activeProducts[0].name)
    }
}
