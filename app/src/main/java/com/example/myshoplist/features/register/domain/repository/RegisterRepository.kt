package com.example.myshoplist.features.register.domain.repository

import com.example.myshoplist.features.login.domain.entities.AuthUser

interface RegisterRepository {
    suspend fun register(name: String, email: String, password: String): Result<AuthUser>
}