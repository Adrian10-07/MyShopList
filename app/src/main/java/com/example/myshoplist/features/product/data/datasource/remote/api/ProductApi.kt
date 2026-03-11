package com.example.myshoplist.features.product.data.datasource.remote.api

import com.example.myshoplist.features.product.data.datasource.remote.model.AddProductRequest
import com.example.myshoplist.features.product.data.datasource.remote.model.AddProductResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ProductApi {
    @POST("products/")
    suspend fun addProduct(@Body request: AddProductRequest): Response<AddProductResponse>
}