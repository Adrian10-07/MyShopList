package com.example.myshoplist.features.register.data.repository

import com.example.myshoplist.core.network.AuthApiService
import com.example.myshoplist.features.login.data.datasource.remote.mapper.toDomain
import com.example.myshoplist.features.login.domain.entities.AuthUser
import com.example.myshoplist.features.register.data.datasource.remote.model.RegisterRequest
import com.example.myshoplist.features.register.domain.repository.RegisterRepository

class RegisterRepositoryImpl(
    private val api: AuthApiService
) : RegisterRepository {

    override suspend fun register(name: String, email: String, password: String): Result<AuthUser> {
        return try {
            val request = RegisterRequest(name, email, password)
            val response = api.register(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    Result.success(body.data.toDomain())
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