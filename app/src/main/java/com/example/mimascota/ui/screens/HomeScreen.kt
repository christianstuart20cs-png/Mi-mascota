package com.christianstuart.mimascota.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.christianstuart.mimascota.Mascota
import com.christianstuart.mimascota.MascotaDao
import com.christianstuart.mimascota.MascotaDatabase
import com.christianstuart.mimascota.RecordatorioDao
import com.christianstuart.mimascota.R
import com.christianstuart.mimascota.backup.BackupManager
import com.christianstuart.mimascota.reminders.ReminderScheduler
import kotlinx.coroutines.launch

@Composable
fun MiMascotaApp(
    db: MascotaDatabase,
    mascotaDao: MascotaDao,
    recordatorioDao: RecordatorioDao,
    navToAdd: () -> Unit,
    navToEdit: (Int) -> Unit,
    navToHistory: (Int) -> Unit,
    navToReminders: (Int) -> Unit
) {
    val mascotas by mascotaDao.getAll().collectAsStateEmpty()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var toDelete by remember { mutableStateOf<Mascota?>(null) }
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                BackupManager.exportToUri(context, db, uri)
            }.onSuccess {
                Toast.makeText(context, "Respaldo exportado.", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(
                    context,
                    "No se pudo exportar: ${it.message.orEmpty()}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                BackupManager.importFromUri(context, db, uri)
            }.onSuccess {
                Toast.makeText(context, "Respaldo importado.", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(
                    context,
                    "No se pudo importar: ${it.message.orEmpty()}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BackgroundColumn(background = R.drawable.una_veterinaria_sonr) {
            Text(
                text = "Mis Mascotas",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val fileName = "mimascota-backup-${System.currentTimeMillis()}.json"
                        exportLauncher.launch(fileName)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Exportar")
                }
                Button(
                    onClick = { importLauncher.launch(arrayOf("application/json", "text/plain")) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Importar")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 88.dp)
            ) {
                items(mascotas, key = { it.id }) { mascota ->
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
                            mascota.fotoUri?.let {
                                AsyncImage(
                                    model = it,
                                    contentDescription = "Foto de ${mascota.nombre}",
                                    modifier = Modifier
                                        .size(80.dp)
                                        .padding(end = 12.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${mascota.nombre} - ${mascota.tipo}",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${mascota.raza} - ${mascota.pesoKg} kg",
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${mascota.sexo} - Nac: ${mascota.fechaNacimiento}",
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = mascota.descripcion,
                                    color = Color.White,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(onClick = { navToHistory(mascota.id) }, modifier = Modifier.weight(1f)) {
                                        Text("Historial")
                                    }
                                    Button(
                                        onClick = { navToReminders(mascota.id) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Recordatorios")
                                    }
                                }
                                Row {
                                    IconButton(onClick = { navToEdit(mascota.id) }) {
                                        Icon(
                                            Icons.Filled.Edit,
                                            contentDescription = "Editar",
                                            tint = Color.White
                                        )
                                    }
                                    IconButton(onClick = { toDelete = mascota }) {
                                        Icon(
                                            Icons.Filled.Delete,
                                            contentDescription = "Eliminar",
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = navToAdd,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Agregar")
        }
    }

    toDelete?.let { mascota ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Eliminar mascota") },
            text = { Text("Se eliminara ${mascota.nombre} con su historial y recordatorios.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            recordatorioDao.getRecordatoriosSnapshotByMascota(mascota.id).forEach { reminder ->
                                ReminderScheduler.cancelReminder(context, reminder.id)
                            }
                            mascotaDao.delete(mascota)
                            toDelete = null
                        }
                    }
                ) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancelar") } }
        )
    }
}
