package com.example.myshoplist.features.login.presentation.screens

import com.example.myshoplist.features.login.domain.entities.AuthUser

data class LoginUIState(
    val isLoading: Boolean = false,
    val email: String = "",
    val password: String = "",
    val error: String? = null,
    val isSuccess: Boolean = false,
    val user: AuthUser? = null
)
