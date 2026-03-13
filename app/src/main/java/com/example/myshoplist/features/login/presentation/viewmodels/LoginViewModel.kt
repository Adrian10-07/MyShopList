package com.example.myshoplist.features.login.presentation.viewmodels

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myshoplist.core.hardware.domain.BiometricManager
import com.example.myshoplist.core.hardware.domain.BiometricResult
import com.example.myshoplist.features.login.domain.use_case.LoginUseCase
import com.example.myshoplist.features.login.presentation.screens.LoginUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val biometric: BiometricManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUIState())
    val uiState = _uiState.asStateFlow()

    // ------------------------------------------------------------------ //
    //  Formulario                                                          //
    // ------------------------------------------------------------------ //

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    // ------------------------------------------------------------------ //
    //  Login con email + contraseña                                        //
    // ------------------------------------------------------------------ //

    fun login() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            loginUseCase(_uiState.value.email, _uiState.value.password).fold(
                onSuccess = { authUser ->
                    _uiState.update {
                        it.copy(isLoading = false, isSuccess = true, user = authUser)
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
            )
        }
    }

    // ------------------------------------------------------------------ //
    //  Login con huella digital                                            //
    // ------------------------------------------------------------------ //

    /** Llama a esta función desde la UI pasando el Activity actual. */
    fun loginWithBiometric(activity: FragmentActivity) {
        if (!biometric.isBiometricAvailable()) {
            _uiState.update {
                it.copy(error = "Huella digital no disponible en este dispositivo.")
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        biometric.authenticate(activity) { result ->
            when (result) {
                BiometricResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }

                BiometricResult.AuthenticationFailed -> {
                    // El prompt sigue abierto — solo mostramos feedback, no cerramos.
                    _uiState.update {
                        it.copy(isLoading = false, error = "Huella no reconocida, intenta de nuevo.")
                    }
                }

                BiometricResult.Cancelled -> {
                    _uiState.update { it.copy(isLoading = false, error = null) }
                }

                is BiometricResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    /** Indica si el dispositivo soporta huella para mostrar/ocultar el botón. */
    fun isBiometricAvailable(): Boolean = biometric.isBiometricAvailable()
}