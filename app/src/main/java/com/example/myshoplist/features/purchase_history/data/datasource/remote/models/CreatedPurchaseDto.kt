package com.example.myshoplist.features.purchase_history.data.datasource.remote.models

data class CreatePurchaseResponse(
    val success: Boolean,
    val data: CreatedPurchaseDto
)

data class CreatedPurchaseDto(
    val id: String
)
