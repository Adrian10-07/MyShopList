package com.example.myshoplist.features.shopping_list.domain.use_case

import com.example.myshoplist.features.add_product.domain.entities.Product
import com.example.myshoplist.features.add_product.domain.repository.ProductRepository
import com.example.myshoplist.features.shopping_list.data.remote.mapper.toDomain
import com.example.myshoplist.features.shopping_list.data.remote.model.ShoppingListDto
import com.example.myshoplist.features.shopping_list.domain.repository.ShoppingListRepository

class ShoppingListUseCase(
    private val repository: ShoppingListRepository
) {

    suspend operator fun invoke(): Result<List<Product>> {
        return repository.getProducts()
            .map { dtoList ->
                dtoList.toDomain()
            }
    }
}
