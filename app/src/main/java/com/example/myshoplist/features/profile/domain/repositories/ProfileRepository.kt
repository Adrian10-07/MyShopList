package com.example.myshoplist.features.profile.domain.repositories

import com.example.myshoplist.core.database.profile.entities.UserProfileEntity

interface ProfileRepository {
    suspend fun getProfile(email: String): UserProfileEntity?
    suspend fun saveProfile(profile: UserProfileEntity)
}