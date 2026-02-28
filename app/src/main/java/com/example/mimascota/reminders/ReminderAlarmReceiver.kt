package com.christianstuart.mimascota.reminders

import android.Manifest
import android.app.AlarmManager
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
import com.christianstuart.mimascota.MainActivity

class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getIntExtra(ReminderScheduler.EXTRA_REMINDER_ID, 0)
        val description = intent.getStringExtra(ReminderScheduler.EXTRA_REMINDER_DESC).orEmpty()
        when (intent.action) {
            ACTION_DISMISS_ALARM -> {
                if (reminderId != 0) {
                    NotificationManagerCompat.from(context).cancel(reminderId)
                    cancelAutoTimeout(context, reminderId)
                }
            }
            ACTION_AUTO_TIMEOUT_ALARM -> {
                if (reminderId != 0) {
                    showReminderInStatusBar(context, reminderId, description)
                    cancelAutoTimeout(context, reminderId)
                }
            }
            else -> {
                ensureChannels(context)
                showAlarmNotification(context, reminderId, description)
                launchAlarmActivity(context, reminderId, description)
                if (reminderId != 0) {
                    scheduleAutoTimeout(context, reminderId, description)
                }
            }
        }
    }

    private fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val alarmChannel = NotificationChannel(
            ALARM_CHANNEL_ID,
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
        val reminderChannel = NotificationChannel(
            REMINDER_CHANNEL_ID,
            "Recordatorios pendientes",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notificaciones de recordatorios pendientes"
            enableVibration(false)
            setSound(null, null)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PRIVATE
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(alarmChannel)
        manager.createNotificationChannel(reminderChannel)
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

        val notification = NotificationCompat.Builder(context, ALARM_CHANNEL_ID)
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
            .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            .setOnlyAlertOnce(true)
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

    private fun launchAlarmActivity(context: Context, reminderId: Int, description: String) {
        val alarmIntent = Intent(context, ReminderAlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(ReminderScheduler.EXTRA_REMINDER_ID, reminderId)
            putExtra(ReminderScheduler.EXTRA_REMINDER_DESC, description)
        }
        runCatching { context.startActivity(alarmIntent) }
    }

    private fun showReminderInStatusBar(context: Context, reminderId: Int, description: String) {
        ensureChannels(context)
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            reminderId + STATUS_BAR_REQUEST_OFFSET,
            contentIntent,
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
        val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Recordatorio pendiente")
            .setContentText(description.ifBlank { "Tienes un recordatorio pendiente" })
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setOngoing(false)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(0, "Marcar como visto", dismissPendingIntent)
            .setOnlyAlertOnce(true)
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

    private fun scheduleAutoTimeout(context: Context, reminderId: Int, description: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val timeoutIntent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = ACTION_AUTO_TIMEOUT_ALARM
            putExtra(ReminderScheduler.EXTRA_REMINDER_ID, reminderId)
            putExtra(ReminderScheduler.EXTRA_REMINDER_DESC, description)
        }
        val timeoutPendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId + AUTO_TIMEOUT_REQUEST_OFFSET,
            timeoutIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAtMillis = System.currentTimeMillis() + ALARM_DURATION_MS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                timeoutPendingIntent
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerAtMillis, timeoutPendingIntent),
                timeoutPendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                timeoutPendingIntent
            )
        }
    }

    private fun cancelAutoTimeout(context: Context, reminderId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val timeoutIntent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = ACTION_AUTO_TIMEOUT_ALARM
            putExtra(ReminderScheduler.EXTRA_REMINDER_ID, reminderId)
        }
        val timeoutPendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId + AUTO_TIMEOUT_REQUEST_OFFSET,
            timeoutIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(timeoutPendingIntent)
        timeoutPendingIntent.cancel()
    }

    companion object {
        private const val ALARM_CHANNEL_ID = "reminders_alarm_channel_v4"
        private const val REMINDER_CHANNEL_ID = "reminders_pending_channel_v1"
        const val ACTION_DISMISS_ALARM = "com.christianstuart.mimascota.ACTION_DISMISS_ALARM"
        const val ACTION_AUTO_TIMEOUT_ALARM = "com.christianstuart.mimascota.ACTION_AUTO_TIMEOUT_ALARM"
        private const val DISMISS_REQUEST_OFFSET = 100_000
        private const val AUTO_TIMEOUT_REQUEST_OFFSET = 200_000
        private const val STATUS_BAR_REQUEST_OFFSET = 300_000
        private const val ALARM_DURATION_MS = 30_000L
    }
}
