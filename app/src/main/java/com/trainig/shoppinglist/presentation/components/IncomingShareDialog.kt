package com.trainig.shoppinglist.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.trainig.shoppinglist.R
import com.trainig.shoppinglist.share.IncomingSharePreview
import com.trainig.shoppinglist.share.SharedItem

/**
 * Asks the user to accept a received shopping list ([IncomingSharePreview.NewList]) or an update
 * of an already accepted one ([IncomingSharePreview.Update]).
 */
@Composable
fun IncomingShareDialog(
    preview: IncomingSharePreview,
    onAccept: (replaceActiveList: Boolean) -> Unit,
    onDecline: () -> Unit
) {
    when (preview) {
        is IncomingSharePreview.NewList -> AlertDialog(
            onDismissRequest = onDecline,
            title = { Text(stringResource(R.string.incoming_new_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.incoming_new_message, preview.payload.items.size))
                    SharedItemList(preview.payload.items)
                    if (preview.plan.keptCompletedCount > 0) {
                        Text(stringResource(R.string.incoming_kept_completed, preview.plan.keptCompletedCount))
                    }
                }
            },
            confirmButton = {
                if (preview.hasActiveItems) {
                    Column {
                        TextButton(onClick = { onAccept(false) }) {
                            Text(stringResource(R.string.incoming_add_to_list))
                        }
                        TextButton(onClick = { onAccept(true) }) {
                            Text(stringResource(R.string.incoming_replace_list))
                        }
                    }
                } else {
                    TextButton(onClick = { onAccept(false) }) {
                        Text(stringResource(R.string.incoming_accept))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onDecline) {
                    Text(stringResource(R.string.incoming_decline))
                }
            }
        )

        is IncomingSharePreview.Update -> AlertDialog(
            onDismissRequest = onDecline,
            title = { Text(stringResource(R.string.incoming_update_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val plan = preview.plan
                    if (plan.hasChanges) {
                        Text(
                            stringResource(
                                R.string.incoming_update_summary,
                                plan.addedCount,
                                plan.changedCount,
                                plan.removedCount
                            )
                        )
                    } else {
                        Text(stringResource(R.string.incoming_update_no_changes))
                    }
                    if (plan.keptCompletedCount > 0) {
                        Text(stringResource(R.string.incoming_kept_completed, plan.keptCompletedCount))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { onAccept(false) }) {
                    Text(stringResource(R.string.incoming_accept))
                }
            },
            dismissButton = {
                TextButton(onClick = onDecline) {
                    Text(stringResource(R.string.incoming_decline))
                }
            }
        )

        else -> Unit
    }
}

@Composable
private fun SharedItemList(items: List<SharedItem>) {
    LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
        items(items) { item ->
            val quantity = item.quantity?.takeIf { it.isNotBlank() }?.let { " - $it" } ?: ""
            Text(
                text = "• ${item.name}$quantity",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
