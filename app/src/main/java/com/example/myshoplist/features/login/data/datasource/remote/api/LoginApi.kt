package com.example.myshoplist.features.login.data.datasource.remote.api

import com.example.myshoplist.features.login.data.datasource.remote.model.LoginRequest
import com.example.myshoplist.features.login.data.datasource.remote.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST


interface LoginApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}