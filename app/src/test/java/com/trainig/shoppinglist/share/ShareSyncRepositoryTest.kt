package com.trainig.shoppinglist.share

import com.trainig.shoppinglist.data.DirectTransactionRunner
import com.trainig.shoppinglist.data.FakeProductDao
import com.trainig.shoppinglist.data.FakeShareSettings
import com.trainig.shoppinglist.data.FakeSharedListDao
import com.trainig.shoppinglist.data.Product
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Simulates user A sharing with user B, each with their own database.
 */
class ShareSyncRepositoryTest {
    private val daoA = FakeProductDao()
    private val daoB = FakeProductDao()
    private val userA = ShareSyncRepository(daoA, FakeSharedListDao(), FakeShareSettings("list-A"), DirectTransactionRunner())
    private val userB = ShareSyncRepository(daoB, FakeSharedListDao(), FakeShareSettings("list-B"), DirectTransactionRunner())

    private suspend fun shareFromA(): SharePayload =
        SharePayloadCodec.fromText(userA.createShareLink())!!

    private suspend fun FakeProductDao.find(name: String) =
        getAllProductsSnapshot().single { it.name == name }

    private suspend fun FakeProductDao.activeNames() =
        getAllProductsSnapshot().filter { it.isActive && !it.isDone }.map { it.name }.toSet()

    @Test
    fun `only active open items are shared and get a stable remote id`() = runTest {
        daoA.insertProduct(Product(name = "Milk", isActive = true))
        daoA.insertProduct(Product(name = "Bread", isActive = true, isDone = true))
        daoA.insertProduct(Product(name = "Salt"))

        val first = shareFromA()
        val second = shareFromA()

        assertEquals(listOf("Milk"), first.items.map { it.name })
        assertEquals(first.items[0].remoteId, second.items[0].remoteId)
        assertEquals(first.items[0].remoteId, daoA.find("Milk").remoteId)
        assertTrue(second.version > first.version)
    }

    @Test
    fun `nothing to share returns null link`() = runTest {
        assertEquals(null, userA.createShareLink())
    }

    @Test
    fun `new list is previewed and accepted onto the active list`() = runTest {
        daoA.insertProduct(Product(name = "Milk", quantity = "2", isActive = true))
        daoA.insertProduct(Product(name = "Eggs", isActive = true))
        daoB.insertProduct(Product(name = "milk")) // B's catalog entry, matched by name

        val payload = shareFromA()
        val preview = userB.preview(payload)

        assertTrue(preview is IncomingSharePreview.NewList)
        assertEquals(2, (preview as IncomingSharePreview.NewList).plan.addedCount)
        assertTrue(daoB.activeNames().isEmpty()) // nothing changes before accepting

        userB.accept(payload, replaceActiveList = false)

        assertEquals(setOf("Milk", "Eggs"), daoB.activeNames())
        assertEquals(2, daoB.getAllProductsSnapshot().size) // no duplicate milk
        assertEquals("2", daoB.find("Milk").quantity)
    }

    @Test
    fun `replace clears the current shopping before importing`() = runTest {
        daoA.insertProduct(Product(name = "Milk", isActive = true))
        daoB.insertProduct(Product(name = "Coffee", isActive = true))

        val payload = shareFromA()
        val preview = userB.preview(payload) as IncomingSharePreview.NewList
        assertTrue(preview.hasActiveItems)

        userB.accept(payload, replaceActiveList = true)

        assertEquals(setOf("Milk"), daoB.activeNames())
    }

    @Test
    fun `update refreshes open items and keeps completed ones`() = runTest {
        daoA.insertProduct(Product(name = "Milk", isActive = true))
        daoA.insertProduct(Product(name = "Eggs", isActive = true))
        daoA.insertProduct(Product(name = "Bread", isActive = true))
        daoB.insertProduct(Product(name = "Coffee", isActive = true)) // B's own item
        userB.accept(shareFromA(), replaceActiveList = false)

        // B buys the bread
        daoB.markAsCompleted(daoB.find("Bread").id)

        // A changes milk, removes eggs, keeps bread, adds apples
        daoA.updateProduct(daoA.find("Milk").copy(quantity = "3"))
        daoA.removeFromActiveList(daoA.find("Eggs").id)
        daoA.insertProduct(Product(name = "Apples", isActive = true))

        val update = shareFromA()
        val preview = userB.preview(update)

        assertTrue(preview is IncomingSharePreview.Update)
        val plan = (preview as IncomingSharePreview.Update).plan
        assertEquals(1, plan.addedCount)
        assertEquals(1, plan.changedCount)
        assertEquals(1, plan.removedCount)
        assertEquals(1, plan.keptCompletedCount)

        userB.accept(update, replaceActiveList = false)

        assertEquals(setOf("Milk", "Apples", "Coffee"), daoB.activeNames())
        assertEquals("3", daoB.find("Milk").quantity)
        assertTrue(daoB.find("Bread").isDone)
        assertNotNull(daoB.find("Eggs")) // still in B's catalog, just not on the list
    }

    @Test
    fun `same or older version is reported as already applied`() = runTest {
        daoA.insertProduct(Product(name = "Milk", isActive = true))
        val payload = shareFromA()
        userB.accept(payload, replaceActiveList = false)

        assertEquals(IncomingSharePreview.AlreadyApplied, userB.preview(payload))
    }

    @Test
    fun `own list is recognised`() = runTest {
        daoA.insertProduct(Product(name = "Milk", isActive = true))

        assertEquals(IncomingSharePreview.OwnList, userA.preview(shareFromA()))
    }
}
