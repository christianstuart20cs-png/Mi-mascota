package com.example.mimascota.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BackgroundColumn(
    @DrawableRes background: Int,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
        )
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .padding(16.dp),
            content = content
        )
    }
}

@Composable
fun <T> Flow<List<T>>.collectAsStateEmpty() = collectAsState(initial = emptyList())

@Composable
fun ErrorList(errors: List<String>, modifier: Modifier = Modifier) {
    if (errors.isEmpty()) return
    androidx.compose.foundation.layout.Column(modifier = modifier) {
        errors.forEach {
            androidx.compose.material3.Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

fun currentDate(): String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

fun currentTime(): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

fun formatDate(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(millis))

fun formatTime(hour: Int, minute: Int): String = "%02d:%02d".format(hour, minute)
