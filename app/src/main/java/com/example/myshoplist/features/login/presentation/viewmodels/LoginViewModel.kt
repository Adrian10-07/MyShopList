package com.example.myshoplist.features.login.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myshoplist.features.login.domain.use_case.LoginUseCase
import com.example.myshoplist.features.login.presentation.screens.LoginUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    // 1. Exponer el estado (StateFlow) para que la UI lo observe de forma segura
    private val _uiState = MutableStateFlow(LoginUIState())
    val uiState = _uiState.asStateFlow()

    // 2. Añadir los eventos que la UI necesita para notificar cambios
    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    // 3. Renombrar la función de login para que coincida con la llamada de la UI
    fun login() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Usamos el estado actual para el login
            val currentState = _uiState.value
            loginUseCase(currentState.email, currentState.password).fold(
                onSuccess = { authUser ->
                    // El token ya se guarda en el repositorio, aquí solo actualizamos la UI
                    _uiState.update { it.copy(isLoading = false, isSuccess = true, user = authUser) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
            )
        }
    }
}
