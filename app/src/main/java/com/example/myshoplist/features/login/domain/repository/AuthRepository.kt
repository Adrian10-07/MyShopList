package com.example.myshoplist.features.login.domain.repository
import com.example.myshoplist.features.login.domain.entities.AuthUser
interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthUser>
}