package com.example.myshoplist.features.profile.data.repositories

import com.example.myshoplist.core.database.profile.dao.UserProfileDao
import com.example.myshoplist.core.database.profile.entities.UserProfileEntity
import com.example.myshoplist.features.profile.domain.repositories.ProfileRepository
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val dao: UserProfileDao
) : ProfileRepository {
    override suspend fun getProfile(email: String): UserProfileEntity? {
        return dao.getProfileByEmail(email)
    }

    override suspend fun saveProfile(profile: UserProfileEntity) {
        dao.saveProfile(profile)
    }
}