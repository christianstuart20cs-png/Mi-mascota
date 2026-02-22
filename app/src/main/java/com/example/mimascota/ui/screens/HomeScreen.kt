package com.example.mimascota.ui.screens

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mimascota.Mascota
import com.example.mimascota.MascotaDao
import com.example.mimascota.R
import kotlinx.coroutines.launch

@Composable
fun MiMascotaApp(
    mascotaDao: MascotaDao,
    navToAdd: () -> Unit,
    navToEdit: (Int) -> Unit,
    navToHistory: (Int) -> Unit,
    navToReminders: (Int) -> Unit
) {
    val mascotas by mascotaDao.getAll().collectAsStateEmpty()
    val scope = rememberCoroutineScope()
    var toDelete by remember { mutableStateOf<Mascota?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        BackgroundColumn(background = R.drawable.una_veterinaria_sonr) {
            Text(
                text = "Mascotas registradas",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 88.dp)
            ) {
                items(mascotas, key = { it.id }) { mascota ->
                    Card(shape = RoundedCornerShape(10.dp)) {
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
                                Text("${mascota.nombre} - ${mascota.tipo}", fontWeight = FontWeight.Bold)
                                Text(
                                    text = "${mascota.raza} - ${mascota.pesoKg} kg",
                                    maxLines = 1,
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
                                        Icon(Icons.Filled.Edit, contentDescription = "Editar")
                                    }
                                    IconButton(onClick = { toDelete = mascota }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
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
