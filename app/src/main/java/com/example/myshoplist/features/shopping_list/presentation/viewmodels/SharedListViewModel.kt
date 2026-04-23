package com.example.myshoplist.features.shopping_list.presentation.viewmodels

import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.example.myshoplist.features.shopping_list.domain.use_case.GenerateShareLinkUseCase
import com.example.myshoplist.features.shopping_list.domain.use_case.ObserveSharedListUseCase
import com.example.myshoplist.features.shopping_list.domain.use_case.SaveAndSyncListUseCase
import com.example.myshoplist.features.shopping_list.domain.use_case.ShoppingListUseCase
import com.example.myshoplist.features.shopping_list.domain.repository.RemoteListDataSource
import com.example.myshoplist.features.shopping_list.framework.worker.SyncListWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── Modelo que la UI consume ────────────────────────────────────────────────
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
    val activeListId: String          = "",   // qué lista se está viendo ahora
    val shareLink: String?            = null,
    val error: String?                = null
)

// ─── ViewModel ───────────────────────────────────────────────────────────────
@HiltViewModel
class SharedListViewModel @Inject constructor(
    private val shoppingListUseCase: ShoppingListUseCase,       // Lee Room
    private val remoteDataSource: RemoteListDataSource,         // Escribe/lee Firestore
    private val observeSharedListUseCase: ObserveSharedListUseCase,
    private val generateShareLinkUseCase: GenerateShareLinkUseCase,
    private val workManager: WorkManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SharedListUiState())
    val uiState: StateFlow<SharedListUiState> = _uiState.asStateFlow()

    /**
     * Punto de entrada principal. Se llama desde LaunchedEffect(listId) en la Screen.
     *
     * Flujo:
     * 1. Carga productos de Room
     * 2. Los sube a Firestore (solo si hay productos locales)
     * 3. Empieza a escuchar Firestore en tiempo real
     *
     * Así tanto el dueño como el invitado ven siempre los datos de Firestore,
     * pero el dueño es quien los alimenta desde Room.
     */
    fun initSharedList(listId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, activeListId = listId) }

            // Paso 1: Cargar productos locales de Room
            shoppingListUseCase()
                .onSuccess { localProducts ->
                    if (localProducts.isNotEmpty()) {
                        // Paso 2: Subir a Firestore
                        _uiState.update { it.copy(isSyncing = true) }
                        remoteDataSource.syncProductsToCloud(listId, localProducts)
                            .onFailure { e ->
                                Log.e("SharedListVM", "Error subiendo a Firestore: ${e.message}")
                            }
                        _uiState.update { it.copy(isSyncing = false) }
                    }

                    // Paso 3: Escuchar Firestore en tiempo real (sea dueño o invitado)
                    observeFirestore(listId)
                }
                .onFailure { e ->
                    // Si Room falla, igual intentamos escuchar Firestore
                    // (útil para el invitado que no tiene datos locales)
                    Log.w("SharedListVM", "Room vacío o error: ${e.message}. Escuchando Firestore.")
                    observeFirestore(listId)
                }
        }
    }

    /**
     * Escucha el documento de Firestore y convierte el mapa en lista de SharedProduct.
     *
     * Estructura esperada en Firestore:
     * shared_lists/{listId} {
     *   items: {
     *     "product_id": { name, category, estimatedPrice, isPurchased }
     *   }
     * }
     */
    private fun observeFirestore(listId: String) {
        viewModelScope.launch {
            observeSharedListUseCase(listId)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { data ->
                    val products = parseFirestoreProducts(data)
                    _uiState.update {
                        it.copy(isLoading = false, products = products)
                    }
                }
        }
    }

    /**
     * Convierte el mapa crudo de Firestore en una lista de SharedProduct ordenada.
     */
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

    // ─── Marcar/desmarcar un producto como comprado ──────────────────────────
    /**
     * Actualización optimista: cambia la UI de inmediato y luego actualiza Firestore.
     * Si Firestore falla, revierte el cambio.
     */
    fun toggleProduct(listId: String, productId: String) {
        // Si el usuario pegó un link, usa ese listId activo; si no, el propio
        val activeId = _uiState.value.activeListId.ifBlank { listId }
        val current = _uiState.value.products
        val product = current.find { it.id == productId } ?: return
        val newValue = !product.isPurchased

        // Actualización optimista: cambia la UI sin esperar a Firestore
        _uiState.update { state ->
            state.copy(products = state.products.map {
                if (it.id == productId) it.copy(isPurchased = newValue) else it
            })
        }

        viewModelScope.launch {
            remoteDataSource.toggleProductInCloud(activeId, productId, newValue)
                .onFailure {
                    // Revertir si Firestore falla
                    _uiState.update { state ->
                        state.copy(products = state.products.map {
                            if (it.id == productId) it.copy(isPurchased = !newValue) else it
                        })
                    }
                }
        }
    }

    // ─── Cargar lista desde un link pegado por el usuario ────────────────────
    /**
     * Acepta tanto el link completo como solo el listId:
     *   "myshoplist://shared?listId=abc123"  →  extrae "abc123"
     *   "abc123"                             →  usa directamente
     */
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

        // Cambia la lista activa y empieza a escuchar esa lista en Firestore.
        // No sincroniza Room → Firestore porque el usuario B solo quiere ver, no sobrescribir.
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, activeListId = listId, products = emptyList()) }
            observeFirestore(listId)
        }
    }

    // ─── Generar link para compartir ─────────────────────────────────────────
    fun onShareList(listId: String) {
        viewModelScope.launch {
            generateShareLinkUseCase(listId)
                .onSuccess { link -> _uiState.update { it.copy(shareLink = link) } }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    // ─── Limpiar estados ─────────────────────────────────────────────────────
    fun onErrorShown()     = _uiState.update { it.copy(error = null) }
    fun onShareLinkShown() = _uiState.update { it.copy(shareLink = null) }
}