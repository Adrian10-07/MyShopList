package com.tuspaquetes.features.purchase_history.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuspaquetes.features.purchase_history.domain.usecases.GetPurchaseHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PurchaseHistoryViewModel @Inject constructor(
    private val getPurchaseHistoryUseCase: GetPurchaseHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PurchaseHistoryState())
    val uiState: StateFlow<PurchaseHistoryState> = _uiState.asStateFlow()

    init {
        loadPurchaseHistory()
    }

    fun loadPurchaseHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = getPurchaseHistoryUseCase()
            result.onSuccess { purchases ->
                _uiState.update { it.copy(purchases = purchases, isLoading = false) }
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}