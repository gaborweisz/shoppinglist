package com.trainig.shoppinglist.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.trainig.shoppinglist.data.Product
import kotlin.math.abs

// Generate a consistent light pastel color for each category
private fun getCategoryColor(category: String): Color {
    val hash = category.hashCode()
    val hue = abs(hash % 360)

    // Convert HSL to RGB with light saturation and high lightness for pastel colors
    val saturation = 0.35f + (abs(hash / 360) % 20) / 100f // 35-55% saturation
    val lightness = 0.85f + (abs(hash / 720) % 10) / 100f // 85-95% lightness

    return hslToRgb(hue.toFloat(), saturation, lightness)
}

// Convert HSL to RGB
private fun hslToRgb(hue: Float, saturation: Float, lightness: Float): Color {
    val h = hue / 360f
    val s = saturation
    val l = lightness

    val c = (1f - abs(2f * l - 1f)) * s
    val x = c * (1f - abs((h * 6f) % 2f - 1f))
    val m = l - c / 2f

    val (r, g, b) = when {
        h < 1f / 6f -> Triple(c, x, 0f)
        h < 2f / 6f -> Triple(x, c, 0f)
        h < 3f / 6f -> Triple(0f, c, x)
        h < 4f / 6f -> Triple(0f, x, c)
        h < 5f / 6f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }

    return Color(
        red = (r + m),
        green = (g + m),
        blue = (b + m),
        alpha = 1f
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductItem(
    product: Product,
    onToggleDone: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    showCategory: Boolean = true,
    showDelete: Boolean = true
) {
    // Get the category color for the entire card
    val cardColor = if (product.category.isNotBlank()) {
        getCategoryColor(product.category)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Product item: ${product.name}" +
                    if (product.isDone) " (completed)" else " (active)"
            },
        onClick = onEdit,
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        shape = androidx.compose.ui.graphics.RectangleShape,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp
        ),
        border = null
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 6.dp, vertical = 0.dp)
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .padding(vertical = if (product.note.isNullOrBlank()) 0.dp else 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Checkbox(
                    checked = product.isDone,
                    onCheckedChange = { onToggleDone() },
                    modifier = Modifier
                        .semantics {
                            contentDescription = if (product.isDone) {
                                "Mark ${product.name} as not done"
                            } else {
                                "Mark ${product.name} as done"
                            }
                        }
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = 4.dp,
                        top = 0.dp ,
                        bottom = 0.dp
                    )
            ) {
                // First line: Category (if showCategory is true), Name, and Quantity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category badge and Product name in a Row
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Category badge (only show if showCategory is true)
                        if (showCategory) {
                            product.category.takeIf { it.isNotBlank() }?.let { category ->
                                Text(
                                    text = "📁 $category",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Product name
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.bodyMedium,
                            textDecoration = if (product.isDone) TextDecoration.LineThrough else null,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }

                    // Quantity aligned to the right
                    product.quantity?.takeIf { it.isNotBlank() }?.let { quantity ->
                        Text(
                            text = quantity,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 0.dp)
                        )
                    }
                }

                // Second line: Note (if present)
                product.note?.takeIf { it.isNotBlank() }?.let { note ->
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            if (showDelete) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .padding(vertical = 0.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .semantics {
                                contentDescription = "Delete ${product.name}"
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
