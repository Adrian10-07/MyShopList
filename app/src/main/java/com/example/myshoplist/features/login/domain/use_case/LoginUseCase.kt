package com.example.myshoplist.features.login.domain.use_case

import com.example.myshoplist.features.login.domain.entities.AuthUser
import com.example.myshoplist.features.login.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(private val repository: AuthRepository) {

    suspend operator fun invoke(email: String, password: String): Result<AuthUser> {

        if (email.isEmpty() || password.isEmpty()) {
            return Result.failure(IllegalArgumentException("Campos vacíos"))
        }
        return repository.login(email, password)
    }
}