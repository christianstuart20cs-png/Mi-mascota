package com.example.mimascota.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mimascota.HistorialMedico
import com.example.mimascota.HistorialMedicoDao
import com.example.mimascota.R
import com.example.mimascota.Recordatorio
import com.example.mimascota.RecordatorioDao
import com.example.mimascota.reminders.ReminderScheduler
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordatoriosMascotaScreen(
    mascotaId: Int,
    recordatorioDao: RecordatorioDao,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (Int) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val recordatorios by recordatorioDao.getRecordatoriosByMascota(mascotaId).collectAsStateEmpty()
    var toDelete by remember { mutableStateOf<Recordatorio?>(null) }

    BackgroundColumn(background = R.drawable.planilla_medica) {
        TopAppBar(
            title = { Text("Recordatorios", fontWeight = FontWeight.ExtraBold) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } }
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 12.dp)
        ) {
            items(recordatorios, key = { it.id }) { item ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.58f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
                            Text("${item.fecha} - ${item.hora}", fontWeight = FontWeight.Bold, color = Color.White)
                            Text(item.descripcion, color = Color.White)
                        }
                        IconButton(onClick = { onEdit(item.id) }) {
                            Icon(Icons.Filled.Edit, "Editar", tint = Color.White)
                        }
                        IconButton(onClick = { toDelete = item }) {
                            Icon(Icons.Filled.Delete, "Eliminar", tint = Color.White)
                        }
                    }
                }
            }
        }
        Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) { Text("Agregar recordatorio") }
    }

    toDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Eliminar recordatorio") },
            text = { Text("Esta accion no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        ReminderScheduler.cancelReminder(context, item.id)
                        recordatorioDao.delete(item)
                        toDelete = null
                    }
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancelar") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarRecordatorioScreen(
    mascotaId: Int,
    recordatorioDao: RecordatorioDao,
    historialDao: HistorialMedicoDao,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var descripcion by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf(currentDate()) }
    var hora by remember { mutableStateOf(currentTime()) }
    var errores by remember { mutableStateOf<List<String>>(emptyList()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val dateState = rememberDatePickerState()
    val timeState = rememberTimePickerState(is24Hour = true)
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedLabelColor = Color.White,
        unfocusedLabelColor = Color.White.copy(alpha = 0.9f),
        focusedBorderColor = Color.White,
        unfocusedBorderColor = Color.White.copy(alpha = 0.85f),
        focusedTrailingIconColor = Color.White,
        unfocusedTrailingIconColor = Color.White,
        cursorColor = Color.White
    )

    fun validar(): Boolean {
        val triggerAtMillis = parseReminderDateTimeMillis(fecha, hora)
        errores = listOfNotNull(
            if (descripcion.isBlank()) "Descripcion obligatoria." else null,
            if (fecha.isBlank()) "Fecha obligatoria." else null,
            if (hora.isBlank()) "Hora obligatoria." else null,
            if (mascotaId <= 0) "Mascota invalida." else null,
            if (triggerAtMillis == null) "Fecha u hora invalida." else null,
            if (triggerAtMillis != null && triggerAtMillis <= System.currentTimeMillis()) "El recordatorio debe ser futuro." else null
        )
        return errores.isEmpty()
    }

    val scrollState = rememberScrollState()

    BackgroundColumn(background = R.drawable.planilla_medica) {
        androidx.compose.foundation.layout.Column(modifier = Modifier.verticalScroll(scrollState)) {
        TopAppBar(
            title = { Text("Agregar recordatorio", fontWeight = FontWeight.ExtraBold) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } }
        )
        OutlinedTextField(
            descripcion,
            { descripcion = it },
            label = { Text("Descripcion") },
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = fecha,
            onValueChange = {},
            readOnly = true,
            label = { Text("Fecha") },
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Filled.DateRange, "Fecha") } }
        )
        OutlinedTextField(
            value = hora,
            onValueChange = {},
            readOnly = true,
            label = { Text("Hora") },
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = { IconButton(onClick = { showTimePicker = true }) { Icon(Icons.Filled.Edit, "Hora") } }
        )
        ErrorList(errors = errores, modifier = Modifier.padding(top = 8.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                if (!validar()) return@Button
                scope.launch {
                    val descripcionLimpia = descripcion.trim()
                    val recordatorioId = recordatorioDao.insert(
                        Recordatorio(
                            mascotaId = mascotaId,
                            fecha = fecha,
                            hora = hora,
                            descripcion = descripcionLimpia
                        )
                    )
                    parseReminderDateTimeMillis(fecha, hora)?.let { triggerAtMillis ->
                        ReminderScheduler.scheduleReminder(
                            context = context,
                            reminderId = recordatorioId.toInt(),
                            description = descripcionLimpia,
                            triggerAtMillis = triggerAtMillis
                        )
                    }
                    historialDao.insert(
                        HistorialMedico(
                            mascotaId = mascotaId,
                            fecha = fecha,
                            sintoma = "Recordatorio creado",
                            tratamiento = descripcionLimpia
                        )
                    )
                    onBack()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Guardar recordatorio") }
        Spacer(modifier = Modifier.height(12.dp))
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

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    hora = formatTime(timeState.hour, timeState.minute)
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") } },
            text = { TimePicker(state = timeState) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarRecordatorioScreen(
    recordatorioId: Int,
    recordatorioDao: RecordatorioDao,
    historialDao: HistorialMedicoDao,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var mascotaId by remember { mutableStateOf(-1) }
    var descripcion by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf(currentDate()) }
    var hora by remember { mutableStateOf(currentTime()) }
    var errores by remember { mutableStateOf<List<String>>(emptyList()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(true) }
    var notFound by remember { mutableStateOf(false) }
    val dateState = rememberDatePickerState()
    val timeState = rememberTimePickerState(is24Hour = true)
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedLabelColor = Color.White,
        unfocusedLabelColor = Color.White.copy(alpha = 0.9f),
        focusedBorderColor = Color.White,
        unfocusedBorderColor = Color.White.copy(alpha = 0.85f),
        focusedTrailingIconColor = Color.White,
        unfocusedTrailingIconColor = Color.White,
        cursorColor = Color.White
    )

    fun validar(): Boolean {
        val triggerAtMillis = parseReminderDateTimeMillis(fecha, hora)
        errores = listOfNotNull(
            if (descripcion.isBlank()) "Descripcion obligatoria." else null,
            if (fecha.isBlank()) "Fecha obligatoria." else null,
            if (hora.isBlank()) "Hora obligatoria." else null,
            if (recordatorioId <= 0) "Recordatorio invalido." else null,
            if (mascotaId <= 0) "Mascota invalida." else null,
            if (triggerAtMillis == null) "Fecha u hora invalida." else null,
            if (triggerAtMillis != null && triggerAtMillis <= System.currentTimeMillis()) "El recordatorio debe ser futuro." else null
        )
        return errores.isEmpty()
    }

    LaunchedEffect(recordatorioId) {
        val actual = recordatorioDao.getById(recordatorioId)
        if (actual == null) {
            notFound = true
        } else {
            mascotaId = actual.mascotaId
            descripcion = actual.descripcion
            fecha = actual.fecha
            hora = actual.hora
        }
        cargando = false
    }

    val scrollState = rememberScrollState()

    BackgroundColumn(background = R.drawable.planilla_medica) {
        androidx.compose.foundation.layout.Column(modifier = Modifier.verticalScroll(scrollState)) {
            TopAppBar(
                title = { Text("Editar recordatorio", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } }
            )

            if (cargando) {
                Text("Cargando...", color = Color.White)
                return@Column
            }

            if (notFound) {
                Text("No se encontro el recordatorio.", color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Volver") }
                return@Column
            }

            OutlinedTextField(
                descripcion,
                { descripcion = it },
                label = { Text("Descripcion") },
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = fecha,
                onValueChange = {},
                readOnly = true,
                label = { Text("Fecha") },
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Filled.DateRange, "Fecha") } }
            )
            OutlinedTextField(
                value = hora,
                onValueChange = {},
                readOnly = true,
                label = { Text("Hora") },
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { IconButton(onClick = { showTimePicker = true }) { Icon(Icons.Filled.Edit, "Hora") } }
            )
            ErrorList(errors = errores, modifier = Modifier.padding(top = 8.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    if (!validar()) return@Button
                    scope.launch {
                        val descripcionLimpia = descripcion.trim()
                        val actualizado = Recordatorio(
                            id = recordatorioId,
                            mascotaId = mascotaId,
                            fecha = fecha,
                            hora = hora,
                            descripcion = descripcionLimpia
                        )
                        recordatorioDao.update(actualizado)
                        ReminderScheduler.cancelReminder(context, recordatorioId)
                        parseReminderDateTimeMillis(fecha, hora)?.let { triggerAtMillis ->
                            ReminderScheduler.scheduleReminder(
                                context = context,
                                reminderId = recordatorioId,
                                description = descripcionLimpia,
                                triggerAtMillis = triggerAtMillis
                            )
                        }
                        historialDao.insert(
                            HistorialMedico(
                                mascotaId = mascotaId,
                                fecha = fecha,
                                sintoma = "Recordatorio actualizado",
                                tratamiento = descripcionLimpia
                            )
                        )
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar cambios") }
            Spacer(modifier = Modifier.height(12.dp))
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

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    hora = formatTime(timeState.hour, timeState.minute)
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") } },
            text = { TimePicker(state = timeState) }
        )
    }
}

private fun parseReminderDateTimeMillis(fecha: String, hora: String): Long? {
    return runCatching {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        val localDateTime = LocalDateTime.parse("$fecha $hora", formatter)
        localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }.getOrNull()
}
