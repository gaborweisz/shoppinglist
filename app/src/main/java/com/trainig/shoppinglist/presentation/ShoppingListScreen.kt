package com.trainig.shoppinglist.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trainig.shoppinglist.data.Product
import com.trainig.shoppinglist.presentation.components.EditProductDialog
import com.trainig.shoppinglist.presentation.components.ProductItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    modifier: Modifier = Modifier,
    viewModel: ShoppingListViewModel = hiltViewModel()
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }

    val products by viewModel.products.collectAsStateWithLifecycle(initialValue = emptyList())
    val categories by viewModel.categories.collectAsStateWithLifecycle(initialValue = emptyList())
    val filter by viewModel.filter.collectAsStateWithLifecycle(initialValue = ProductFilter.ALL)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(initialValue = UiState.Success)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Shopping List") },
                actions = {
                    FilterChips(
                        currentFilter = filter,
                        onFilterSelected = viewModel::setFilter,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add product")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (products.isEmpty()) {
                EmptyState(
                    modifier = Modifier.fillMaxSize(),
                    filter = filter
                )
            } else {
                ProductsList(
                    products = products,
                    onToggleDone = viewModel::toggleProductDone,
                    onDelete = viewModel::deleteProduct,
                    onEdit = { productToEdit = it },
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (uiState is UiState.Error) {
                ErrorSnackbar(
                    message = (uiState as UiState.Error).message,
                    onDismiss = viewModel::clearError,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomCenter)
                )
            }
        }
    }

    if (showAddDialog) {
        EditProductDialog(
            product = null,
            availableCategories = categories,
            onDismiss = { showAddDialog = false },
            onSave = { name, quantity, note, category ->
                viewModel.addProduct(name, quantity, note, category)
                showAddDialog = false
            }
        )
    }

    productToEdit?.let { product ->
        EditProductDialog(
            product = product,
            availableCategories = categories,
            onDismiss = { productToEdit = null },
            onSave = { name, quantity, note, category ->
                viewModel.updateProduct(product.copy(
                    name = name,
                    quantity = quantity,
                    note = note,
                    category = category
                ))
                productToEdit = null
            }
        )
    }
}

@Composable
private fun ProductsList(
    products: List<Product>,
    onToggleDone: (Long) -> Unit,
    onDelete: (Product) -> Unit,
    onEdit: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = products,
            key = { product -> product.id }
        ) { product ->
            ProductItem(
                product = product,
                onToggleDone = { onToggleDone(product.id) },
                onDelete = { onDelete(product) },
                onEdit = { onEdit(product) }
            )
        }
    }
}

@Composable
private fun ErrorSnackbar(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Snackbar(
        modifier = modifier
            .padding(16.dp),
        action = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    ) {
        Text(message)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChips(
    currentFilter: ProductFilter,
    onFilterSelected: (ProductFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.padding(horizontal = 16.dp)
    ) {
        FilterChip(
            selected = currentFilter == ProductFilter.ALL,
            onClick = { onFilterSelected(ProductFilter.ALL) },
            label = { Text("All") }
        )
        FilterChip(
            selected = currentFilter == ProductFilter.ACTIVE,
            onClick = { onFilterSelected(ProductFilter.ACTIVE) },
            label = { Text("Active") }
        )
        FilterChip(
            selected = currentFilter == ProductFilter.COMPLETED,
            onClick = { onFilterSelected(ProductFilter.COMPLETED) },
            label = { Text("Completed") }
        )
    }
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier,
    filter: ProductFilter
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = when (filter) {
                ProductFilter.ALL -> "No items in your shopping list"
                ProductFilter.ACTIVE -> "No active items"
                ProductFilter.COMPLETED -> "No completed items"
            },
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = when (filter) {
                ProductFilter.ALL -> "Tap + to add items"
                ProductFilter.ACTIVE -> "All items are completed"
                ProductFilter.COMPLETED -> "Complete some items to see them here"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
