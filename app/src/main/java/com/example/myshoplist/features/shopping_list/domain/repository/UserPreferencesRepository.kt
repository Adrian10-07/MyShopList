package com.example.myshoplist.features.shopping_list.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    suspend fun saveActiveSharedListId(listId: String)
    fun getActiveSharedListId(): Flow<String?>
    suspend fun clearSharedListId()
}