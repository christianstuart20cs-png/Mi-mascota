package com.example.mimascota

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Mascota::class, HistorialMedico::class, Recordatorio::class], version = 6)
abstract class MascotaDatabase : RoomDatabase() {
    abstract fun mascotaDao(): MascotaDao
    abstract fun historialMedicoDao(): HistorialMedicoDao
    abstract fun recordatorioDao(): RecordatorioDao
}
