package com.example.myshoplist.features.shopping_list.framework.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.myshoplist.R
import com.example.myshoplist.features.shopping_list.domain.use_case.SaveFcmTokenUseCase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Servicio FCM que maneja:
 *  1. Renovación del token del dispositivo → lo guarda en Firestore.
 *  2. Mensajes recibidos en primer plano → muestra una notificación local.
 *
 * Cumple el Requisito 10: Push Notifications mediante Firebase MaaS.
 *
 * IMPORTANTE: Declarar en AndroidManifest.xml dentro de <application>:
 *
 *   <service
 *       android:name=".features.shopping_list.framework.fcm.MyShopListMessagingService"
 *       android:exported="false">
 *       <intent-filter>
 *           <action android:name="com.google.firebase.MESSAGING_EVENT" />
 *       </intent-filter>
 *   </service>
 */
@AndroidEntryPoint
class MyShopListMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var saveFcmTokenUseCase: SaveFcmTokenUseCase

    // ─── 1. Nuevo token asignado por Firebase ────────────────────────────────
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Nuevo token generado: $token")

        // Guarda el token en Firestore para poder enviarle notificaciones a este dispositivo.
        // Cuando integres autenticación, reemplaza USER_PLACEHOLDER por el UID real del usuario.
        CoroutineScope(Dispatchers.IO).launch {
            saveFcmTokenUseCase(
                userId = "USER_PLACEHOLDER", // TODO: reemplazar con FirebaseAuth.uid
                token  = token
            )
        }
    }

    // ─── 2. Mensaje recibido con la app en PRIMER PLANO ──────────────────────
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Mensaje recibido de: ${message.from}")

        // Caso A: El mensaje trae un payload de notificación (título + cuerpo)
        message.notification?.let { notification ->
            showLocalNotification(
                title  = notification.title ?: "MyShopList",
                body   = notification.body  ?: "Tu lista fue actualizada",
                listId = message.data["listId"]
            )
        }

        // Caso B: El mensaje trae solo datos (data payload), sin notificación visual
        // Útil para actualizaciones silenciosas que deben refrescar la UI.
        if (message.data.isNotEmpty() && message.notification == null) {
            val listId = message.data["listId"]
            Log.d(TAG, "Data payload recibido para listId=$listId")
            // Aquí podrías lanzar un BroadcastReceiver o enqueue un Worker para
            // refrescar los datos de Room desde Firestore.
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Muestra una notificación local cuando la app está en primer plano.
     * Android >= 8.0 requiere que el NotificationChannel exista antes de notificar.
     */
    private fun showLocalNotification(title: String, body: String, listId: String?) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Crear canal (solo necesario en Android 8+, en versiones anteriores se ignora)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Actualizaciones de Lista",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones cuando tu lista compartida cambia"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent que abre la lista compartida al tocar la notificación
        val intent = Intent(this, Class.forName("com.example.myshoplist.MainActivity")).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            listId?.let { putExtra("navigateTo", "shared_list/$it") }
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Cambia por tu ícono de notificación
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        private const val TAG        = "FCM_SERVICE"
        private const val CHANNEL_ID = "myshoplist_updates"
    }
}