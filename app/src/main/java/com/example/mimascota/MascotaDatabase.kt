package com.example.mimascota

import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Mascota::class, HistorialMedico::class, Recordatorio::class], version = 7)
abstract class MascotaDatabase : RoomDatabase() {
    abstract fun mascotaDao(): MascotaDao
    abstract fun historialMedicoDao(): HistorialMedicoDao
    abstract fun recordatorioDao(): RecordatorioDao

    companion object {
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE mascotas ADD COLUMN sexo TEXT NOT NULL DEFAULT ''"
                )
                db.execSQL(
                    "ALTER TABLE mascotas ADD COLUMN fechaNacimiento TEXT NOT NULL DEFAULT ''"
                )
            }
        }
    }
}
