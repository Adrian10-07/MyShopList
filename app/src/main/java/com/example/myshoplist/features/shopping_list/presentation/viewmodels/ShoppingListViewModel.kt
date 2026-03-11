package com.example.myshoplist.features.shopping_list.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myshoplist.features.shopping_list.domain.use_case.DeleteProductUseCase
import com.example.myshoplist.features.shopping_list.domain.use_case.ShoppingListUseCase
import com.example.myshoplist.features.shopping_list.domain.use_case.UpdateProductUseCase
import com.example.myshoplist.features.shopping_list.presentation.screens.ShoppingListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val shoppingListUseCase: ShoppingListUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val updateProductUseCase: UpdateProductUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ShoppingListUiState>(ShoppingListUiState.Loading)
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        // No siempre es necesario mostrar Loading al refrescar, pero ayuda en la carga inicial
        viewModelScope.launch {
            val result = shoppingListUseCase()

            result
                .onSuccess { items ->
                    _uiState.value = if (items.isEmpty()) {
                        ShoppingListUiState.Empty
                    } else {
                        // Asegúrate de que ShoppingListUiState.Success reciba List<Product>
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
                loadProducts() // Recarga la lista tras eliminar
            }
        }
    }

    fun updateProduct(id: String) {
        viewModelScope.launch {
            val result = updateProductUseCase(id)

            result.onSuccess {

                loadProducts()
            }.onFailure {
            }
        }
    }

    fun refresh() {
        loadProducts()
    }
}
