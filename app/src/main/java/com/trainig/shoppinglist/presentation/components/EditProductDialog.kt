package com.trainig.shoppinglist.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.trainig.shoppinglist.data.Product

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
    var isTyping by remember { mutableStateOf(false) }

    // Filter categories based on current input only when actively typing
    val filteredCategories = remember(category, availableCategories, isTyping) {
        if (!isTyping || category.isEmpty()) {
            // Show all categories when not typing or when category is empty
            availableCategories
        } else {
            // Filter only when user is actively typing
            availableCategories.filter {
                it.contains(category, ignoreCase = true)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "Add Product" else "Edit Product") },
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
                    label = { Text("Name") },
                    isError = showError && name.all { it.isWhitespace() },
                    supportingText = if (showError && name.all { it.isWhitespace() }) {
                        { Text("Name cannot be empty") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Category dropdown with autocomplete
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { newExpanded ->
                        expanded = newExpanded
                        if (newExpanded) {
                            // When opening dropdown, reset typing flag to show all categories
                            isTyping = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { newValue ->
                            // Always mark as typing when the value changes (including backspace)
                            isTyping = true
                            category = newValue
                            expanded = true // Show dropdown when typing
                        },
                        label = { Text("Category (optional)") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )

                    if (filteredCategories.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = {
                                expanded = false
                                isTyping = false
                            }
                        ) {
                            filteredCategories.forEach { categoryOption ->
                                DropdownMenuItem(
                                    text = { Text(categoryOption) },
                                    onClick = {
                                        category = categoryOption
                                        expanded = false
                                        isTyping = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optional)") },
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
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
