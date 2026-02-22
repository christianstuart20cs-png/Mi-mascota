package com.example.mimascota.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mimascota.HistorialMedico
import com.example.mimascota.HistorialMedicoDao
import com.example.mimascota.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialMedicoScreen(
    mascotaId: Int,
    historialDao: HistorialMedicoDao,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val historial by historialDao.getHistorialByMascota(mascotaId).collectAsStateEmpty()
    var fecha by remember { mutableStateOf(currentDate()) }
    var sintoma by remember { mutableStateOf("") }
    var tratamiento by remember { mutableStateOf("") }
    var errores by remember { mutableStateOf<List<String>>(emptyList()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val dateState = rememberDatePickerState()

    fun validar(): Boolean {
        errores = listOfNotNull(
            if (fecha.isBlank()) "Fecha obligatoria." else null,
            if (sintoma.isBlank()) "Sintoma obligatorio." else null,
            if (tratamiento.isBlank()) "Tratamiento obligatorio." else null,
            if (mascotaId <= 0) "Mascota invalida." else null
        )
        return errores.isEmpty()
    }

    fun share() {
        if (historial.isEmpty()) return
        val body = buildString {
            appendLine("HISTORIAL MEDICO")
            appendLine()
            historial.forEach {
                appendLine("Fecha: ${it.fecha}")
                appendLine("Sintoma: ${it.sintoma}")
                appendLine("Tratamiento: ${it.tratamiento}")
                appendLine("-----")
            }
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, body)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir historial"))
    }

    BackgroundColumn(background = R.drawable.planilla_medica) {
        TopAppBar(
            title = { Text("Historial medico", fontWeight = FontWeight.ExtraBold) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } },
            actions = {
                IconButton(onClick = { share() }, enabled = historial.isNotEmpty()) {
                    Icon(Icons.Filled.Share, "Compartir")
                }
            }
        )
        OutlinedTextField(
            value = fecha,
            onValueChange = {},
            label = { Text("Fecha") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Filled.DateRange, "Fecha") }
            }
        )
        OutlinedTextField(sintoma, { sintoma = it }, label = { Text("Sintoma") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            tratamiento,
            { tratamiento = it },
            label = { Text("Tratamiento") },
            modifier = Modifier.fillMaxWidth()
        )
        ErrorList(errors = errores, modifier = Modifier.padding(top = 8.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                if (!validar()) return@Button
                scope.launch {
                    historialDao.insert(
                        HistorialMedico(
                            mascotaId = mascotaId,
                            fecha = fecha,
                            sintoma = sintoma.trim(),
                            tratamiento = tratamiento.trim()
                        )
                    )
                    sintoma = ""
                    tratamiento = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Guardar historial") }
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 12.dp)
        ) {
            items(historial, key = { it.id }) { item ->
                Card(shape = RoundedCornerShape(10.dp)) {
                    androidx.compose.foundation.layout.Column(modifier = Modifier.padding(12.dp)) {
                        Text(item.fecha, fontWeight = FontWeight.Bold)
                        Text("Sintoma: ${item.sintoma}")
                        Text("Tratamiento: ${item.tratamiento}")
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let { fecha = formatDate(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } }
        ) { DatePicker(state = dateState) }
    }
}
