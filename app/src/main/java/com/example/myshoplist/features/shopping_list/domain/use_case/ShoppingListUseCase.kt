package com.example.myshoplist.features.shopping_list.domain.use_case

import com.example.myshoplist.features.product.domain.entities.Product
import com.example.myshoplist.features.shopping_list.domain.repository.ShoppingListRepository
import javax.inject.Inject

class ShoppingListUseCase @Inject constructor(
    private val repository: ShoppingListRepository
) {
    suspend operator fun invoke(): Result<List<Product>> = repository.getProducts()
}