package com.example.myshoplist.features.add_product.domain.use_case

import com.example.myshoplist.features.add_product.domain.entities.Product
import com.example.myshoplist.features.add_product.domain.repository.AddProductRepository

class AddProductUseCase(
    private val repository: AddProductRepository
) {
    companion object {
        val VALID_CATEGORIES = listOf(
            "Frutas y Verduras",
            "Lácteos",
            "Carnes y Pescados",
            "Panadería",
            "Limpieza",
            "Bebidas",
            "Despensa",
            "Congelados",
            "Higiene Personal",
            "Otros"
        )
    }

    suspend operator fun invoke(
        name: String,
        category: String,
        estimatedPrice: Double
    ): Result<Product> {

        // 1. Validar campos requeridos y longitud
        val trimmedName = name.trim()

        if (trimmedName.isEmpty() || category.isBlank()) {
            return Result.failure(IllegalArgumentException("Nombre y categoría son requeridos"))
        }

        if (trimmedName.length < 2) {
            return Result.failure(IllegalArgumentException("El nombre debe tener al menos 2 caracteres"))
        }

        // 2. Validar categoría
        if (!VALID_CATEGORIES.contains(category)) {
            return Result.failure(IllegalArgumentException("Categoría no válida: $category"))
        }

        // 3. Validar precio
        if (estimatedPrice < 0) {
            return Result.failure(IllegalArgumentException("El precio debe ser un número positivo"))
        }

        return repository.addProduct(
            name = trimmedName,
            category = category,
            estimatedPrice = estimatedPrice
        )
    }
}