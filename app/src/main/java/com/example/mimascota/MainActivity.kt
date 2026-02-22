package com.example.mimascota

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import coil.compose.AsyncImage
import com.example.mimascota.ui.theme.MiMascotaTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            MascotaDatabase::class.java,
            "mascotas-db"
        ).build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MiMascotaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavHost(db)
                }
            }
        }
    }
}

@Composable
fun AppNavHost(db: MascotaDatabase) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            MiMascotaApp(
                mascotaDao = db.mascotaDao(),
                navToAdd = { navController.navigate("add_pet") },
                navToEdit = { id -> navController.navigate("edit_pet/$id") },
                navToHistory = { id -> navController.navigate("history/$id") },
                navToReminders = { id -> navController.navigate("reminders/$id") }
            )
        }
        composable("add_pet") {
            MascotaFormScreen("Registrar mascota", db.mascotaDao(), null) { navController.popBackStack() }
        }
        composable(
            route = "edit_pet/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStack ->
            MascotaFormScreen(
                title = "Editar mascota",
                mascotaDao = db.mascotaDao(),
                mascotaId = backStack.arguments?.getInt("id")
            ) { navController.popBackStack() }
        }
        composable(
            route = "history/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStack ->
            HistorialMedicoScreen(
                mascotaId = backStack.arguments?.getInt("id") ?: -1,
                historialDao = db.historialMedicoDao()
            ) { navController.popBackStack() }
        }
        composable(
            route = "reminders/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStack ->
            val mascotaId = backStack.arguments?.getInt("id") ?: -1
            RecordatoriosMascotaScreen(
                mascotaId = mascotaId,
                recordatorioDao = db.recordatorioDao(),
                onBack = { navController.popBackStack() },
                onAdd = { navController.navigate("add_reminder/$mascotaId") }
            )
        }
        composable(
            route = "add_reminder/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStack ->
            AgregarRecordatorioScreen(
                mascotaId = backStack.arguments?.getInt("id") ?: -1,
                recordatorioDao = db.recordatorioDao(),
                historialDao = db.historialMedicoDao()
            ) { navController.popBackStack() }
        }
    }
}

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
        Image(
            painter = painterResource(id = R.drawable.una_veterinaria_sonr),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Mascotas registradas", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(mascotas, key = { it.id }) { mascota ->
                    AnimatedVisibility(visible = true, enter = slideInVertically() + fadeIn(), exit = slideOutVertically() + fadeOut()) {
                        Card(shape = RoundedCornerShape(10.dp)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                mascota.fotoUri?.let {
                                    AsyncImage(
                                        model = it,
                                        contentDescription = "Foto ${mascota.nombre}",
                                        modifier = Modifier.size(80.dp).padding(end = 12.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${mascota.nombre} - ${mascota.tipo}", fontWeight = FontWeight.Bold)
                                    Text("${mascota.raza} - ${mascota.pesoKg} kg")
                                    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(onClick = { navToHistory(mascota.id) }, modifier = Modifier.weight(1f)) { Text("Historial") }
                                        Button(onClick = { navToReminders(mascota.id) }, modifier = Modifier.weight(1f)) { Text("Recordatorios") }
                                    }
                                    Row {
                                        IconButton(onClick = { navToEdit(mascota.id) }) { Icon(Icons.Filled.Edit, "Editar") }
                                        IconButton(onClick = { toDelete = mascota }) { Icon(Icons.Filled.Delete, "Eliminar") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        FloatingActionButton(onClick = navToAdd, modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
            Icon(Icons.Filled.Add, contentDescription = "Agregar")
        }
    }

    toDelete?.let { mascota ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Eliminar mascota") },
            text = { Text("Se eliminara ${mascota.nombre} con su historial y recordatorios.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        mascotaDao.delete(mascota)
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

    fun validar(): Boolean {
        val valorPeso = peso.toDoubleOrNull()
        errores = listOfNotNull(
            if (nombre.isBlank()) "Nombre obligatorio." else null,
            if (tipo.isBlank()) "Tipo obligatorio." else null,
            if (raza.isBlank()) "Raza obligatoria." else null,
            if (descripcion.isBlank()) "Descripcion obligatoria." else null,
            if (valorPeso == null || valorPeso <= 0.0) "Peso invalido." else null
        )
        return errores.isEmpty()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.una_veterinaria_sonr),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(modifier = Modifier.padding(16.dp)) {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
            OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            TipoAnimalDropdown(selectedTipo = tipo, onTipoSelected = { tipo = it })
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(raza, { raza = it }, label = { Text("Raza") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(descripcion, { descripcion = it }, label = { Text("Descripcion") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(peso, { peso = it }, label = { Text("Peso (kg)") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { picker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) { Text("Seleccionar foto") }
            fotoUri?.let {
                AsyncImage(
                    model = it,
                    contentDescription = "Foto mascota",
                    modifier = Modifier.size(120.dp).padding(top = 8.dp),
                    contentScale = ContentScale.Crop
                )
            }
            AnimatedVisibility(visible = errores.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    errores.forEach { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = {
                if (!validar()) return@Button
                val payload = Mascota(
                    id = mascota?.id ?: 0,
                    nombre = nombre.trim(),
                    tipo = tipo,
                    raza = raza.trim(),
                    descripcion = descripcion.trim(),
                    pesoKg = peso.toDouble(),
                    fotoUri = fotoUri?.toString()
                )
                scope.launch {
                    if (mascota == null) mascotaDao.insert(payload) else mascotaDao.update(payload)
                    onBack()
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text(if (mascota == null) "Guardar mascota" else "Actualizar mascota")
            }
        }
    }
}

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
            if (tratamiento.isBlank()) "Tratamiento obligatorio." else null
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

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.planilla_medica),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(modifier = Modifier.padding(16.dp)) {
            TopAppBar(
                title = { Text("Historial medico", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Volver") }
                },
                actions = { IconButton(onClick = { share() }) { Icon(Icons.Filled.Share, "Compartir") } }
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
            OutlinedTextField(tratamiento, { tratamiento = it }, label = { Text("Tratamiento") }, modifier = Modifier.fillMaxWidth())
            AnimatedVisibility(visible = errores.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    errores.forEach { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = {
                if (!validar() || mascotaId <= 0) return@Button
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
            }, modifier = Modifier.fillMaxWidth()) { Text("Guardar historial") }
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(historial, key = { it.id }) { item ->
                    Card(shape = RoundedCornerShape(10.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(item.fecha, fontWeight = FontWeight.Bold)
                            Text("Sintoma: ${item.sintoma}")
                            Text("Tratamiento: ${item.tratamiento}")
                        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordatoriosMascotaScreen(
    mascotaId: Int,
    recordatorioDao: RecordatorioDao,
    onBack: () -> Unit,
    onAdd: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val recordatorios by recordatorioDao.getRecordatoriosByMascota(mascotaId).collectAsStateEmpty()
    var toDelete by remember { mutableStateOf<Recordatorio?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.planilla_medica),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(modifier = Modifier.padding(16.dp)) {
            TopAppBar(
                title = { Text("Recordatorios", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Volver") } }
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(recordatorios, key = { it.id }) { item ->
                    Card(shape = RoundedCornerShape(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${item.fecha} - ${item.hora}", fontWeight = FontWeight.Bold)
                                Text(item.descripcion)
                            }
                            IconButton(onClick = { toDelete = item }) { Icon(Icons.Filled.Delete, "Eliminar") }
                        }
                    }
                }
            }
            Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) { Text("Agregar recordatorio") }
        }
    }

    toDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Eliminar recordatorio") },
            text = { Text("Esta accion no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
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
    val scope = rememberCoroutineScope()
    var descripcion by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf(currentDate()) }
    var hora by remember { mutableStateOf(currentTime()) }
    var errores by remember { mutableStateOf<List<String>>(emptyList()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val dateState = rememberDatePickerState()
    val timeState = rememberTimePickerState(is24Hour = true)

    fun validar(): Boolean {
        errores = listOfNotNull(
            if (descripcion.isBlank()) "Descripcion obligatoria." else null,
            if (fecha.isBlank()) "Fecha obligatoria." else null,
            if (hora.isBlank()) "Hora obligatoria." else null
        )
        return errores.isEmpty()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.planilla_medica),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(modifier = Modifier.padding(16.dp)) {
            TopAppBar(
                title = { Text("Agregar recordatorio", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Volver") } }
            )
            OutlinedTextField(descripcion, { descripcion = it }, label = { Text("Descripcion") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = fecha,
                onValueChange = {},
                readOnly = true,
                label = { Text("Fecha") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Filled.DateRange, "Fecha") } }
            )
            OutlinedTextField(
                value = hora,
                onValueChange = {},
                readOnly = true,
                label = { Text("Hora") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { IconButton(onClick = { showTimePicker = true }) { Icon(Icons.Filled.Edit, "Hora") } }
            )
            AnimatedVisibility(visible = errores.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    errores.forEach { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = {
                if (!validar() || mascotaId <= 0) return@Button
                scope.launch {
                    recordatorioDao.insert(Recordatorio(mascotaId = mascotaId, fecha = fecha, hora = hora, descripcion = descripcion.trim()))
                    historialDao.insert(
                        HistorialMedico(
                            mascotaId = mascotaId,
                            fecha = fecha,
                            sintoma = "Recordatorio creado",
                            tratamiento = descripcion.trim()
                        )
                    )
                    onBack()
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("Guardar recordatorio") }
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
fun TipoAnimalDropdown(selectedTipo: String, onTipoSelected: (String) -> Unit) {
    val options = listOf("Perro", "Gato", "Ave", "Pez", "Conejo", "Hamster", "Tortuga", "Caballo", "Serpiente")
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedTipo,
            onValueChange = {},
            readOnly = true,
            label = { Text("Tipo de animal") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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

@Composable
private fun <T> Flow<List<T>>.collectAsStateEmpty() = collectAsState(initial = emptyList())

private fun currentDate(): String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
private fun currentTime(): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
private fun formatDate(millis: Long): String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(millis))
private fun formatTime(hour: Int, minute: Int): String = "%02d:%02d".format(hour, minute)
