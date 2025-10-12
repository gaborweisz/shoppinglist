package com.trainig.shoppinglist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trainig.shoppinglist.data.Product
import com.trainig.shoppinglist.domain.ShoppingListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val repository: ShoppingListRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(ProductFilter.ALL)
    val filter: StateFlow<ProductFilter> = _filter

    val products = combine(
        _filter,
        repository.getAllProducts(),
        repository.getActiveProducts(),
        repository.getCompletedProducts()
    ) { filter, all, active, completed ->
        when (filter) {
            ProductFilter.ALL -> all
            ProductFilter.ACTIVE -> active
            ProductFilter.COMPLETED -> completed
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val categories = repository.getDistinctCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow<UiState>(UiState.Success)
    val uiState: StateFlow<UiState> = _uiState

    fun setFilter(filter: ProductFilter) {
        _filter.value = filter
    }

    fun addProduct(name: String, quantity: String? = null, note: String? = null, category: String = "") {
        if (name.isBlank()) {
            _uiState.value = UiState.Error("Product name cannot be empty")
            return
        }

        viewModelScope.launch {
            repository.addProduct(name, quantity, note, category)
                .onFailure { _uiState.value = UiState.Error(it.message ?: "Failed to add product") }
        }
    }

    fun updateProduct(product: Product) {
        if (product.name.isBlank()) {
            _uiState.value = UiState.Error("Product name cannot be empty")
            return
        }

        viewModelScope.launch {
            repository.updateProduct(product)
                .onFailure { _uiState.value = UiState.Error(it.message ?: "Failed to update product") }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
                .onFailure { _uiState.value = UiState.Error(it.message ?: "Failed to delete product") }
        }
    }

    fun toggleProductDone(productId: Long) {
        viewModelScope.launch {
            repository.toggleProductDone(productId)
                .onFailure { _uiState.value = UiState.Error(it.message ?: "Failed to update product") }
        }
    }

    fun clearError() {
        _uiState.value = UiState.Success
    }

    fun formatShoppingListForSharing(products: List<Product>): String {
        if (products.isEmpty()) {
            return "My Shopping List is empty"
        }

        val groupedProducts = products.groupBy { it.category.ifBlank { "Uncategorized" } }
        val stringBuilder = StringBuilder()
        stringBuilder.append("🛒 My Shopping List\n")
        stringBuilder.append("━━━━━━━━━━━━━━━━━━━━\n\n")

        groupedProducts.forEach { (category, categoryProducts) ->
            stringBuilder.append("📁 $category\n")
            stringBuilder.append("─────────────────────\n")

            categoryProducts.forEachIndexed { index, product ->
                val checkbox = if (product.isDone) "☑" else "☐"
                stringBuilder.append("$checkbox ${product.name}")

                if (!product.quantity.isNullOrBlank()) {
                    stringBuilder.append(" - ${product.quantity}")
                }

                if (!product.note.isNullOrBlank()) {
                    stringBuilder.append("\n   Note: ${product.note}")
                }

                stringBuilder.append("\n")
            }
            stringBuilder.append("\n")
        }

        return stringBuilder.toString()
    }
}

enum class ProductFilter {
    ALL, ACTIVE, COMPLETED
}

sealed interface UiState {
    data object Success : UiState
    data class Error(val message: String) : UiState
}
