package com.example.myshoplist.features.shopping_list.domain.use_case

import com.example.myshoplist.features.shopping_list.data.remote.model.CreatePurchaseRequest
import com.example.myshoplist.features.shopping_list.domain.repository.ShoppingListRepository
import javax.inject.Inject

class CreatePurchaseUseCase @Inject constructor(
    private val repository: ShoppingListRepository
) {
    suspend operator fun invoke(request: CreatePurchaseRequest): Result<Unit> {
        return try {
            if (request.products.isEmpty()) {
                return Result.failure(Exception("No hay productos seleccionados para la compra"))
            }

            if (request.totalAmount <= 0) {
                return Result.failure(Exception("El monto total debe ser mayor a cero"))
            }

            repository.createPurchase(request)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}