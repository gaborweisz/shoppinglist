package com.trainig.shoppinglist.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import java.util.UUID

/**
 * Identity of the user's own shared list: a stable list id and a version that grows with every share.
 */
interface ShareSettings {
    suspend fun ownListId(): String
    suspend fun nextVersion(): Int
}

class ShareSettingsImpl(private val context: Context) : ShareSettings {
    private val LIST_ID_KEY = stringPreferencesKey("share_list_id")
    private val VERSION_KEY = intPreferencesKey("share_version")

    override suspend fun ownListId(): String {
        var listId = ""
        context.dataStore.edit { preferences ->
            listId = preferences[LIST_ID_KEY] ?: UUID.randomUUID().toString().also {
                preferences[LIST_ID_KEY] = it
            }
        }
        return listId
    }

    override suspend fun nextVersion(): Int {
        var version = 0
        context.dataStore.edit { preferences ->
            version = (preferences[VERSION_KEY] ?: 0) + 1
            preferences[VERSION_KEY] = version
        }
        return version
    }
}
