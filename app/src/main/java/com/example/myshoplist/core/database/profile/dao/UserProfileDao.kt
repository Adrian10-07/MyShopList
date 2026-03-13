package com.example.myshoplist.core.database.profile.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myshoplist.core.database.profile.entities.UserProfileEntity

@Dao
interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: UserProfileEntity)

    @Query("SELECT * FROM user_profiles WHERE email = :email")
    suspend fun getProfileByEmail(email: String): UserProfileEntity?
}