package com.example.myshoplist.core.hardware.data

import android.content.Context
import androidx.biometric.BiometricManager as AndroidBiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.myshoplist.core.hardware.domain.BiometricManager
import com.example.myshoplist.core.hardware.domain.BiometricResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BiometricManager {

    override fun isBiometricAvailable(): Boolean {
        val biometricManager = AndroidBiometricManager.from(context)
        return biometricManager.canAuthenticate(
            AndroidBiometricManager.Authenticators.BIOMETRIC_STRONG
        ) == AndroidBiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * [activity] se pasa desde la UI para que [BiometricPrompt]
     * pueda adjuntar su Fragment interno sin crashear.
     * Nunca hagas cast del ApplicationContext a FragmentActivity.
     */
    override fun authenticate(activity: FragmentActivity, onResult: (BiometricResult) -> Unit) {
        val executor = ContextCompat.getMainExecutor(activity)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Iniciar sesión")
            .setSubtitle("Usa tu huella digital para continuar")
            .setNegativeButtonText("Usar contraseña")
            .setAllowedAuthenticators(AndroidBiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    onResult(BiometricResult.Success)
                }

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                        BiometricPrompt.ERROR_CANCELED -> onResult(BiometricResult.Cancelled)
                        else -> onResult(BiometricResult.Error(errString.toString()))
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // No cerramos el prompt — el sistema deja reintentar.
                    // Solo notificamos para que la UI muestre feedback.
                    onResult(BiometricResult.AuthenticationFailed)
                }
            }
        )

        biometricPrompt.authenticate(promptInfo)
    }
}