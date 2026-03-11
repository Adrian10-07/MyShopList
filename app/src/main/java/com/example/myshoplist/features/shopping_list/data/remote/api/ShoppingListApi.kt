package com.example.myshoplist.features.shopping_list.data.remote.api

import com.example.myshoplist.features.product.data.datasource.remote.model.AddProductResponse
import com.example.myshoplist.features.shopping_list.data.remote.model.ShoppingListRequest
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface ShoppingListApi {
    @GET("products/")
    suspend fun getProducts(): Response<ShoppingListRequest>
    @DELETE("products/{id}")
    suspend fun deleteProduct(@Path("id") id: String): Response<Unit>
    @PATCH("products/{id}/toggle")
    suspend fun updateProduct(@Path("id") id: String): Response<AddProductResponse>
}