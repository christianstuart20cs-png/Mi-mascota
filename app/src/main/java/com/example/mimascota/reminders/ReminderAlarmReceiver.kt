package com.christianstuart.mimascota.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.christianstuart.mimascota.R

class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_DISMISS_ALARM) {
            val reminderId = intent.getIntExtra(ReminderScheduler.EXTRA_REMINDER_ID, 0)
            if (reminderId != 0) {
                NotificationManagerCompat.from(context).cancel(reminderId)
            }
            return
        }

        val reminderId = intent.getIntExtra(ReminderScheduler.EXTRA_REMINDER_ID, 0)
        val description = intent.getStringExtra(ReminderScheduler.EXTRA_REMINDER_DESC).orEmpty()
        ensureChannel(context)
        showAlarmNotification(context, reminderId, description)
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Recordatorios",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alarmas de recordatorios de mascotas"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 400, 250, 400)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun showAlarmNotification(context: Context, reminderId: Int, description: String) {
        val alarmIntent = Intent(context, ReminderAlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(ReminderScheduler.EXTRA_REMINDER_ID, reminderId)
            putExtra(ReminderScheduler.EXTRA_REMINDER_DESC, description)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            reminderId,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val dismissIntent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = ACTION_DISMISS_ALARM
            putExtra(ReminderScheduler.EXTRA_REMINDER_ID, reminderId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId + DISMISS_REQUEST_OFFSET,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Alarma de recordatorio")
            .setContentText(description.ifBlank { "Tienes un recordatorio pendiente" })
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(fullScreenPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(0, "Detener", dismissPendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
            .build()

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        NotificationManagerCompat.from(context).notify(reminderId, notification)
    }

    companion object {
        private const val CHANNEL_ID = "reminders_alarm_channel"
        private const val ACTION_DISMISS_ALARM = "com.christianstuart.mimascota.ACTION_DISMISS_ALARM"
        private const val DISMISS_REQUEST_OFFSET = 100_000
    }
}
