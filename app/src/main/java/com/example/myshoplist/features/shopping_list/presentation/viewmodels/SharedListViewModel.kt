package com.example.myshoplist.features.shopping_list.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
// import androidx.work.OneTimeWorkRequestBuilder // Descomenta cuando uses tu Worker real
import com.example.myshoplist.features.shopping_list.domain.use_case.GenerateShareLinkUseCase
import com.example.myshoplist.features.shopping_list.domain.use_case.ObserveSharedListUseCase
import com.example.myshoplist.features.shopping_list.domain.use_case.ShoppingListUseCase
import com.example.myshoplist.features.shopping_list.domain.repository.RemoteListDataSource
import com.example.myshoplist.features.shopping_list.domain.repository.UserPreferencesRepository
import com.example.myshoplist.features.shopping_list.domain.use_case.UpdateProductUseCase
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
    val isFinalized: Boolean          = false,
    val error: String?                = null
)

@HiltViewModel
class SharedListViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository, // Inyectamos el repo
    private val updateProductUseCase: UpdateProductUseCase, // Inyectamos el caso de uso para actualizar
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

            // Parseo robusto del estado de compra
            val rawIsPurchased = item["isPurchased"]
            val isPurchased = when (rawIsPurchased) {
                is Boolean -> rawIsPurchased
                is Number -> rawIsPurchased.toInt() == 1
                is String -> rawIsPurchased.toBooleanStrictOrNull() ?: false
                else -> false
            }

            SharedProduct(
                id             = id,
                name           = item["name"] as? String ?: "",
                category       = item["category"] as? String ?: "",
                estimatedPrice = (item["estimatedPrice"] as? Number)?.toDouble() ?: 0.0,
                isPurchased    = isPurchased
            )
        }.sortedBy { it.name }
    }

    // ─── Marcar/desmarcar producto ────────────────────────────────────────────
    fun toggleProduct(listId: String, productId: String) {
        val activeId = _uiState.value.activeListId.ifBlank { listId }
        val product  = _uiState.value.products.find { it.id == productId } ?: return
        val newValue = !product.isPurchased

        _uiState.update { state ->
            state.copy(products = state.products.map {
                if (it.id == productId) it.copy(isPurchased = newValue) else it
            })
        }

        viewModelScope.launch {
            remoteDataSource.toggleProductInCloud(activeId, productId, newValue)
                .onFailure {
                    _uiState.update { state ->
                        state.copy(products = state.products.map {
                            if (it.id == productId) it.copy(isPurchased = !newValue) else it
                        })
                    }
                }
        }
    }

    // ─── Eliminar producto (Individual) ──────────────────────────────────────
    fun deleteProduct(listId: String, productId: String) {
        val activeId = _uiState.value.activeListId.ifBlank { listId }
        val currentProducts = _uiState.value.products

        // Optimista: Eliminarlo de la vista inmediatamente
        _uiState.update { state ->
            state.copy(products = state.products.filter { it.id != productId })
        }

        viewModelScope.launch {
            remoteDataSource.deleteProductInCloud(activeId, productId)
                .onFailure { e ->
                    // Revertir si falla
                    _uiState.update { state ->
                        state.copy(products = currentProducts, error = "No se pudo eliminar: ${e.message}")
                    }
                }
        }
    }

    // ─── Finalizar compra compartida ──────────────────────────────────────────
    fun finalizeSharedPurchase(listId: String) {
        val activeId = _uiState.value.activeListId.ifBlank { listId }

        // ✅ Obtenemos los productos completos que fueron comprados
        val purchasedProducts = _uiState.value.products.filter { it.isPurchased }
        val purchasedIds = purchasedProducts.map { it.id }

        if (purchasedIds.isEmpty()) return // No hay nada que finalizar

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // 1. NUBE: Borramos los comprados de la lista compartida (Firebase)
            remoteDataSource.finalizeSharedPurchase(activeId, purchasedIds)
                .onSuccess {

                    // ✅ 2. LOCAL: Actualizamos el estado en Room
                    // IMPORTANTE: Asegúrate de que este llamado coincida con los parámetros que
                    // espera tu UpdateProductUseCase (por ejemplo, enviarle el ID y el nuevo estado).
                    purchasedProducts.forEach { product ->
                        // Si tu UseCase solo recibe el ID para marcarlo como comprado:
                        // updateProductUseCase.markAsPurchased(product.id)

                        // Si tu UseCase requiere la entidad completa, tendrías que mapearlo, ejemplo:
                        // val productEntity = mapSharedToLocalProduct(product)
                        // updateProductUseCase(productEntity.copy(isPurchased = 1, pendingSync = 1))

                        // ** DEJA AQUÍ TU IMPLEMENTACIÓN EXACTA **
                        // updateProductUseCase(...)
                    }

                    // ✅ 3. API: Disparamos la sincronización hacia Node.js
                    // (Descomenta y ajusta esta línea usando la clase de tu Worker real)
                    /*
                    val syncRequest = OneTimeWorkRequestBuilder<TuProductSyncWorker>().build()
                    workManager.enqueue(syncRequest)
                    */

                    _uiState.update { it.copy(isLoading = false, isFinalized = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

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
            _uiState.update { it.copy(isLoading = true, activeListId = listId) }

            // Guardar en preferencias (para sincronización)
            preferencesRepository.saveActiveSharedListId(listId)

            // Notificar al dueño de la lista que alguien se unió.
            // Escribe lastJoinedAt en Firestore → el dueño lo detecta
            // en su listener en tiempo real y muestra notificación local.
            remoteDataSource.registerJoin(listId)

            observeFirestore(listId)
        }
    }

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