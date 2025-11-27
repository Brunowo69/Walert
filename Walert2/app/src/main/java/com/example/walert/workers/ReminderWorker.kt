package com.example.walert.workers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.walert.R
import com.example.walert.datastore.UserPreferences
import kotlinx.coroutines.flow.first

class ReminderWorker(private val context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val userPreferences = UserPreferences(context)
        val alias = userPreferences.userAliasFlow.first()
        val isMuted = userPreferences.notificationsMutedFlow.first()
        val vibrationEnabled = userPreferences.vibrationEnabledFlow.first()

        val notificationTitle = "¡Es hora de hidratarse!"
        val notificationText = if (alias.isNotBlank()) {
            "¡Hey, $alias! Tu cuerpo te pide un vaso de agua."
        } else {
            "¡Hey! Tu cuerpo te pide un vaso de agua."
        }

        // Guardamos la notificación en el historial antes de mostrarla.
        userPreferences.addNotificationToHistory(notificationText)

        createNotificationChannel()

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Asegúrate de que este ícono exista en tus `drawables`.
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        if (isMuted) {
            builder.setSilent(true)
        } else {
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            builder.setSound(defaultSoundUri)

            if (vibrationEnabled) {
                builder.setVibrate(longArrayOf(0, 500, 100, 500))
            }
        }

        with(NotificationManagerCompat.from(applicationContext)) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return Result.failure()
            }
            notify(NOTIFICATION_ID, builder.build())
        }

        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Recordatorios de Hidratación"
            val descriptionText = "Canal para los recordatorios de beber agua."
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null)
            }
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "water_reminder_channel"
        const val NOTIFICATION_ID = 1
    }
}
