package com.trainig.shoppinglist.share

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Hands text received from outside the app (a tapped link or a shared message) to the ViewModel.
 */
@Singleton
class IncomingShareInbox @Inject constructor() {
    private val _text = MutableStateFlow<String?>(null)
    val text: StateFlow<String?> = _text.asStateFlow()

    fun offer(text: String) {
        _text.value = text
    }

    fun consume() {
        _text.value = null
    }
}
