package com.christianstuart.mimascota.backup

import android.content.Context
import android.net.Uri
import com.christianstuart.mimascota.HistorialMedico
import com.christianstuart.mimascota.Mascota
import com.christianstuart.mimascota.MascotaDatabase
import com.christianstuart.mimascota.Recordatorio
import com.christianstuart.mimascota.reminders.ReminderScheduler
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object BackupManager {
    private const val SCHEMA_VERSION = 1
    private val reminderDateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    suspend fun exportToUri(
        context: Context,
        db: MascotaDatabase,
        outputUri: Uri
    ) {
        val mascotas = db.mascotaDao().getAllSnapshot()
        val historial = db.historialMedicoDao().getAllSnapshot()
        val recordatorios = db.recordatorioDao().getAllSnapshot()

        val payload = JSONObject().apply {
            put("schemaVersion", SCHEMA_VERSION)
            put("exportedAtEpochMillis", System.currentTimeMillis())
            put("mascotas", mascotas.toJsonArray { it.toJson() })
            put("historialMedico", historial.toJsonArray { it.toJson() })
            put("recordatorios", recordatorios.toJsonArray { it.toJson() })
        }

        context.contentResolver.openOutputStream(outputUri)?.use { stream ->
            stream.writer(Charsets.UTF_8).use { writer ->
                writer.write(payload.toString(2))
            }
        } ?: error("No se pudo abrir el archivo de destino.")
    }

    suspend fun importFromUri(
        context: Context,
        db: MascotaDatabase,
        inputUri: Uri
    ) {
        val jsonText = context.contentResolver.openInputStream(inputUri)?.use { stream ->
            stream.bufferedReader(Charsets.UTF_8).readText()
        } ?: error("No se pudo leer el archivo seleccionado.")

        val root = JSONObject(jsonText)
        val version = root.optInt("schemaVersion", -1)
        if (version != SCHEMA_VERSION) {
            error("Version de respaldo no compatible: $version")
        }

        val mascotas = root.optJSONArray("mascotas").toMascotas()
        val historial = root.optJSONArray("historialMedico").toHistorial()
        val recordatorios = root.optJSONArray("recordatorios").toRecordatorios()

        val existingReminders = db.recordatorioDao().getAllSnapshot()
        existingReminders.forEach { ReminderScheduler.cancelReminder(context, it.id) }

        db.clearAllTables()

        mascotas.forEach { db.mascotaDao().insert(it) }
        historial.forEach { db.historialMedicoDao().insert(it) }
        recordatorios.forEach { recordatorio ->
            db.recordatorioDao().insert(recordatorio)
            parseReminderDateTimeMillis(recordatorio.fecha, recordatorio.hora)
                ?.takeIf { it > System.currentTimeMillis() }
                ?.let { triggerAtMillis ->
                    ReminderScheduler.scheduleReminder(
                        context = context,
                        reminderId = recordatorio.id,
                        description = recordatorio.descripcion,
                        triggerAtMillis = triggerAtMillis
                    )
                }
        }
    }

    private fun parseReminderDateTimeMillis(fecha: String, hora: String): Long? {
        return runCatching {
            val localDateTime = LocalDateTime.parse("$fecha $hora", reminderDateTimeFormatter)
            localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }.getOrNull()
    }

    private fun Mascota.toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("nombre", nombre)
            put("tipo", tipo)
            put("sexo", sexo)
            put("fechaNacimiento", fechaNacimiento)
            put("raza", raza)
            put("descripcion", descripcion)
            put("pesoKg", pesoKg)
            put("fotoUri", fotoUri)
        }
    }

    private fun HistorialMedico.toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("mascotaId", mascotaId)
            put("fecha", fecha)
            put("sintoma", sintoma)
            put("tratamiento", tratamiento)
        }
    }

    private fun Recordatorio.toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("mascotaId", mascotaId)
            put("fecha", fecha)
            put("hora", hora)
            put("descripcion", descripcion)
        }
    }

    private fun JSONArray?.toMascotas(): List<Mascota> {
        if (this == null) return emptyList()
        val result = mutableListOf<Mascota>()
        for (i in 0 until length()) {
            val item = optJSONObject(i) ?: continue
            result.add(
                Mascota(
                    id = item.optInt("id", 0),
                    nombre = item.optString("nombre"),
                    tipo = item.optString("tipo"),
                    sexo = item.optString("sexo"),
                    fechaNacimiento = item.optString("fechaNacimiento"),
                    raza = item.optString("raza"),
                    descripcion = item.optString("descripcion"),
                    pesoKg = item.optDouble("pesoKg", 0.0),
                    fotoUri = item.optString("fotoUri", "").ifBlank { null }
                )
            )
        }
        return result
    }

    private fun JSONArray?.toHistorial(): List<HistorialMedico> {
        if (this == null) return emptyList()
        val result = mutableListOf<HistorialMedico>()
        for (i in 0 until length()) {
            val item = optJSONObject(i) ?: continue
            result.add(
                HistorialMedico(
                    id = item.optInt("id", 0),
                    mascotaId = item.optInt("mascotaId", 0),
                    fecha = item.optString("fecha"),
                    sintoma = item.optString("sintoma"),
                    tratamiento = item.optString("tratamiento")
                )
            )
        }
        return result
    }

    private fun JSONArray?.toRecordatorios(): List<Recordatorio> {
        if (this == null) return emptyList()
        val result = mutableListOf<Recordatorio>()
        for (i in 0 until length()) {
            val item = optJSONObject(i) ?: continue
            result.add(
                Recordatorio(
                    id = item.optInt("id", 0),
                    mascotaId = item.optInt("mascotaId", 0),
                    fecha = item.optString("fecha"),
                    hora = item.optString("hora"),
                    descripcion = item.optString("descripcion")
                )
            )
        }
        return result
    }

    private inline fun <T> List<T>.toJsonArray(serializer: (T) -> JSONObject): JSONArray {
        val array = JSONArray()
        forEach { array.put(serializer(it)) }
        return array
    }
}
