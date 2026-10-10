package com.trainig.shoppinglist.share

import com.trainig.shoppinglist.data.Product

/**
 * The changes needed to bring the local shopping list in line with a received [SharePayload].
 */
data class MergePlan(
    val inserts: List<Product>,
    val updates: List<Product>,
    val removeFromActive: List<Long>,
    val addedCount: Int,
    val changedCount: Int,
    val keptCompletedCount: Int
) {
    val removedCount: Int get() = removeFromActive.size
    val hasChanges: Boolean get() = inserts.isNotEmpty() || updates.isNotEmpty() || removeFromActive.isNotEmpty()
}

object ShareMerge {

    /**
     * Rules:
     * - Each received item is matched to a local product by remote id, otherwise by name and
     *   category (case-insensitive), so the catalog does not fill with duplicates.
     * - Unmatched items are inserted onto the active list.
     * - Matched items that are already completed stay completed and are not changed.
     * - Other matched items get the sender's name, quantity, note and category and become active.
     * - When [removeMissing] is set (an update of an already accepted list), items that came from
     *   this list, are still active and are no longer in it are removed from the active list.
     *   Own items of the receiver are never touched.
     */
    fun plan(local: List<Product>, payload: SharePayload, removeMissing: Boolean): MergePlan {
        val incomingRemoteIds = payload.items.map { it.remoteId }.toSet()
        val byRemoteId = local.filter { it.remoteId != null }.associateBy { it.remoteId }
        val byName = local
            .filter { it.remoteId == null || it.remoteId !in incomingRemoteIds }
            .sortedByDescending { it.isActive && !it.isDone }
            .groupBy { matchKey(it.name, it.category) }

        val matchedIds = mutableSetOf<Long>()
        val inserts = mutableListOf<Product>()
        val updates = mutableListOf<Product>()
        var added = 0
        var changed = 0
        var keptCompleted = 0

        payload.items.forEach { item ->
            val match = byRemoteId[item.remoteId]?.takeIf { it.id !in matchedIds }
                ?: byName[matchKey(item.name, item.category)]?.firstOrNull { it.id !in matchedIds }

            if (match == null) {
                inserts += Product(
                    name = item.name,
                    quantity = item.quantity,
                    note = item.note,
                    category = item.category,
                    isActive = true,
                    isDone = false,
                    remoteId = item.remoteId,
                    sharedListId = payload.listId
                )
                added++
                return@forEach
            }

            matchedIds += match.id
            if (match.isDone) {
                keptCompleted++
                return@forEach
            }

            val updated = match.copy(
                name = item.name,
                quantity = item.quantity,
                note = item.note,
                category = item.category,
                isActive = true,
                remoteId = item.remoteId,
                sharedListId = payload.listId
            )
            if (updated != match) updates += updated
            when {
                !match.isActive -> added++
                updated.hasDifferentContent(match) -> changed++
            }
        }

        val removeFromActive = if (removeMissing) {
            local
                .filter { it.sharedListId == payload.listId && it.isActive && !it.isDone && it.id !in matchedIds }
                .map { it.id }
        } else {
            emptyList()
        }

        return MergePlan(inserts, updates, removeFromActive, added, changed, keptCompleted)
    }

    private fun matchKey(name: String, category: String) =
        name.trim().lowercase() to category.trim().lowercase()

    private fun Product.hasDifferentContent(other: Product) =
        name != other.name || quantity != other.quantity || note != other.note || category != other.category
}
