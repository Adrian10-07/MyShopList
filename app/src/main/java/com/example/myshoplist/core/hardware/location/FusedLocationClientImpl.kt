package com.example.myshoplist.core.hardware.location

import android.annotation.SuppressLint
import android.content.Context
import com.example.myshoplist.core.hardware.location.model.LocationData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class FusedLocationClientImpl @Inject constructor(
    @ApplicationContext private val context: Context // Hilt nos provee el contexto
) : LocationClient {

    // Cliente de Google Play Services
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // Usamos SupressLint porque asumimos que verificaremos permisos en la UI antes de llamar aquí.
    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): LocationData? {
        // Verificación de seguridad extra básica
        if (!context.hasLocationPermission()) return null

        return suspendCancellableCoroutine { continuation ->
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY, // Alta precisión para GPS
                null // Sin token de cancelación simple
            ).addOnSuccessListener { location ->
                if (location != null) {
                    continuation.resume(LocationData(location.latitude, location.longitude))
                } else {
                    continuation.resume(null) // No se pudo obtener ubicación
                }
            }.addOnFailureListener {
                continuation.resume(null) // Error en la petición
            }
        }
    }

    // Función de extensión auxiliar para chequear permisos rápidamente aquí
    private fun Context.hasLocationPermission(): Boolean {
        return androidx.core.content.ContextCompat.checkSelfPermission(
            this, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}