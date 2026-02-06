package com.example.myshoplist.features.login.domain.entities


data class AuthUser(
    val id: String,
    val name: String,
    val email: String,
    val token: String
)