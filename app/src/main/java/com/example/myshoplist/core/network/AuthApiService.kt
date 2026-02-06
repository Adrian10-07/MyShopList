package com.example.myshoplist.core.network

import com.example.myshoplist.features.add_product.data.datasource.remote.model.AddProductRequest
import com.example.myshoplist.features.add_product.data.datasource.remote.model.AddProductResponse
import com.example.myshoplist.features.login.data.datasource.remote.model.LoginResponse
import com.example.myshoplist.features.login.data.datasource.remote.model.LoginRequest
import com.example.myshoplist.features.register.data.datasource.remote.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.*
interface AuthApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>

    //PRODUCTS
    @POST("products/")
    suspend fun addProduct(
        @Body request: AddProductRequest
    ): Response<AddProductResponse>

}