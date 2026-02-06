package com.example.myshoplist.features.register.presentation.screens

import com.example.myshoplist.features.login.domain.entities.AuthUser

data class RegisterUIState(
    val isLoading: Boolean = false,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val error: String? = null,
    val isSuccess: Boolean = false,
    val user: AuthUser? = null
)
