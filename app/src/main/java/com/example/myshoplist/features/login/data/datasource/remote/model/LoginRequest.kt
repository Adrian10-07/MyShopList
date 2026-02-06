package com.example.myshoplist.features.login.data.datasource.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: UserDto
)

@Serializable
data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val token: String
)