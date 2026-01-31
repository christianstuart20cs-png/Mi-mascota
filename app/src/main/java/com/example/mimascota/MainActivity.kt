package com.example.mimascota

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.*
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.MenuAnchorType
import coil.compose.AsyncImage

/* ------------------ ROOM ------------------ */
@Entity(tableName = "mascotas")
data class Mascota(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val tipo: String,
    val raza: String,
    val descripcion: String,
    val pesoKg: Double,
    val fotoUri: String? = null
)

@Entity(
    tableName = "historial_medico",
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
            entity = HistorialMedico::class,
            parentColumns = ["id"],
            childColumns = ["historialId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("historialId")]
)
data class Recordatorio(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val historialId: Int,
    val fecha: String,
    val hora: String,
    val descripcion: String
)

@Dao
interface MascotaDao {
    @Query("SELECT * FROM mascotas")
    fun getAll(): List<Mascota>

    @Insert
    fun insert(mascota: Mascota)

    @Delete
    fun delete(mascota: Mascota)
}

@Dao
interface HistorialMedicoDao {
    @Query("SELECT * FROM historial_medico WHERE mascotaId = :mascotaId")
    fun getHistorialByMascota(mascotaId: Int): List<HistorialMedico>

    @Insert
    fun insert(historial: HistorialMedico)
}

@Dao
interface RecordatorioDao {
    @Query("SELECT * FROM recordatorios WHERE historialId = :historialId")
    fun getRecordatoriosByHistorial(historialId: Int): List<Recordatorio>

    @Insert
    fun insert(recordatorio: Recordatorio)

    @Delete
    fun delete(recordatorio: Recordatorio)
}

@Database(entities = [Mascota::class, HistorialMedico::class, Recordatorio::class], version = 5)
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

/* ------------------ NAVIGATION ------------------ */
@Composable
fun AppNavHost(db: MascotaDatabase) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "mascotas") {
        composable("mascotas") {
            MiMascotaApp(db.mascotaDao(), navController)
        }
        composable("agregar_mascota") {
            AgregarMascotaScreen(db.mascotaDao(), navController)
        }
        composable("historial/{mascotaId}") { backStackEntry ->
            val mascotaId = backStackEntry.arguments?.getString("mascotaId")?.toInt() ?: 0
            HistorialMedicoScreen(mascotaId, db.historialMedicoDao(), db.recordatorioDao(), navController)
        }
        composable("recordatorio/{historialId}") { backStackEntry ->
            val historialId = backStackEntry.arguments?.getString("historialId")?.toInt() ?: 0
            AgregarRecordatorioScreen(historialId, db.recordatorioDao(), navController)
        }
    }
}

/* ------------------ UI - LISTA DE MASCOTAS ------------------ */
@Composable
fun MiMascotaApp(mascotaDao: MascotaDao, navController: NavController) {
    var mascotas by remember { mutableStateOf(mascotaDao.getAll()) }

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
            Text("Mascotas Registradas", style = MaterialTheme.typography.headlineMedium, fontSize = 24.sp, fontWeight = FontWeight.Bold)
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
                                    Text("${mascota.nombre} - ${mascota.tipo} - ${mascota.raza} - ${mascota.pesoKg} kg", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    
                                    Row(modifier = Modifier.padding(top = 8.dp)) {
                                        Button(onClick = { navController.navigate("historial/${mascota.id}") }, modifier = Modifier.weight(1f)) {
                                            Text("Historial", fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        IconButton(onClick = {
                                            mascotaDao.delete(mascota)
                                            mascotas = mascotaDao.getAll()
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

        // Botón flotante para agregar mascota
        FloatingActionButton(
            onClick = { navController.navigate("agregar_mascota") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Agregar mascota")
        }
    }
}

/* ------------------ AGREGAR MASCOTA SCREEN ------------------ */
@OptIn(ExperimentalMaterial3Api::class)
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
                Text("Registrar Nueva Mascota", style = MaterialTheme.typography.headlineMedium, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre", fontWeight = FontWeight.Bold) }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            TipoAnimalDropdown(selectedTipo = tipo, onTipoSelected = { tipo = it })
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = raza, onValueChange = { raza = it }, label = { Text("Raza", fontWeight = FontWeight.Bold) }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción", fontWeight = FontWeight.Bold) }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = peso, onValueChange = { peso = it }, label = { Text("Peso (kg)", fontWeight = FontWeight.Bold) }, modifier = Modifier.fillMaxWidth())
            
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
fun HistorialMedicoScreen(mascotaId: Int, historialDao: HistorialMedicoDao, recordatorioDao: RecordatorioDao, navController: NavController) {
    val context = LocalContext.current
    var fecha by remember { mutableStateOf("") }
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
                Text("Historial médico", style = MaterialTheme.typography.headlineMedium, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = { compartirPorWhatsApp() }) {
                    Icon(Icons.Filled.Share, contentDescription = "Compartir por WhatsApp")
                }
            }

            OutlinedTextField(value = fecha, onValueChange = { fecha = it }, label = { Text("Fecha", fontWeight = FontWeight.Bold) }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = sintoma, onValueChange = { sintoma = it }, label = { Text("Síntoma", fontWeight = FontWeight.Bold) }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = tratamiento, onValueChange = { tratamiento = it }, label = { Text("Tratamiento", fontWeight = FontWeight.Bold) }, modifier = Modifier.fillMaxWidth())

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
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { navController.navigate("recordatorio/${h.id}") }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Agregar Recordatorio", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ------------------ AGREGAR RECORDATORIO SCREEN ------------------ */
@Composable
fun AgregarRecordatorioScreen(historialId: Int, recordatorioDao: RecordatorioDao, navController: NavController) {
    var descripcion by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }
    var errores by remember { mutableStateOf<List<String>>(emptyList()) }

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
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                }
                Text("Agregar Recordatorio", style = MaterialTheme.typography.headlineMedium, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción del Recordatorio", fontWeight = FontWeight.Bold) }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = fecha, onValueChange = { fecha = it }, label = { Text("Fecha (DD/MM/YYYY)", fontWeight = FontWeight.Bold) }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = hora, onValueChange = { hora = it }, label = { Text("Hora (HH:MM)", fontWeight = FontWeight.Bold) }, modifier = Modifier.fillMaxWidth())

            AnimatedVisibility(visible = errores.isNotEmpty()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    errores.forEach { error ->
                        Text(error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (historialId > 0 && validarFormularioRecordatorio()) {
                    val nuevoRecordatorio = Recordatorio(
                        historialId = historialId,
                        descripcion = descripcion,
                        fecha = fecha,
                        hora = hora
                    )
                    recordatorioDao.insert(nuevoRecordatorio)
                    // Aquí se podría programar la notificación con WorkManager
                    navController.popBackStack()
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Guardar Recordatorio", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/* ------------------ DROPDOWN ------------------ */
@OptIn(ExperimentalMaterial3Api::class)
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
