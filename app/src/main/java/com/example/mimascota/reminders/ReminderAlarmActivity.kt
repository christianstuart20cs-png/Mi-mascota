package com.christianstuart.mimascota.reminders

import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationManagerCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.christianstuart.mimascota.ui.theme.MiMascotaTheme

class ReminderAlarmActivity : ComponentActivity() {
    private var ringtone: Ringtone? = null
    private var reminderId: Int = 0
    private val alarmHandler = Handler(Looper.getMainLooper())
    private val autoStopAlarmRunnable = Runnable {
        stopAlarmEffects()
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        val description = intent.getStringExtra(ReminderScheduler.EXTRA_REMINDER_DESC).orEmpty()
        reminderId = intent.getIntExtra(ReminderScheduler.EXTRA_REMINDER_ID, 0)

        startAlarmEffects()
        alarmHandler.postDelayed(autoStopAlarmRunnable, ALARM_DURATION_MS)

        setContent {
            MiMascotaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF1A222B))
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Recordatorio",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = description.ifBlank { "Tienes una tarea pendiente para tu mascota." },
                            modifier = Modifier.padding(top = 12.dp, bottom = 24.dp),
                            color = Color.White
                        )
                        val stopAndClose = remember {
                            {
                                stopAlarmEffects()
                                alarmHandler.removeCallbacks(autoStopAlarmRunnable)
                                if (reminderId != 0) {
                                    NotificationManagerCompat.from(this@ReminderAlarmActivity).cancel(reminderId)
                                }
                                finish()
                            }
                        }
                        Button(onClick = stopAndClose) {
                            Text("Cerrar alarma")
                        }
                    }
                }
                DisposableEffect(Unit) {
                    onDispose { stopAlarmEffects() }
                }
            }
        }
    }

    override fun onDestroy() {
        alarmHandler.removeCallbacks(autoStopAlarmRunnable)
        stopAlarmEffects()
        super.onDestroy()
    }

    private fun startAlarmEffects() {
        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        ringtone = RingtoneManager.getRingtone(this, alarmUri)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ringtone?.isLooping = true
        }
        ringtone?.play()

        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(1200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(1200)
        }
    }

    private fun stopAlarmEffects() {
        ringtone?.stop()
        ringtone = null
    }

    companion object {
        private const val ALARM_DURATION_MS = 30_000L
    }
}
