package com.example.myshoplist.features.shopping_list.domain.use_case

import com.example.myshoplist.features.shopping_list.domain.repository.ShoppingListRepository
import javax.inject.Inject

class UpdateProductUseCase @Inject constructor(
    private val repository: ShoppingListRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> = repository.updateProduct(id)
}