
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete
import androidx.room.Database
import androidx.room.RoomDatabase

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardActions

            childColumns = ["mascotaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("mascotaId")]
)
data class HistorialMedico(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mascotaId: Int,
    val fecha: String,
    val sintoma: String,
    val tratamiento: String
)

@Entity(
    tableName = "recordatorios",
    foreignKeys = [
        ForeignKey(
            entity = Mascota::class,
            parentColumns = ["id"],
            childColumns = ["mascotaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("mascotaId")]
)
data class Recordatorio(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mascotaId: Int,
    val fecha: String,
    val hora: String,
    val descripcion: String
)

@Dao
interface MascotaDao {
    @Query("SELECT * FROM mascotas")
    fun getAll(): List<Mascota>

    @Query("SELECT * FROM mascotas WHERE id = :id")
    fun getById(id: Int): Mascota?

    @Insert
    fun insert(mascota: Mascota)

    @Update
    fun update(mascota: Mascota)

    @Delete
    fun delete(mascota: Mascota)

@Dao
interface HistorialMedicoDao {
    @Query("SELECT * FROM historial_medico WHERE mascotaId = :mascotaId")
    fun getHistorialByMascota(mascotaId: Int): List<HistorialMedico>

    @Insert
    fun insert(historial: HistorialMedico)
}

@Dao
interface RecordatorioDao {
    @Query("SELECT * FROM recordatorios WHERE mascotaId = :mascotaId ORDER BY fecha DESC")
    fun getRecordatoriosByMascota(mascotaId: Int): List<Recordatorio>

    @Insert
    fun insert(recordatorio: Recordatorio)

    @Delete
    fun delete(recordatorio: Recordatorio)
}

@Database(entities = [Mascota::class, HistorialMedico::class, Recordatorio::class], version = 6)
abstract class MascotaDatabase : RoomDatabase() {
    abstract fun mascotaDao(): MascotaDao
    abstract fun historialMedicoDao(): HistorialMedicoDao
    abstract fun recordatorioDao(): RecordatorioDao
}

/* ------------------ MAIN ------------------ */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(
            applicationContext,
            MascotaDatabase::class.java,
            "mascotas-db"
        ).fallbackToDestructiveMigration().allowMainThreadQueries().build()

        setContent {
            MaterialTheme {
                AppNavHost(db)
            }
        }
    }
}

/* ------------------ NAVIGATION & NOTIFICATION SYSTEM ------------------ */
@Composable
fun AppNavHost(db: MascotaDatabase) {
    val navController = rememberNavController()
    val context = LocalContext.current
    
    // Sistema de notificaciones para recordatorios
    var activeNotification by remember { mutableStateOf<Recordatorio?>(null) }
    var notificationShowCount by remember { mutableStateOf(0) }
    var lastNotificationTime by remember { mutableStateOf(0L) }
    
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(10000) // Chequear cada 10 segundos
            val now = java.util.Calendar.getInstance()
            val currentDate = String.format("%02d/%02d/%04d", now.get(java.util.Calendar.DAY_OF_MONTH), now.get(java.util.Calendar.MONTH) + 1, now.get(java.util.Calendar.YEAR))
            val currentTime = String.format("%02d:%02d", now.get(java.util.Calendar.HOUR_OF_DAY), now.get(java.util.Calendar.MINUTE))
            
            val allRecordatorios = db.recordatorioDao().getRecordatoriosByMascota(-1).takeIf { it.isNotEmpty() } ?: emptyList()
            val recordatoriosHoy = try {
                val allRecords = mutableListOf<Recordatorio>()
                val allMascotas = db.mascotaDao().getAll()
                allMascotas.forEach { mascota ->
                    allRecords.addAll(db.recordatorioDao().getRecordatoriosByMascota(mascota.id))
                }
                allRecords.filter { it.fecha == currentDate && it.hora == currentTime }
            } catch (e: Exception) {
                emptyList()
            }
            
            import androidx.room.Entity
            import androidx.room.PrimaryKey
            import androidx.room.ForeignKey
            import androidx.room.Index
            import androidx.room.Dao
            import androidx.room.Query
            import androidx.room.Insert
            import androidx.room.Update
            import androidx.room.Delete
            import androidx.room.Database
            import androidx.room.RoomDatabase

            import androidx.compose.runtime.*
            import androidx.compose.material3.*
            import androidx.compose.foundation.layout.*
            import androidx.compose.foundation.Image
            import androidx.compose.ui.Modifier
            import androidx.compose.ui.unit.dp
            import androidx.compose.ui.unit.sp
            import androidx.compose.ui.text.font.FontWeight
            import androidx.compose.ui.graphics.Color
            import androidx.compose.ui.platform.LocalContext
            import androidx.compose.foundation.lazy.LazyColumn
            import androidx.compose.foundation.lazy.items
            import androidx.compose.foundation.shape.RoundedCornerShape
            import androidx.compose.material.icons.Icons
            import androidx.compose.material.icons.filled.*
            import androidx.compose.ui.res.painterResource
            import androidx.compose.ui.Alignment
            import androidx.compose.ui.text.input.TextFieldValue
            import androidx.compose.ui.text.input.VisualTransformation
            import androidx.compose.ui.text.input.PasswordVisualTransformation
            import androidx.compose.ui.text.input.KeyboardType
            import androidx.compose.ui.text.input.ImeAction
            import androidx.compose.ui.text.input.KeyboardOptions
            import androidx.compose.ui.text.input.KeyboardActions

                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { 
                        showAlertDialog = false
                        activeNotification = null
                    }) { 
                        Text("Confirmar", fontWeight = FontWeight.Bold, fontSize = 16.sp) 
                    }
                }
            )
        }
    }
}

