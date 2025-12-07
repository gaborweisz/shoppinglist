package com.trainig.shoppinglist.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.trainig.shoppinglist.data.Product
import com.trainig.shoppinglist.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductDialog(
    product: Product?,
    availableCategories: List<String>,
    onDismiss: () -> Unit,
    onSave: (String, String?, String?, String) -> Unit
) {
    var name by remember(product) { mutableStateOf(product?.name ?: "") }
    var quantity by remember(product) { mutableStateOf(product?.quantity ?: "") }
    var note by remember(product) { mutableStateOf(product?.note ?: "") }
    var category by remember(product) { mutableStateOf(product?.category ?: "") }
    var showError by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }

    // Filter categories based on current input - always recalculate on change
    val filteredCategories = availableCategories.filter { categoryOption ->
        category.isEmpty() || categoryOption.contains(category, ignoreCase = true)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (product == null)
                    stringResource(R.string.add_product_title)
                else
                    stringResource(R.string.edit_product)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        showError = false
                    },
                    label = { Text(stringResource(R.string.name)) },
                    isError = showError && name.all { it.isWhitespace() },
                    supportingText = if (showError && name.all { it.isWhitespace() }) {
                        { Text("Name cannot be empty") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("${stringResource(R.string.quantity)} (${stringResource(R.string.optional)})") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Category field with dropdown - using Box with regular DropdownMenu
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { newValue ->
                            category = newValue
                            // Keep dropdown open, but don't force it open if user closed it
                            // Only auto-open when field is focused
                        },
                        label = { Text("${stringResource(R.string.category)} (${stringResource(R.string.optional)})") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused && !isFocused) {
                                    // When field gains focus, show all categories
                                    isFocused = true
                                    expanded = true
                                } else if (!focusState.isFocused) {
                                    isFocused = false
                                    // Close dropdown when focus is lost
                                    expanded = false
                                }
                            },
                        singleLine = true
                    )

                    DropdownMenu(
                        expanded = expanded && (filteredCategories.isNotEmpty() || availableCategories.isNotEmpty()),
                        onDismissRequest = {
                            expanded = false
                            isFocused = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                    ) {
                        // Show filtered categories, or all categories if filter is empty
                        val categoriesToShow = if (category.isEmpty()) availableCategories else filteredCategories

                        categoriesToShow.forEach { categoryOption ->
                            DropdownMenuItem(
                                text = { Text(categoryOption) },
                                onClick = {
                                    category = categoryOption
                                    expanded = false
                                    isFocused = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("${stringResource(R.string.note)} (${stringResource(R.string.optional)})") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.all { it.isWhitespace() }) {
                        showError = true
                        return@TextButton
                    }
                    onSave(
                        name.trim(),
                        quantity.trim().takeUnless { it.all { c -> c.isWhitespace() } },
                        note.trim().takeUnless { it.all { c -> c.isWhitespace() } },
                        category.trim()
                    )
                }
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
