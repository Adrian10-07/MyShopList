package com.example.myshoplist.features.purchase_history.data.datasource.remote.api

import androidx.room.Query
import com.example.myshoplist.features.purchase_history.data.datasource.remote.models.PurchaseResponseDto
import retrofit2.Response
import retrofit2.http.GET

interface PurchaseApi {
    @GET("api/purchases")
    suspend fun getPurchaseHistory(
        @Query("month") month: String? = null,
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0
    ): Response<PurchaseResponseDto>
}