package com.example.mimascota.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mimascota.Mascota
import com.example.mimascota.MascotaDao
import com.example.mimascota.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MascotaFormScreen(
    title: String,
    mascotaDao: MascotaDao,
    mascotaId: Int?,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val mascota by produceState<Mascota?>(initialValue = null, key1 = mascotaId) {
        value = mascotaId?.let { mascotaDao.getById(it) }
    }

    var nombre by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("") }
    var raza by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var peso by remember { mutableStateOf("") }
    var fotoUri by remember { mutableStateOf<Uri?>(null) }
    var errores by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(mascota?.id) {
        mascota?.let {
            nombre = it.nombre
            tipo = it.tipo
            raza = it.raza
            descripcion = it.descripcion
            peso = it.pesoKg.toString()
            fotoUri = it.fotoUri?.let(Uri::parse)
        }
    }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> fotoUri = uri }
    )

    fun validar(): Double? {
        val valorPeso = peso.replace(",", ".").toDoubleOrNull()
        errores = listOfNotNull(
            if (nombre.isBlank()) "Nombre obligatorio." else null,
            if (tipo.isBlank()) "Tipo obligatorio." else null,
            if (raza.isBlank()) "Raza obligatoria." else null,
            if (descripcion.isBlank()) "Descripcion obligatoria." else null,
            if (valorPeso == null || valorPeso <= 0.0) "Peso invalido." else null
        )
        return if (errores.isEmpty()) valorPeso else null
    }

    val scrollState = rememberScrollState()

    BackgroundColumn(background = R.drawable.una_veterinaria_sonr) {
        androidx.compose.foundation.layout.Column(modifier = Modifier.verticalScroll(scrollState)) {
        TopAppBar(
            title = { Text(title, fontWeight = FontWeight.ExtraBold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            }
        )
        OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        TipoAnimalDropdown(selectedTipo = tipo, onTipoSelected = { tipo = it })
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(raza, { raza = it }, label = { Text("Raza") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            descripcion,
            { descripcion = it },
            label = { Text("Descripcion") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = peso,
            onValueChange = { peso = it },
            label = { Text("Peso (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { picker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
            Text("Seleccionar foto")
        }
        fotoUri?.let {
            AsyncImage(
                model = it,
                contentDescription = "Foto de la mascota",
                modifier = Modifier
                    .size(120.dp)
                    .padding(top = 8.dp),
                contentScale = ContentScale.Crop
            )
        }
        ErrorList(errors = errores, modifier = Modifier.padding(top = 8.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                val valorPeso = validar() ?: return@Button
                val payload = Mascota(
                    id = mascota?.id ?: 0,
                    nombre = nombre.trim(),
                    tipo = tipo,
                    raza = raza.trim(),
                    descripcion = descripcion.trim(),
                    pesoKg = valorPeso,
                    fotoUri = fotoUri?.toString()
                )
                scope.launch {
                    if (mascota == null) mascotaDao.insert(payload) else mascotaDao.update(payload)
                    onBack()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (mascota == null) "Guardar mascota" else "Actualizar mascota")
        }
        Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TipoAnimalDropdown(selectedTipo: String, onTipoSelected: (String) -> Unit) {
    val options = listOf("Perro", "Gato", "Ave", "Pez", "Conejo", "Hamster", "Tortuga", "Caballo", "Serpiente")
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedTipo,
            onValueChange = {},
            readOnly = true,
            label = { Text("Tipo de animal") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
        )
        androidx.compose.material3.DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onTipoSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
