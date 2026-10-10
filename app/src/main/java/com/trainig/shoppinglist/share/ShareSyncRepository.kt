package com.trainig.shoppinglist.share

import com.trainig.shoppinglist.data.ProductDao
import com.trainig.shoppinglist.data.ShareSettings
import com.trainig.shoppinglist.data.SharedList
import com.trainig.shoppinglist.data.SharedListDao
import com.trainig.shoppinglist.data.TransactionRunner
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

sealed interface IncomingSharePreview {
    /** The list was never accepted before. */
    data class NewList(val payload: SharePayload, val plan: MergePlan, val hasActiveItems: Boolean) : IncomingSharePreview

    /** A newer version of an already accepted list. */
    data class Update(val payload: SharePayload, val plan: MergePlan) : IncomingSharePreview

    data object AlreadyApplied : IncomingSharePreview
    data object OwnList : IncomingSharePreview
}

@Singleton
class ShareSyncRepository @Inject constructor(
    private val productDao: ProductDao,
    private val sharedListDao: SharedListDao,
    private val shareSettings: ShareSettings,
    private val transactionRunner: TransactionRunner
) {

    /**
     * Builds the share link for the current shopping list (active, not yet completed items).
     * Every call produces a new version, so the receiver can tell it is an update.
     * Returns null if there is nothing to share.
     */
    suspend fun createShareLink(): String? {
        val items = transactionRunner.run {
            productDao.getAllProductsSnapshot()
                .filter { it.isActive && !it.isDone }
                .map { product ->
                    val remoteId = product.remoteId ?: UUID.randomUUID().toString().also {
                        productDao.setRemoteId(product.id, it)
                    }
                    SharedItem(remoteId, product.name, product.quantity, product.note, product.category)
                }
        }
        if (items.isEmpty()) return null
        val payload = SharePayload(shareSettings.ownListId(), shareSettings.nextVersion(), items)
        return SharePayloadCodec.toLink(payload)
    }

    suspend fun preview(payload: SharePayload): IncomingSharePreview {
        if (payload.listId == shareSettings.ownListId()) return IncomingSharePreview.OwnList

        val appliedVersion = sharedListDao.getAppliedVersion(payload.listId)
        val local = productDao.getAllProductsSnapshot()
        return when {
            appliedVersion == null -> IncomingSharePreview.NewList(
                payload = payload,
                plan = ShareMerge.plan(local, payload, removeMissing = false),
                hasActiveItems = local.any { it.isActive && !it.isDone }
            )
            payload.version <= appliedVersion -> IncomingSharePreview.AlreadyApplied
            else -> IncomingSharePreview.Update(payload, ShareMerge.plan(local, payload, removeMissing = true))
        }
    }

    /**
     * Applies a received list. With [replaceActiveList] the current shopping is cleared first
     * (only offered for a new list). The plan is recomputed inside the transaction so it reflects
     * any changes made while the dialog was open.
     */
    suspend fun accept(payload: SharePayload, replaceActiveList: Boolean): MergePlan = transactionRunner.run {
        val isUpdate = sharedListDao.getAppliedVersion(payload.listId) != null
        if (replaceActiveList) productDao.startNewShopping()

        val plan = ShareMerge.plan(productDao.getAllProductsSnapshot(), payload, removeMissing = isUpdate)
        plan.inserts.forEach { productDao.insertProduct(it) }
        plan.updates.forEach { productDao.updateProduct(it) }
        plan.removeFromActive.forEach { productDao.removeFromActiveList(it) }
        sharedListDao.upsert(SharedList(payload.listId, payload.version))
        plan
    }
}
