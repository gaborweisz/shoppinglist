package com.trainig.shoppinglist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trainig.shoppinglist.data.Product
import com.trainig.shoppinglist.data.ThemePreferences
import com.trainig.shoppinglist.domain.ShoppingListRepository
import com.trainig.shoppinglist.share.IncomingShareInbox
import com.trainig.shoppinglist.share.IncomingSharePreview
import com.trainig.shoppinglist.share.SharePayloadCodec
import com.trainig.shoppinglist.share.ShareSyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val repository: ShoppingListRepository,
    private val themePreferences: ThemePreferences,
    private val shareSyncRepository: ShareSyncRepository,
    private val incomingShareInbox: IncomingShareInbox
) : ViewModel() {

    private val _filter = MutableStateFlow(ProductFilter.ALL)
    val filter: StateFlow<ProductFilter> = _filter

    // Dark mode state
    val isDarkMode: StateFlow<Boolean?> = themePreferences.isDarkMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Language state
    val language: StateFlow<String?> = themePreferences.language
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val products = combine(
        _filter,
        repository.getAllProducts(),
        repository.getActiveProducts(),
        repository.getCompletedProducts()
    ) { filter, all, active, completed ->
        when (filter) {
            ProductFilter.ALL -> all
            ProductFilter.ACTIVE -> active
            ProductFilter.COMPLETED -> completed
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val categories = repository.getDistinctCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeProductsCount = repository.getActiveProducts()
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private val _uiState = MutableStateFlow<UiState>(UiState.Success)
    val uiState: StateFlow<UiState> = _uiState

    // Received list waiting for the user to accept or decline (NewList or Update)
    private val _incomingShare = MutableStateFlow<IncomingSharePreview?>(null)
    val incomingShare: StateFlow<IncomingSharePreview?> = _incomingShare

    private val _shareMessage = MutableStateFlow<ShareMessage?>(null)
    val shareMessage: StateFlow<ShareMessage?> = _shareMessage

    private val _shareRequest = MutableStateFlow<ShareRequest?>(null)
    val shareRequest: StateFlow<ShareRequest?> = _shareRequest

    init {
        viewModelScope.launch {
            incomingShareInbox.text.filterNotNull().collect { text ->
                incomingShareInbox.consume()
                handleIncomingText(text)
            }
        }
    }

    fun setFilter(filter: ProductFilter) {
        _filter.value = filter
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            val currentMode = isDarkMode.value ?: false
            themePreferences.setDarkMode(!currentMode)
        }
    }

    fun setLanguage(languageCode: String) {
        viewModelScope.launch {
            themePreferences.setLanguage(languageCode)
        }
    }

    fun addProduct(name: String, quantity: String? = null, note: String? = null, category: String = "") {
        if (name.isBlank()) {
            _uiState.value = UiState.Error("Product name cannot be empty")
            return
        }

        viewModelScope.launch {
            repository.addProduct(name, quantity, note, category)
                .onFailure { _uiState.value = UiState.Error(it.message ?: "Failed to add product") }
        }
    }

    fun updateProduct(product: Product) {
        if (product.name.isBlank()) {
            _uiState.value = UiState.Error("Product name cannot be empty")
            return
        }

        viewModelScope.launch {
            repository.updateProduct(product)
                .onFailure { _uiState.value = UiState.Error(it.message ?: "Failed to update product") }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
                .onFailure { _uiState.value = UiState.Error(it.message ?: "Failed to delete product") }
        }
    }

    fun toggleProductDone(productId: Long) {
        viewModelScope.launch {
            repository.toggleProductDone(productId)
                .onFailure { _uiState.value = UiState.Error(it.message ?: "Failed to update product") }
        }
    }

    fun updateCategory(oldCategory: String, newCategory: String) {
        viewModelScope.launch {
            repository.updateCategory(oldCategory, newCategory)
                .onFailure { _uiState.value = UiState.Error(it.message ?: "Failed to update category") }
        }
    }

    fun startNewShopping() {
        viewModelScope.launch {
            repository.startNewShopping()
                .onFailure { _uiState.value = UiState.Error(it.message ?: "Failed to start new shopping") }
        }
    }

    fun addToActiveList(productId: Long) {
        viewModelScope.launch {
            repository.addToActiveList(productId)
                .onFailure { _uiState.value = UiState.Error(it.message ?: "Failed to add to active list") }
        }
    }

    fun removeFromActiveList(productId: Long) {
        viewModelScope.launch {
            repository.removeFromActiveList(productId)
                .onFailure { _uiState.value = UiState.Error(it.message ?: "Failed to remove from active list") }
        }
    }

    fun markAsCompleted(productId: Long) {
        viewModelScope.launch {
            repository.markAsCompleted(productId)
                .onFailure { _uiState.value = UiState.Error(it.message ?: "Failed to mark as completed") }
        }
    }

    fun moveBackToActive(productId: Long) {
        viewModelScope.launch {
            repository.moveBackToActive(productId)
                .onFailure { _uiState.value = UiState.Error(it.message ?: "Failed to move back to active") }
        }
    }

    fun clearError() {
        _uiState.value = UiState.Success
    }

    // --- Sharing ---

    /** Starts preparing a share; the screen sends it once [shareRequest] is set. */
    fun shareList() {
        viewModelScope.launch {
            runCatching { shareSyncRepository.createShareLink() }
                .onSuccess { _shareRequest.value = ShareRequest(it) }
                .onFailure { _uiState.value = UiState.Error(it.message ?: "Failed to share list") }
        }
    }

    fun onShareRequestHandled() {
        _shareRequest.value = null
    }

    /** Looks for a shared list link in [text] and asks the user to accept it. */
    fun handleIncomingText(text: String?) {
        val payload = SharePayloadCodec.fromText(text)
        if (payload == null) {
            _shareMessage.value = ShareMessage.INVALID
            return
        }
        viewModelScope.launch {
            runCatching { shareSyncRepository.preview(payload) }
                .onSuccess { preview ->
                    when (preview) {
                        is IncomingSharePreview.NewList, is IncomingSharePreview.Update -> _incomingShare.value = preview
                        IncomingSharePreview.AlreadyApplied -> _shareMessage.value = ShareMessage.ALREADY_APPLIED
                        IncomingSharePreview.OwnList -> _shareMessage.value = ShareMessage.OWN_LIST
                    }
                }
                .onFailure { _uiState.value = UiState.Error(it.message ?: "Failed to read shared list") }
        }
    }

    fun acceptIncomingShare(replaceActiveList: Boolean = false) {
        val payload = when (val preview = _incomingShare.value) {
            is IncomingSharePreview.NewList -> preview.payload
            is IncomingSharePreview.Update -> preview.payload
            else -> return
        }
        _incomingShare.value = null
        viewModelScope.launch {
            runCatching { shareSyncRepository.accept(payload, replaceActiveList) }
                .onSuccess {
                    _filter.value = ProductFilter.ACTIVE
                    _shareMessage.value = ShareMessage.ACCEPTED
                }
                .onFailure { _uiState.value = UiState.Error(it.message ?: "Failed to accept shared list") }
        }
    }

    fun declineIncomingShare() {
        _incomingShare.value = null
    }

    fun clearShareMessage() {
        _shareMessage.value = null
    }

    fun formatShoppingListForSharing(products: List<Product>): String {
        if (products.isEmpty()) {
            return "My Shopping List is empty"
        }

        val groupedProducts = products.groupBy { it.category.ifBlank { "Uncategorized" } }
        val stringBuilder = StringBuilder()
        stringBuilder.append("🛒 My Shopping List\n")
        stringBuilder.append("━━━━━━━━━━━━━━━━━━━━\n\n")

        groupedProducts.forEach { (category, categoryProducts) ->
            stringBuilder.append("📁 $category\n")
            stringBuilder.append("─────────────────────\n")

            categoryProducts.forEachIndexed { index, product ->
                val checkbox = if (product.isDone) "☑" else "☐"
                stringBuilder.append("$checkbox ${product.name}")

                if (!product.quantity.isNullOrBlank()) {
                    stringBuilder.append(" - ${product.quantity}")
                }

                if (!product.note.isNullOrBlank()) {
                    stringBuilder.append("\n   Note: ${product.note}")
                }

                stringBuilder.append("\n")
            }
            stringBuilder.append("\n")
        }

        return stringBuilder.toString()
    }
}

enum class ProductFilter {
    ALL, ACTIVE, COMPLETED
}

/** Outgoing share; [link] is null when the active list is empty and only the text is sent. */
data class ShareRequest(val link: String?)

enum class ShareMessage {
    ACCEPTED, INVALID, ALREADY_APPLIED, OWN_LIST
}

sealed interface UiState {
    data object Success : UiState
    data class Error(val message: String) : UiState
}
