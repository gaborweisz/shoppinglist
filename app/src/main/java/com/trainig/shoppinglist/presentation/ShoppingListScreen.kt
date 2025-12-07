package com.trainig.shoppinglist.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trainig.shoppinglist.data.Product
import com.trainig.shoppinglist.presentation.components.EditProductDialog
import com.trainig.shoppinglist.presentation.components.EditCategoryDialog
import com.trainig.shoppinglist.presentation.components.ProductItem
import android.content.Intent
import com.trainig.shoppinglist.R
import com.trainig.shoppinglist.MainActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    modifier: Modifier = Modifier,
    viewModel: ShoppingListViewModel = hiltViewModel()
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var categoryToEdit by remember { mutableStateOf<String?>(null) }
    var showNewShoppingConfirmDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }
    var showNewShoppingMessage by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val products by viewModel.products.collectAsStateWithLifecycle(initialValue = emptyList())
    val categories by viewModel.categories.collectAsStateWithLifecycle(initialValue = emptyList())
    val filter by viewModel.filter.collectAsStateWithLifecycle(initialValue = ProductFilter.ALL)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(initialValue = UiState.Success)
    val darkModePreference by viewModel.isDarkMode.collectAsStateWithLifecycle(initialValue = null)
    val activeProductsCount by viewModel.activeProductsCount.collectAsStateWithLifecycle(initialValue = 0)
    val currentLanguage by viewModel.language.collectAsStateWithLifecycle(initialValue = null)

    // Remove the automatic language change effect that causes infinite loop
    // Language will only change when user explicitly selects it from the dialog

    // Use preference if available, otherwise fall back to system setting
    val isDarkTheme = darkModePreference ?: isSystemInDarkTheme()

    // Dynamic background color based on theme
    val backgroundColor = if (isDarkTheme) {
        androidx.compose.ui.graphics.Color(0xFF2E2E2E) // Dark grey for dark mode
    } else {
        androidx.compose.ui.graphics.Color(0xFFFFFFF0) // Ivory for light mode
    }

    // Use lighter blue in dark mode, darker blue in light mode
    val titleIconColor = if (isDarkTheme) {
        androidx.compose.ui.graphics.Color(0xFF6B9EFF) // Lighter blue for dark mode
    } else {
        androidx.compose.ui.graphics.Color(0xFF00008B) // Dark blue for light mode
    }

    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.shopping_list),
                        style = MaterialTheme.typography.titleLarge,
                        color = titleIconColor
                    )
                },
                actions = {
                    // Language selection button
                    IconButton(
                        onClick = { showLanguageDialog = true },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(
                            text = when (currentLanguage) {
                                "en" -> "EN"
                                "hu" -> "HU"
                                "de" -> "DE"
                                else -> "EN"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = titleIconColor
                        )
                    }

                    // Theme toggle button
                    IconButton(
                        onClick = { viewModel.toggleDarkMode() },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (isDarkTheme) R.drawable.ic_sun else R.drawable.ic_moon
                            ),
                            contentDescription = stringResource(
                                if (isDarkTheme) R.string.switch_to_light_mode else R.string.switch_to_dark_mode
                            ),
                            tint = titleIconColor
                        )
                    }

                    // Share button
                    IconButton(
                        onClick = {
                            val shareText = viewModel.formatShoppingListForSharing(products)
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                                setPackage("com.facebook.orca")
                            }

                            try {
                                context.startActivity(sendIntent)
                            } catch (e: Exception) {
                                val chooserIntent = Intent.createChooser(
                                    sendIntent.apply { setPackage(null) },
                                    context.getString(R.string.share_shopping_list)
                                )
                                context.startActivity(chooserIntent)
                            }
                        },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(R.string.share_shopping_list),
                            tint = titleIconColor
                        )
                    }

                    IconButton(
                        onClick = { /* TODO: Navigate to cart screen */ },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.shopping_list),
                            contentDescription = stringResource(R.string.view_cart),
                            tint = titleIconColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                ),
                modifier = Modifier.height(60.dp)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_product))
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // New Shopping button and Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // New Shopping button on the left
                Button(
                    onClick = {
                        if (activeProductsCount > 0) {
                            showNewShoppingConfirmDialog = true
                        } else {
                            viewModel.startNewShopping()
                            viewModel.setFilter(ProductFilter.ALL)
                            showNewShoppingMessage = true // Show message even when starting from empty
                        }
                    },
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = stringResource(R.string.new_shopping),
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                // Filter chips on the right
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = filter == ProductFilter.ACTIVE,
                        onClick = { viewModel.setFilter(ProductFilter.ACTIVE) },
                        label = { Text(stringResource(R.string.filter_active)) }
                    )
                    FilterChip(
                        selected = filter == ProductFilter.COMPLETED,
                        onClick = { viewModel.setFilter(ProductFilter.COMPLETED) },
                        label = { Text(stringResource(R.string.filter_completed)) }
                    )
                    FilterChip(
                        selected = filter == ProductFilter.ALL,
                        onClick = { viewModel.setFilter(ProductFilter.ALL) },
                        label = { Text(stringResource(R.string.filter_all)) }
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                if (products.isEmpty()) {
                    EmptyState(
                        modifier = Modifier.fillMaxSize(),
                        filter = filter
                    )
                } else {
                    ProductsList(
                        products = products,
                        filter = filter,
                        onToggleDone = viewModel::toggleProductDone,
                        onDelete = { productToDelete = it },
                        onEdit = { productToEdit = it },
                        onCategoryClick = { categoryToEdit = it },
                        onAddToActive = viewModel::addToActiveList,
                        onRemoveFromActive = viewModel::removeFromActiveList,
                        onMarkCompleted = viewModel::markAsCompleted,
                        onMoveBackToActive = viewModel::moveBackToActive,
                        isDarkTheme = isDarkTheme,
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

                // New Shopping message Snackbar
                if (showNewShoppingMessage) {
                    // Auto-dismiss after 3 seconds
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(3000)
                        showNewShoppingMessage = false
                    }

                    Snackbar(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.TopCenter), // Position at top instead of bottom
                        action = {
                            TextButton(
                                onClick = {
                                    // Dismiss the message
                                    showNewShoppingMessage = false
                                }
                            ) {
                                Text(stringResource(R.string.dismiss))
                            }
                        }
                    ) {
                        Text(stringResource(R.string.new_shopping_started))
                    }
                }
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
                // Check if category has changed
                val oldCategory = product.category
                val newCategory = category.trim()

                if (oldCategory != newCategory && oldCategory.isNotBlank()) {
                    // Update all products with the old category to the new category
                    viewModel.updateCategory(oldCategory, newCategory)
                } else {
                    // Just update the current product
                    viewModel.updateProduct(product.copy(
                        name = name,
                        quantity = quantity,
                        note = note,
                        category = newCategory
                    ))
                }
                productToEdit = null
            }
        )
    }

    categoryToEdit?.let { category ->
        EditCategoryDialog(
            currentCategory = category,
            onDismiss = { categoryToEdit = null },
            onSave = { newCategory ->
                viewModel.updateCategory(category, newCategory)
                categoryToEdit = null
            }
        )
    }

    // New Shopping Confirmation Dialog
    if (showNewShoppingConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showNewShoppingConfirmDialog = false },
            title = { Text(stringResource(R.string.start_new_shopping_title)) },
            text = {
                val itemText = if (activeProductsCount != 1) "s" else ""
                Text(stringResource(R.string.start_new_shopping_message, activeProductsCount, itemText))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.startNewShopping()
                        viewModel.setFilter(ProductFilter.ALL)
                        showNewShoppingConfirmDialog = false
                        showNewShoppingMessage = true // Show the snackbar message
                    }
                ) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showNewShoppingConfirmDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Delete Confirmation Dialog
    productToDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text(stringResource(R.string.delete_item_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.delete_item_message))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.delete_item_name, product.name),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    product.note?.takeIf { it.isNotBlank() }?.let { note ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.delete_item_note, note),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteProduct(product)
                        productToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { productToDelete = null }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Language Selection Dialog
    if (showLanguageDialog) {
        val activity = context as? MainActivity
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.select_language)) },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            viewModel.setLanguage("en")
                            showLanguageDialog = false
                            activity?.setLocale("en")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.language_english))
                            if (currentLanguage == "en" || currentLanguage == null) {
                                Text("✓", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                    TextButton(
                        onClick = {
                            viewModel.setLanguage("hu")
                            showLanguageDialog = false
                            activity?.setLocale("hu")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.language_hungarian))
                            if (currentLanguage == "hu") {
                                Text("✓", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                    TextButton(
                        onClick = {
                            viewModel.setLanguage("de")
                            showLanguageDialog = false
                            activity?.setLocale("de")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.language_german))
                            if (currentLanguage == "de") {
                                Text("✓", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showLanguageDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun ProductsList(
    products: List<Product>,
    filter: ProductFilter,
    onToggleDone: (Long) -> Unit,
    onDelete: (Product) -> Unit,
    onEdit: (Product) -> Unit,
    onCategoryClick: (String) -> Unit,
    onAddToActive: (Long) -> Unit,
    onRemoveFromActive: (Long) -> Unit,
    onMarkCompleted: (Long) -> Unit,
    onMoveBackToActive: (Long) -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    // Group products by category
    val uncategorizedLabel = stringResource(R.string.uncategorized)
    val groupedProducts = products.groupBy { it.category.ifBlank { uncategorizedLabel } }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        groupedProducts.forEach { (category, categoryProducts) ->
            // Category header - clickable only if not "Uncategorized"
            item(key = "header_$category") {
                // Use lighter dark green in dark mode
                val categoryColor = if (isDarkTheme) {
                    Color(0xFF6B9B6B) // Lighter dark green for dark mode
                } else {
                    MaterialTheme.colorScheme.primary // Default primary color for light mode
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = if (category != groupedProducts.keys.first()) 12.dp else 0.dp, bottom = 4.dp)
                        .then(
                            if (category != uncategorizedLabel) {
                                Modifier.clickable { onCategoryClick(category) }
                            } else {
                                Modifier
                            }
                        )
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleMedium,
                        color = categoryColor,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Divider(
                        thickness = 2.dp,
                        color = categoryColor
                    )
                }
            }

            // Products in this category
            items(
                items = categoryProducts,
                key = { product -> product.id }
            ) { product ->
                ProductItem(
                    product = product,
                    onToggleDone = { onToggleDone(product.id) },
                    onDelete = { onDelete(product) },
                    onEdit = { onEdit(product) },
                    showCategory = false,
                    showDelete = filter == ProductFilter.ALL,
                    isDarkTheme = isDarkTheme,
                    filter = filter,
                    onAddToActive = { onAddToActive(product.id) },
                    onRemoveFromActive = { onRemoveFromActive(product.id) },
                    onMarkCompleted = { onMarkCompleted(product.id) },
                    onMoveBackToActive = { onMoveBackToActive(product.id) }
                )
            }
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
                Text(stringResource(R.string.dismiss))
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
                ProductFilter.ALL -> stringResource(R.string.no_items)
                ProductFilter.ACTIVE -> stringResource(R.string.no_active_items)
                ProductFilter.COMPLETED -> stringResource(R.string.no_completed_items)
            },
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = when (filter) {
                ProductFilter.ALL -> stringResource(R.string.tap_to_add_items)
                ProductFilter.ACTIVE -> stringResource(R.string.all_items_completed)
                ProductFilter.COMPLETED -> stringResource(R.string.complete_items_to_see)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
