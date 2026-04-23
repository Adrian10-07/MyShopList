package com.example.myshoplist.features.login.domain.use_case

import android.util.Log
import com.example.myshoplist.core.database.PurchaseHistory.dao.PurchaseHistoryDao
import com.example.myshoplist.core.database.PurchaseHistory.dao.PurchaseLocationDao
import com.example.myshoplist.core.database.product.dao.ProductDao
import com.example.myshoplist.core.session.SessionManager
import com.example.myshoplist.features.login.domain.entities.AuthUser
import com.example.myshoplist.features.login.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository,
    private val productDao: ProductDao,
    private val purchaseHistoryDao: PurchaseHistoryDao,
    private val purchaseLocationDao: PurchaseLocationDao,
) {

    suspend operator fun invoke(email: String, password: String): Result<AuthUser> {

        if (email.isEmpty() || password.isEmpty()) {
            return Result.failure(IllegalArgumentException("Campos vacíos"))
        }

        val result = repository.login(email, password)

        result.onSuccess { authUser ->
            val previousUserId = SessionManager.userId

            // Si hay un usuario previo diferente → limpiar toda la caché de Room
            // para que los datos del usuario anterior no contaminen la nueva sesión.
            if (previousUserId != null && previousUserId != authUser.id) {
                Log.d("SESSION", "Cambio de usuario: $previousUserId → ${authUser.id}. Limpiando Room.")
                productDao.deleteAllProducts()
                purchaseHistoryDao.deleteAllPurchasesAndItems()
                purchaseLocationDao.deleteAllLocations()
            }

            // Actualizar el userId de la sesión activa
            SessionManager.userId = authUser.id
        }

        return result
    }
}
