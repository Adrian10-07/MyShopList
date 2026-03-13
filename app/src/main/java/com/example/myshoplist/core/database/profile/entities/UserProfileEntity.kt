package com.example.myshoplist.core.database.profile.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val email: String,
    val name: String,
    val profileImagePath: String? = null
)