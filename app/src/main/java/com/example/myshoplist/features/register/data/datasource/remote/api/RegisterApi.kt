package com.example.myshoplist.features.register.data.datasource.remote.api

import com.example.myshoplist.features.login.data.datasource.remote.model.LoginResponse
import com.example.myshoplist.features.register.data.datasource.remote.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface RegisterApi {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>
}