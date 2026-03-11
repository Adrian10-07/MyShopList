package com.example.myshoplist.features.product.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myshoplist.features.product.domain.use_case.AddProductUseCase
import com.example.myshoplist.features.product.presentation.screens.AddProductUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddProductViewModel @Inject constructor(
    private val addProductUseCase: AddProductUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddProductUiState>(AddProductUiState.Idle)
    val uiState: StateFlow<AddProductUiState> = _uiState.asStateFlow()

    fun addProduct(name: String, category: String, estimatedPrice: String) {
        viewModelScope.launch {
            // 1. Validaciones de UI iniciales
            if (name.isBlank()) {
                _uiState.value = AddProductUiState.Error("El nombre es requerido")
                return@launch
            }

            if (category.isBlank()) {
                _uiState.value = AddProductUiState.Error("La categoría es requerida")
                return@launch
            }

            val price = estimatedPrice.toDoubleOrNull()
            if (price == null || price < 0) {
                _uiState.value = AddProductUiState.Error("Ingresa un precio válido")
                return@launch
            }

            // 2. Estado de carga
            _uiState.value = AddProductUiState.Loading

            // 3. Llamada al Use Case
            val result = addProductUseCase(
                name = name.trim(),
                category = category,
                estimatedPrice = price
            )

            // 4. Manejo de resultado
            result.onSuccess { product ->
                _uiState.value = AddProductUiState.Success(product)
            }.onFailure { error ->
                _uiState.value = AddProductUiState.Error(
                    error.message ?: "Error desconocido al agregar producto"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = AddProductUiState.Idle
    }
}