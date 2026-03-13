package com.example.myshoplist.features.profile.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myshoplist.core.database.profile.entities.UserProfileEntity
import com.example.myshoplist.features.profile.domain.repositories.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow<UserProfileEntity?>(null)
    val profileState: StateFlow<UserProfileEntity?> = _profileState.asStateFlow()

    private val currentUserEmail = "gael@gmail.com"
    private val currentUserName = "Angel Gael"

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            val existingProfile = profileRepository.getProfile(currentUserEmail)
            if (existingProfile != null) {
                _profileState.value = existingProfile
            } else {
                // Si no existe, creamos uno por defecto con los datos del login
                val newProfile = UserProfileEntity(
                    email = currentUserEmail,
                    name = currentUserName,
                    profileImagePath = null
                )
                profileRepository.saveProfile(newProfile)
                _profileState.value = newProfile
            }
        }
    }

    fun updateProfileImage(imagePath: String) {
        viewModelScope.launch {
            val current = _profileState.value
            if (current != null) {
                val updatedProfile = current.copy(profileImagePath = imagePath)
                profileRepository.saveProfile(updatedProfile)
                _profileState.value = updatedProfile
            }
        }
    }
}