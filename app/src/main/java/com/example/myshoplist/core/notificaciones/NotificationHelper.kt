package com.example.myshoplist.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.myshoplist.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val manager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createChannel()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Lista Compartida",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avisos sobre tu lista compartida"
            }
            manager.createNotificationChannel(channel)
        }
    }

    fun showSomeoneJoined() {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("¡Alguien se unió a tu lista! 🛒")
            .setContentText("Un usuario está viendo y comprando de tu lista compartida.")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(NOTIF_ID_JOINED, notification)
    }

    companion object {
        const val CHANNEL_ID       = "myshoplist_shared"
        const val NOTIF_ID_JOINED  = 1001
    }
}