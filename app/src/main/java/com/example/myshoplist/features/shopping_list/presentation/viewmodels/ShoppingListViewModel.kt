package com.example.myshoplist.features.shopping_list.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myshoplist.features.shopping_list.data.remote.model.CreatePurchaseRequest
import com.example.myshoplist.features.shopping_list.data.remote.model.PurchaseProductRequest
import com.example.myshoplist.features.shopping_list.domain.use_case.CreatePurchaseUseCase
import com.example.myshoplist.features.shopping_list.domain.use_case.DeleteProductUseCase
import com.example.myshoplist.features.shopping_list.domain.use_case.ShoppingListUseCase
import com.example.myshoplist.features.shopping_list.domain.use_case.UpdateProductUseCase
import com.example.myshoplist.features.shopping_list.presentation.screens.ShoppingListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val shoppingListUseCase: ShoppingListUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    private val createPurchaseUseCase: CreatePurchaseUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ShoppingListUiState>(ShoppingListUiState.Loading)
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = ShoppingListUiState.Loading
            shoppingListUseCase()
                .onSuccess { items ->
                    _uiState.value = if (items.isEmpty()) {
                        ShoppingListUiState.Empty
                    } else {
                        ShoppingListUiState.Success(items)
                    }
                }
                .onFailure {
                    _uiState.value = ShoppingListUiState.Error(
                        it.message ?: "Error desconocido al cargar productos"
                    )
                }
        }
    }

    fun deleteProduct(id: String) {
        viewModelScope.launch {
            deleteProductUseCase(id).onSuccess {
                loadProducts()
            }
        }
    }

    fun updateProduct(id: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is ShoppingListUiState.Success) {
                val optimisticItems = currentState.items.map {
                    if (it.id == id) it.copy(isPurchased = if (it.isPurchased == 1) 0 else 1)
                    else it
                }
                _uiState.value = ShoppingListUiState.Success(optimisticItems)
            }

            //updateProductUseCase(id).onFailure {
                //loadProducts()
            //}
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
                        productId = it.id ?: "",
                        productName = it.name,
                        category = it.category,
                        price = it.estimatedPrice
                    )
                }
            )
            android.util.Log.d("PayloadDebug", "Enviando a Node.js: ${request.products.map { it.productName }}")
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