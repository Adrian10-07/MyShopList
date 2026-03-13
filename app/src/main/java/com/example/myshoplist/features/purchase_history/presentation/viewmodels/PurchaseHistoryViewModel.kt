package com.tuspaquetes.features.purchase_history.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myshoplist.core.database.PurchaseHistory.dao.PurchaseLocationDao
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
    private val getPurchaseHistoryUseCase: GetPurchaseHistoryUseCase,
    private val localDao: PurchaseLocationDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(PurchaseHistoryState())
    val uiState: StateFlow<PurchaseHistoryState> = _uiState.asStateFlow()

    init {
        loadPurchaseHistory()
    }

    fun loadPurchaseHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // 1. Descargas de la API
            getPurchaseHistoryUseCase().onSuccess { apiPurchases ->
                android.util.Log.d("HistorialDebug", "Compras recibidas de la API: ${apiPurchases.size}")
                val enrichedPurchases = apiPurchases.map { purchase ->
                    // Buscamos localmente si tenemos la ubicación de este ID
                    val localLocation = localDao.getLocationForPurchase(purchase.id)

                    // Creamos un nuevo objeto de dominio que incluya la ubicación (si existe)
                    purchase.copy(
                        latitude = localLocation?.latitude,
                        longitude = localLocation?.longitude
                    )
                }

                _uiState.update { it.copy(purchases = enrichedPurchases, isLoading = false) }

            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}