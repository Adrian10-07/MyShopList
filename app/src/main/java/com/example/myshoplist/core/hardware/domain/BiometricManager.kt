package com.example.myshoplist.core.hardware.domain

import androidx.fragment.app.FragmentActivity

interface BiometricManager {
    fun isBiometricAvailable(): Boolean
    fun authenticate(activity: FragmentActivity, onResult: (BiometricResult) -> Unit)
}