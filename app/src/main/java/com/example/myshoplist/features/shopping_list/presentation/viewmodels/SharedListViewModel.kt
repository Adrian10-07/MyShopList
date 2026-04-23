package com.example.myshoplist.features.shopping_list.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.example.myshoplist.features.shopping_list.domain.use_case.GenerateShareLinkUseCase
import com.example.myshoplist.features.shopping_list.domain.use_case.ObserveSharedListUseCase
import com.example.myshoplist.features.shopping_list.domain.use_case.ShoppingListUseCase
import com.example.myshoplist.features.shopping_list.domain.repository.RemoteListDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SharedProduct(
    val id: String,
    val name: String,
    val category: String,
    val estimatedPrice: Double,
    val isPurchased: Boolean
)

data class SharedListUiState(
    val isLoading: Boolean            = false,
    val isSyncing: Boolean            = false,
    val products: List<SharedProduct> = emptyList(),
    val activeListId: String          = "",
    val shareLink: String?            = null,
    val isFinalized: Boolean          = false,  // para mostrar confirmación
    val error: String?                = null
)

@HiltViewModel
class SharedListViewModel @Inject constructor(
    private val shoppingListUseCase: ShoppingListUseCase,
    private val remoteDataSource: RemoteListDataSource,
    private val observeSharedListUseCase: ObserveSharedListUseCase,
    private val generateShareLinkUseCase: GenerateShareLinkUseCase,
    private val workManager: WorkManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SharedListUiState())
    val uiState: StateFlow<SharedListUiState> = _uiState.asStateFlow()

    fun initSharedList(listId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, activeListId = listId) }

            shoppingListUseCase()
                .onSuccess { localProducts ->
                    if (localProducts.isNotEmpty()) {
                        _uiState.update { it.copy(isSyncing = true) }
                        remoteDataSource.syncProductsToCloud(listId, localProducts)
                            .onFailure { e ->
                                Log.e("SharedListVM", "Error subiendo a Firestore: ${e.message}")
                            }
                        _uiState.update { it.copy(isSyncing = false) }
                    }
                    observeFirestore(listId)
                }
                .onFailure { e ->
                    Log.w("SharedListVM", "Room vacío: ${e.message}. Escuchando Firestore.")
                    observeFirestore(listId)
                }
        }
    }

    private fun observeFirestore(listId: String) {
        viewModelScope.launch {
            observeSharedListUseCase(listId)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { data ->
                    val products = parseFirestoreProducts(data)
                    _uiState.update { it.copy(isLoading = false, products = products) }
                }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseFirestoreProducts(data: Map<String, Any>): List<SharedProduct> {
        val itemsMap = data["items"] as? Map<String, Any> ?: return emptyList()
        return itemsMap.entries.mapNotNull { (id, value) ->
            val item = value as? Map<String, Any> ?: return@mapNotNull null
            SharedProduct(
                id             = id,
                name           = item["name"] as? String ?: "",
                category       = item["category"] as? String ?: "",
                estimatedPrice = (item["estimatedPrice"] as? Number)?.toDouble() ?: 0.0,
                isPurchased    = item["isPurchased"] as? Boolean ?: false
            )
        }.sortedBy { it.name }
    }

    // ─── Marcar/desmarcar producto ────────────────────────────────────────────
    fun toggleProduct(listId: String, productId: String) {
        val activeId = _uiState.value.activeListId.ifBlank { listId }
        val product  = _uiState.value.products.find { it.id == productId } ?: return
        val newValue = !product.isPurchased

        // Optimista
        _uiState.update { state ->
            state.copy(products = state.products.map {
                if (it.id == productId) it.copy(isPurchased = newValue) else it
            })
        }

        viewModelScope.launch {
            remoteDataSource.toggleProductInCloud(activeId, productId, newValue)
                .onFailure {
                    // Revertir
                    _uiState.update { state ->
                        state.copy(products = state.products.map {
                            if (it.id == productId) it.copy(isPurchased = !newValue) else it
                        })
                    }
                }
        }
    }

    // ─── Finalizar compra compartida ──────────────────────────────────────────
    /**
     * Resetea todos los isPurchased a false en Firestore.
     * Ambos usuarios ven la lista limpia en tiempo real.
     */
    fun finalizeSharedPurchase(listId: String) {
        val activeId = _uiState.value.activeListId.ifBlank { listId }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            remoteDataSource.finalizeSharedPurchase(activeId)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, isFinalized = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    // ─── Cargar lista desde link pegado ──────────────────────────────────────
    fun loadFromLink(input: String) {
        val listId = if (input.contains("listId=")) {
            input.substringAfter("listId=").trim()
        } else {
            input.trim()
        }

        if (listId.isBlank()) {
            _uiState.update { it.copy(error = "El link no es válido") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, activeListId = listId, products = emptyList()) }
            observeFirestore(listId)
        }
    }

    // ─── Compartir ────────────────────────────────────────────────────────────
    fun onShareList(listId: String) {
        val activeId = _uiState.value.activeListId.ifBlank { listId }
        viewModelScope.launch {
            generateShareLinkUseCase(activeId)
                .onSuccess { link -> _uiState.update { it.copy(shareLink = link) } }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun onErrorShown()      = _uiState.update { it.copy(error = null) }
    fun onShareLinkShown()  = _uiState.update { it.copy(shareLink = null) }
    fun onFinalizedShown()  = _uiState.update { it.copy(isFinalized = false) }
}