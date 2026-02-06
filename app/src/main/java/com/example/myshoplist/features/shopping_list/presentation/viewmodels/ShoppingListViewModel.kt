package com.example.myshoplist.features.shopping_list.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myshoplist.features.shopping_list.domain.use_case.ShoppingListUseCase
import com.example.myshoplist.features.shopping_list.presentation.screens.ShoppingListUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ShoppingListViewModel(
    private val shoppingListUseCase: ShoppingListUseCase
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<ShoppingListUiState>(ShoppingListUiState.Loading)
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        _uiState.value = ShoppingListUiState.Loading

        viewModelScope.launch {
            val result = shoppingListUseCase()

            result
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

    fun refresh() {
        loadProducts()
    }
}
