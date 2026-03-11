package com.example.myshoplist.core.hardware.domain

sealed class BiometricResult {
    data object Success : BiometricResult()
    data object AuthenticationFailed : BiometricResult()
    data object Cancelled : BiometricResult()
    data class Error(val message: String) : BiometricResult()
}