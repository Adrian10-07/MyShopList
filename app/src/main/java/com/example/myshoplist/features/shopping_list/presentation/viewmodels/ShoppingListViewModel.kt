package com.example.myshoplist.features.shopping_list.presentation.viewmodels

import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myshoplist.features.shopping_list.data.remote.model.CreatePurchaseRequest
import com.example.myshoplist.features.shopping_list.data.remote.model.PurchaseProductRequest
import com.example.myshoplist.features.shopping_list.domain.use_case.CreatePurchaseUseCase
import com.example.myshoplist.features.shopping_list.domain.use_case.DeleteProductUseCase
import com.example.myshoplist.features.shopping_list.domain.use_case.ObserveSharedListUseCase
import com.example.myshoplist.features.shopping_list.domain.use_case.ShoppingListUseCase
import com.example.myshoplist.features.shopping_list.domain.use_case.UpdateProductUseCase
import com.example.myshoplist.features.shopping_list.presentation.screens.ShoppingListUiState
import com.example.myshoplist.core.notifications.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationHelper: NotificationHelper,
    private val shoppingListUseCase: ShoppingListUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    private val createPurchaseUseCase: CreatePurchaseUseCase,
    private val observeSharedListUseCase: ObserveSharedListUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ShoppingListUiState>(ShoppingListUiState.Loading)
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()

    // null  → todavía no hemos recibido el primer snapshot de Firestore.
    // 0L+   → valor baseline ya conocido; cualquier valor mayor es un join nuevo.
    // Usar null en lugar de 0L evita que la condición "!= 0L" bloquee la primera
    // notificación cuando nadie ha entrado nunca (lastJoinedAt ausente del documento).
    private var lastKnownJoinedAt: Long? = null

    init {
        loadProducts()
        // Usar ANDROID_ID directamente — mismo valor que usa SharedListNavGraph.
        // Así siempre escuchamos el documento correcto sin depender de preferencias.
        val listId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        startCloudSync(listId)
    }

    fun loadProducts() {
        viewModelScope.launch {
            shoppingListUseCase()
                .onSuccess { products ->
                    _uiState.value = if (products.isEmpty())
                        ShoppingListUiState.Empty
                    else
                        ShoppingListUiState.Success(products)
                }
                .onFailure { e ->
                    _uiState.value = ShoppingListUiState.Error(e.message ?: "Error desconocido")
                }
        }
    }

    // ─── Escuchar Firestore y sincronizar con Room ────────────────────────────
    /**
     * Corre en background mientras el ViewModel está vivo.
     * Cada vez que Firestore cambia (otro usuario marca/desmarca/finaliza),
     * sincroniza el estado de isPurchased en Room y refresca la UI.
     *
     * distinctUntilChanged evita procesar el mismo snapshot dos veces.
     */
    private fun startCloudSync(listId: String) {
        viewModelScope.launch {
            observeSharedListUseCase(listId)
                .distinctUntilChanged()
                .catch { e -> Log.e("ShoppingListVM", "Error en sync: ${e.message}") }
                .collect { cloudData ->
                    // Detectar si alguien se unió a la lista.
                    val joinedAt = (cloudData["lastJoinedAt"] as? Number)?.toLong() ?: 0L
                    val previous = lastKnownJoinedAt
                    if (previous == null) {
                        // Primer snapshot: solo establecemos el baseline, sin notificar.
                        // Así evitamos mostrar una notificación "fantasma" al abrir la app
                        // si alguien ya se había unido antes.
                        lastKnownJoinedAt = joinedAt
                    } else if (joinedAt > previous) {
                        // Join nuevo ocurrido mientras la app estaba abierta.
                        lastKnownJoinedAt = joinedAt
                        notificationHelper.showSomeoneJoined()
                    }

                    syncCloudToLocal(cloudData)
                }
        }
    }

    /**
     * Compara el estado de Firestore contra Room y aplica los cambios necesarios.
     *
     * Casos:
     * A) Producto está en Firestore con isPurchased=true y en Room es 0 → marcarlo
     * B) Producto está en Firestore con isPurchased=false y en Room es 1 → desmarcarlo
     * C) Producto ya no existe en Firestore (finalizaron la lista) y en Room es 0 → marcarlo
     */
    private suspend fun syncCloudToLocal(cloudData: Map<String, Any>) {
        @Suppress("UNCHECKED_CAST")
        val cloudItems = (cloudData["items"] as? Map<String, Any>)
            ?.mapValues { (_, v) ->
                val item = v as? Map<String, Any>
                parseBool(item?.get("isPurchased"))
            } ?: emptyMap()

        val localProducts = shoppingListUseCase().getOrNull() ?: return

        var needsRefresh = false

        localProducts.forEach { localProduct ->
            val productId = localProduct.id ?: return@forEach
            val isLocalPurchased = localProduct.isPurchased == 1

            val isCloudPurchased: Boolean? = cloudItems[productId]

            when {
                // Caso A y B: existe en Firestore pero el estado difiere
                isCloudPurchased != null && isCloudPurchased != isLocalPurchased -> {
                    Log.d("ShoppingListVM", "Sincronizando $productId: cloud=$isCloudPurchased local=$isLocalPurchased")
                    updateProductUseCase(productId)
                    needsRefresh = true
                }
                // Caso C: producto borrado de Firestore (lista finalizada) y localmente pendiente
                isCloudPurchased == null && !isLocalPurchased && cloudItems.isNotEmpty().not() -> {
                    // cloudItems vacío = lista fue finalizada
                    Log.d("ShoppingListVM", "Lista finalizada, marcando $productId como comprado")
                    updateProductUseCase(productId)
                    needsRefresh = true
                }
            }
        }

        if (needsRefresh) {
            loadProducts()
        }
    }

    private fun parseBool(value: Any?): Boolean = when (value) {
        is Boolean -> value
        is Long    -> value != 0L
        is Int     -> value != 0
        is String  -> value.toBooleanStrictOrNull() ?: false
        else       -> false
    }

    // ─── Operaciones locales ──────────────────────────────────────────────────

    fun deleteProduct(id: String) {
        viewModelScope.launch {
            deleteProductUseCase(id)
                .onSuccess { loadProducts() }
                .onFailure { e ->
                    _uiState.value = ShoppingListUiState.Error(e.message ?: "Error al eliminar")
                }
        }
    }

    fun updateProduct(id: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is ShoppingListUiState.Success) {
                // Actualización optimista en la UI
                val optimisticItems = currentState.items.map {
                    if (it.id == id) it.copy(isPurchased = if (it.isPurchased == 1) 0 else 1)
                    else it
                }
                _uiState.value = ShoppingListUiState.Success(optimisticItems)
            }
            // Escribir en Room
            updateProductUseCase(id)
        }
    }

    fun finalizePurchase() {
        val currentState = _uiState.value
        if (currentState !is ShoppingListUiState.Success) return

        val purchasedItems = currentState.items.filter { it.isPurchased == 1 }
        if (purchasedItems.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = ShoppingListUiState.Loading

            val total = purchasedItems.sumOf { it.estimatedPrice }
            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val request = CreatePurchaseRequest(
                totalAmount = total,
                purchaseDate = todayDate,
                products = purchasedItems.map {
                    PurchaseProductRequest(
                        productId   = it.id ?: "",
                        productName = it.name,
                        category    = it.category,
                        price       = it.estimatedPrice
                    )
                }
            )
            Log.d("PayloadDebug", "Enviando a Node.js: ${request.products.map { it.productName }}")
            createPurchaseUseCase(request)
                .onSuccess { loadProducts() }
                .onFailure { e ->
                    _uiState.value = ShoppingListUiState.Error(
                        e.message ?: "Error al finalizar compra"
                    )
                }
        }
    }

    fun refresh() = loadProducts()
}