package com.example.mimascota

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "mascotas"
)
data class Mascota(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val tipo: String,
    val sexo: String = "",
    val fechaNacimiento: String = "",
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
