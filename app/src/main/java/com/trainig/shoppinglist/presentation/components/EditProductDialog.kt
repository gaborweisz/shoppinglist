package com.trainig.shoppinglist.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

    // Filter categories based on current input
    val filteredCategories = remember(category, availableCategories) {
        if (category.isEmpty()) {
            availableCategories
        } else {
            availableCategories.filter { categoryOption ->
                categoryOption.contains(category as CharSequence, ignoreCase = true)
            }
        }
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

                // Category dropdown with autocomplete
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { newExpanded ->
                        expanded = newExpanded
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { newValue ->
                            category = newValue
                            expanded = true // Show dropdown when typing
                        },
                        label = { Text("${stringResource(R.string.category)} (${stringResource(R.string.optional)})") },
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
                            },
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                            filteredCategories.forEach { categoryOption ->
                                DropdownMenuItem(
                                    text = { Text(categoryOption) },
                                    onClick = {
                                        category = categoryOption
                                        expanded = false
                                    }
                                )
                            }
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
