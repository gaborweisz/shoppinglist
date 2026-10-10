package com.trainig.shoppinglist.data

class FakeSharedListDao : SharedListDao {
    val lists = mutableMapOf<String, Int>()

    override suspend fun getAppliedVersion(listId: String): Int? = lists[listId]

    override suspend fun upsert(sharedList: SharedList) {
        lists[sharedList.listId] = sharedList.appliedVersion
    }
}

class FakeShareSettings(private val listId: String = "own-list") : ShareSettings {
    var version = 0

    override suspend fun ownListId(): String = listId

    override suspend fun nextVersion(): Int = ++version
}

class DirectTransactionRunner : TransactionRunner {
    override suspend fun <R> run(block: suspend () -> R): R = block()
}
