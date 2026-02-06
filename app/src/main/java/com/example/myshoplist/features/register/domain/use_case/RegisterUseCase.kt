package com.example.myshoplist.features.register.domain.use_case

import com.example.myshoplist.features.login.domain.entities.AuthUser
import com.example.myshoplist.features.register.domain.repository.RegisterRepository

class RegisterUseCase(private val repository: RegisterRepository) {
    suspend operator fun invoke(name: String, email: String, password: String): Result<AuthUser> {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            return Result.failure(IllegalArgumentException("Campos vacíos"))
        }
        if (name.trim().length < 2) {
            return Result.failure(IllegalArgumentException("El nombre debe tener al menos 2 caracteres"))
        }
        return repository.register(name, email, password)
    }
}