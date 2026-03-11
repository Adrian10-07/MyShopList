package com.example.myshoplist.features.login.data.repository

import com.example.myshoplist.core.network.AuthApiService
import com.example.myshoplist.core.session.SessionManager
import com.example.myshoplist.features.login.data.datasource.remote.mapper.toDomain
import com.example.myshoplist.features.login.data.datasource.remote.model.LoginRequest
import com.example.myshoplist.features.login.domain.entities.AuthUser
import com.example.myshoplist.features.login.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(private val api: AuthApiService) : AuthRepository {
    override suspend fun login(email: String, password: String): Result<AuthUser> {
        return try {
            val response = api.login(LoginRequest(email, password))

            if (response.isSuccessful) {
                val body = response.body()

                if (body != null && body.success && body.data != null) {
                    SessionManager.authToken = body.data.token
                    
                    val user = body.data.toDomain()
                    Result.success(user)
                } else {
                    Result.failure(Exception(body?.message ?: "Error desconocido del servidor"))
                }
            } else {
                Result.failure(Exception("Error en la petición: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}