package com.example.myshoplist.features.login.data.datasource.remote.mapper

import com.example.myshoplist.features.login.data.datasource.remote.model.UserDto
import com.example.myshoplist.features.login.domain.entities.AuthUser

fun UserDto.toDomain(): AuthUser {
    return AuthUser(
        id = this.id,
        name = this.name,
        email = this.email,
        token = this.token
    )
}