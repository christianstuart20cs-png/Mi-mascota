package com.christianstuart.mimascota.reminders

import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        val description = intent.getStringExtra(ReminderScheduler.EXTRA_REMINDER_DESC).orEmpty()

        startAlarmEffects()

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
        stopAlarmEffects()
        super.onDestroy()
    }

    private fun startAlarmEffects() {
        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        ringtone = RingtoneManager.getRingtone(this, alarmUri)
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
}