/* ------------------ UI - LISTA DE MASCOTAS ------------------ */
@Composable
fun MiMascotaApp(mascotaDao: MascotaDao, navController: NavController) {
    var mascotas by remember { mutableStateOf(mascotaDao.getAll()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var mascotaToDelete by remember { mutableStateOf<Mascota?>(null) }

    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect {
            mascotas = mascotaDao.getAll()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.una_veterinaria_sonr),
            contentDescription = "Veterinaria sonriendo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Mascotas Registradas", style = MaterialTheme.typography.headlineMedium, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(mascotas) { mascota ->
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically() + fadeIn(),
                        exit = slideOutVertically() + fadeOut()
                    ) {
                        Card(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
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
                                    Text("${mascota.nombre} - ${mascota.tipo} - ${mascota.raza} - ${mascota.pesoKg} kg", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    
                                    Row(modifier = Modifier.padding(top = 8.dp)) {
                                        Button(onClick = { navController.navigate("historial/${mascota.id}") }, modifier = Modifier.weight(1f)) {
                                            Text("Historial", fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(onClick = { navController.navigate("recordatorios/${mascota.id}") }, modifier = Modifier.weight(1f)) {
                                            Text("Recordatorios", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Row(modifier = Modifier.padding(top = 8.dp)) {
                                        IconButton(onClick = { navController.navigate("editar_mascota/${mascota.id}") }) {
                                            Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(onClick = {
                                            mascotaToDelete = mascota
                                            showDeleteConfirm = true
                                        }) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Botón flotante para agregar mascota (más arriba)
        FloatingActionButton(
            onClick = { navController.navigate("agregar_mascota") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 140.dp, end = 16.dp) // sube el botón aún más
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Agregar mascota")
        }
    }

    if (showDeleteConfirm && mascotaToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Confirmar eliminación", fontWeight = FontWeight.Bold) },
            text = { Text("¿Deseas eliminar a ${mascotaToDelete?.nombre}? Esta acción no se puede deshacer.", fontWeight = FontWeight.Bold) },
            confirmButton = {
                TextButton(onClick = {
                    mascotaToDelete?.let { mascota ->
                        mascotaDao.delete(mascota)
                        mascotas = mascotaDao.getAll()
                    }
                    showDeleteConfirm = false
                    mascotaToDelete = null
                }) { Text("Sí, eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showDeleteConfirm = false
                    mascotaToDelete = null
                }) { Text("Cancelar") }
            }
        )
    }
}
}

/* ------------------ AGREGAR MASCOTA SCREEN ------------------ */
@Composable
fun AgregarMascotaScreen(mascotaDao: MascotaDao, navController: NavController) {
    var nombre by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("") }
    var raza by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var peso by remember { mutableStateOf("") }
    var fotoUri by remember { mutableStateOf<Uri?>(null) }
    var errores by remember { mutableStateOf<List<String>>(emptyList()) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            fotoUri = uri
        }
    )

    fun validarFormulario(): Boolean {
        errores = listOfNotNull(
            if (nombre.isBlank()) "El nombre es requerido" else null,
            if (tipo.isBlank()) "El tipo de animal es requerido" else null,
            if (raza.isBlank()) "La raza es requerida" else null,
            if (descripcion.isBlank()) "La descripción es requerida" else null,
            if (peso.isBlank()) "El peso es requerido" else null,
            if (peso.toDoubleOrNull() == null && peso.isNotBlank()) "El peso debe ser un número válido" else null
        )
        return errores.isEmpty()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.una_veterinaria_sonr),
            contentDescription = "Veterinaria sonriendo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                }
                Text("Registrar Nueva Mascota", style = MaterialTheme.typography.headlineMedium, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre", fontWeight = FontWeight.Bold, fontSize = 18.sp) }, modifier = Modifier.fillMaxWidth(), textStyle = LocalTextStyle.current.copy(fontSize = 18.sp))
            Spacer(modifier = Modifier.height(8.dp))
            TipoAnimalDropdown(selectedTipo = tipo, onTipoSelected = { tipo = it })
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = raza, onValueChange = { raza = it }, label = { Text("Raza", fontWeight = FontWeight.Bold, fontSize = 18.sp) }, modifier = Modifier.fillMaxWidth(), textStyle = LocalTextStyle.current.copy(fontSize = 18.sp))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción", fontWeight = FontWeight.Bold, fontSize = 18.sp) }, modifier = Modifier.fillMaxWidth(), textStyle = LocalTextStyle.current.copy(fontSize = 18.sp))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = peso, onValueChange = { peso = it }, label = { Text("Peso (kg)", fontWeight = FontWeight.Bold, fontSize = 18.sp) }, modifier = Modifier.fillMaxWidth(), textStyle = LocalTextStyle.current.copy(fontSize = 18.sp))
            
            AnimatedVisibility(visible = errores.isNotEmpty()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    errores.forEach { error ->
                        Text(error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { imagePickerLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                Text("Seleccionar foto", fontWeight = FontWeight.Bold)
            }
            fotoUri?.let {
                AsyncImage(
                    model = it,
                    contentDescription = "Foto de la mascota",
                    modifier = Modifier
                        .size(128.dp)
                        .padding(top = 8.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (validarFormulario()) {
                    val nuevaMascota = Mascota(
                        nombre = nombre,
                        tipo = tipo,
                        raza = raza,
                        descripcion = descripcion,
                        pesoKg = peso.toDoubleOrNull() ?: 0.0,
                        fotoUri = fotoUri?.toString()
                    )
                    mascotaDao.insert(nuevaMascota)
                    navController.popBackStack()
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Guardar mascota", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/* ------------------ HISTORIAL SCREEN ------------------ */
@Composable
fun HistorialMedicoScreen(mascotaId: Int, historialDao: HistorialMedicoDao, navController: NavController) {
    val context = LocalContext.current
    var fecha by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    var sintoma by remember { mutableStateOf("") }
    var tratamiento by remember { mutableStateOf("") }
    var errores by remember { mutableStateOf<List<String>>(emptyList()) }

    var historial by remember { mutableStateOf(historialDao.getHistorialByMascota(mascotaId)) }

    fun validarFormularioHistorial(): Boolean {
        errores = listOfNotNull(
            if (fecha.isBlank()) "La fecha es requerida" else null,
            if (sintoma.isBlank()) "El síntoma es requerido" else null,
            if (tratamiento.isBlank()) "El tratamiento es requerido" else null
        )
        return errores.isEmpty()
    }

    fun compartirPorWhatsApp() {
        if (historial.isEmpty()) {
            return
        }
        
        val mensaje = StringBuilder()
        mensaje.append("📋 *HISTORIAL MÉDICO* 📋\n\n")
        historial.forEach { h ->
            mensaje.append("📅 Fecha: ${h.fecha}\n")
            mensaje.append("🤒 Síntoma: ${h.sintoma}\n")
            mensaje.append("💊 Tratamiento: ${h.tratamiento}\n")
            mensaje.append("─────────────────────\n\n")
        }
        
        val intent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            putExtra(android.content.Intent.EXTRA_TEXT, mensaje.toString())
            type = "text/plain"
            `package` = "com.whatsapp"
        }
        
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Si WhatsApp no está instalado, abrir Play Store
            val playStoreIntent = android.content.Intent(
                android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse("https://play.google.com/store/apps/details?id=com.whatsapp")
            )
            context.startActivity(playStoreIntent)
        }
    }

    fun formatearFecha(millis: Long): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = millis
        val day = String.format("%02d", calendar.get(java.util.Calendar.DAY_OF_MONTH))
        val month = String.format("%02d", calendar.get(java.util.Calendar.MONTH) + 1)
        val year = calendar.get(java.util.Calendar.YEAR)
        return "$day/$month/$year"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.planilla_medica),
            contentDescription = "Planilla médica",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                }
                Text("Historial médico", style = MaterialTheme.typography.headlineMedium, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                IconButton(onClick = { compartirPorWhatsApp() }) {
                    Icon(Icons.Filled.Share, contentDescription = "Compartir por WhatsApp")
                }
            }

            OutlinedTextField(
                value = fecha,
                onValueChange = {},
                label = { Text("Fecha", fontWeight = FontWeight.Bold) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                readOnly = true,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.secondary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.secondary
                ),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Filled.DateRange, contentDescription = "Seleccionar fecha", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let {
                                fecha = formatearFecha(it)
                            }
                            showDatePicker = false
                        }) { Text("Seleccionar", fontWeight = FontWeight.Bold) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
                    }
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Selecciona la fecha del historial", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))
                        DatePicker(state = datePickerState)
                    }
                }
            }
            OutlinedTextField(value = sintoma, onValueChange = { sintoma = it }, label = { Text("Síntoma", fontWeight = FontWeight.Bold, fontSize = 18.sp) }, modifier = Modifier.fillMaxWidth(), textStyle = LocalTextStyle.current.copy(fontSize = 18.sp))
            OutlinedTextField(value = tratamiento, onValueChange = { tratamiento = it }, label = { Text("Tratamiento", fontWeight = FontWeight.Bold, fontSize = 18.sp) }, modifier = Modifier.fillMaxWidth(), textStyle = LocalTextStyle.current.copy(fontSize = 18.sp))

            AnimatedVisibility(visible = errores.isNotEmpty()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    errores.forEach { error ->
                        Text(error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (mascotaId > 0 && validarFormularioHistorial()) {
                    val nuevoHistorial = HistorialMedico(mascotaId = mascotaId, fecha = fecha, sintoma = sintoma, tratamiento = tratamiento)
                    historialDao.insert(nuevoHistorial)
                    historial = historialDao.getHistorialByMascota(mascotaId)
                    fecha = ""; sintoma = ""; tratamiento = ""
                    errores = emptyList()
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Guardar historial", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Historial registrado:", style = MaterialTheme.typography.titleMedium, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            LazyColumn {
                items(historial) { h ->
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically() + fadeIn(),
                        exit = slideOutVertically() + fadeOut()
                    ) {
                        Card(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("${h.fecha} - ${h.sintoma}: ${h.tratamiento}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ------------------ RECORDATORIOS MASCOTA SCREEN ------------------ */
@Composable
fun RecordatoriosMascotaScreen(mascotaId: Int, recordatorioDao: RecordatorioDao, navController: NavController) {
    var recordatorios by remember { mutableStateOf(recordatorioDao.getRecordatoriosByMascota(mascotaId)) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var recordatorioToDelete by remember { mutableStateOf<Recordatorio?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.planilla_medica),
            contentDescription = "Planilla médica",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                }
                Text("Recordatorios", style = MaterialTheme.typography.headlineMedium, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(recordatorios) { recordatorio ->
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically() + fadeIn(),
                        exit = slideOutVertically() + fadeOut()
                    ) {
                        Card(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${recordatorio.fecha} - ${recordatorio.hora}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(recordatorio.descripcion, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                                IconButton(onClick = {
                                    recordatorioToDelete = recordatorio
                                    showDeleteConfirm = true
                                }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { navController.navigate("agregar_recordatorio/$mascotaId") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 80.dp) // sube el botón aún más
            ) {
                Text("Agregar Recordatorio", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }

    if (showDeleteConfirm && recordatorioToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Confirmar eliminación", fontWeight = FontWeight.Bold) },
            text = { Text("¿Deseas eliminar este recordatorio?", fontWeight = FontWeight.Bold) },
            confirmButton = {
                TextButton(onClick = {
                    recordatorioToDelete?.let { recordatorio ->
                        recordatorioDao.delete(recordatorio)
                        recordatorios = recordatorioDao.getRecordatoriosByMascota(mascotaId)
                    }
                    showDeleteConfirm = false
                    recordatorioToDelete = null
                }) { Text("Sí, eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showDeleteConfirm = false
                    recordatorioToDelete = null
                }) { Text("Cancelar") }
            }
        )
    }
}

/* ------------------ AGREGAR RECORDATORIO SCREEN ------------------ */
@Composable
fun AgregarRecordatorioScreen(
    mascotaId: Int,
    recordatorioDao: RecordatorioDao,
    navController: NavController
) {
    // Acceso a la base de datos para historial
    val context = LocalContext.current
    val db = remember {
        androidx.room.Room.databaseBuilder(
            context.applicationContext,
            MascotaDatabase::class.java,
            "mascotas-db"
        ).fallbackToDestructiveMigration().allowMainThreadQueries().build()
    }
    var descripcion by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }
    var errores by remember { mutableStateOf<List<String>>(emptyList()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState()

    fun validarFormularioRecordatorio(): Boolean {
        errores = listOfNotNull(
            if (descripcion.isBlank()) "La descripción es requerida" else null,
            if (fecha.isBlank()) "La fecha es requerida" else null,
            if (hora.isBlank()) "La hora es requerida" else null
        )
        return errores.isEmpty()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.planilla_medica),
            contentDescription = "Planilla médica",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                }
                Text("Agregar Recordatorio", style = MaterialTheme.typography.headlineMedium, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción", fontWeight = FontWeight.Bold, fontSize = 18.sp) }, modifier = Modifier.fillMaxWidth(), textStyle = LocalTextStyle.current.copy(fontSize = 18.sp))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = fecha,
                onValueChange = {},
                label = { Text("Fecha", fontWeight = FontWeight.Bold) },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Seleccionar fecha")
                    }
                }
            )
            OutlinedTextField(
                value = hora,
                onValueChange = {},
                label = { Text("Hora", fontWeight = FontWeight.Bold) },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showTimePicker = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Seleccionar hora")
                    }
                }
            )

            AnimatedVisibility(visible = errores.isNotEmpty()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    errores.forEach { error ->
                        Text(error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (mascotaId > 0 && validarFormularioRecordatorio()) {
                    val nuevoRecordatorio = Recordatorio(
                        mascotaId = mascotaId,
                        descripcion = descripcion,
                        fecha = fecha,
                        hora = hora
                    )
                    recordatorioDao.insert(nuevoRecordatorio)
                    // Crear evento en historial médico
                    val eventoHistorial = HistorialMedico(
                        mascotaId = mascotaId,
                        fecha = fecha,
                        sintoma = "Recordatorio creado",
                        tratamiento = descripcion
                    )
                    db.historialMedicoDao().insert(eventoHistorial)
                    navController.popBackStack()
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Guardar Recordatorio", fontWeight = FontWeight.Bold)
            }
        }
    }
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        fecha = formatearFecha(it)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    hora = formatearHora(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}

/* ------------------ DROPDOWN ------------------ */
@Composable
fun TipoAnimalDropdown(selectedTipo: String, onTipoSelected: (String) -> Unit) {
    val tipos = listOf("Perro", "Gato", "Ave", "Pez", "Conejo", "Hámster", "Tortuga", "Caballo", "Serpiente")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedTipo,
            onValueChange = {},
            label = { Text("Tipo de animal", fontWeight = FontWeight.Bold) },
            readOnly = true,
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            tipos.forEach { tipo ->
                DropdownMenuItem(
                    text = { Text(tipo, fontWeight = FontWeight.Bold) },
                    onClick = {
                        onTipoSelected(tipo)
                        expanded = false
                    }
                )
            }
        }
    }
}
