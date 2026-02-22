package com.example.mimascota.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.mimascota.MainActivity

object ReminderScheduler {
    const val EXTRA_REMINDER_ID = "extra_reminder_id"
    const val EXTRA_REMINDER_DESC = "extra_reminder_desc"

    fun scheduleReminder(
        context: Context,
        reminderId: Int,
        description: String,
        triggerAtMillis: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = buildAlarmPendingIntent(context, reminderId, description)
        val openAppIntent = PendingIntent.getActivity(
            context,
            reminderId,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val alarmInfo = AlarmManager.AlarmClockInfo(triggerAtMillis, openAppIntent)
            alarmManager.setAlarmClock(alarmInfo, pendingIntent)
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    private fun buildAlarmPendingIntent(
        context: Context,
        reminderId: Int,
        description: String
    ): PendingIntent {
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_REMINDER_DESC, description)
        }
        return PendingIntent.getBroadcast(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
