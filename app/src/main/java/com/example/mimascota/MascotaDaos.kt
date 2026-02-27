package com.christianstuart.mimascota

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MascotaDao {
    @Query("SELECT * FROM mascotas")
    fun getAll(): Flow<List<Mascota>>

    @Query("SELECT * FROM mascotas")
    suspend fun getAllSnapshot(): List<Mascota>

    @Query("SELECT * FROM mascotas WHERE id = :id")
    suspend fun getById(id: Int): Mascota?

    @Insert
    suspend fun insert(mascota: Mascota)

    @Query("DELETE FROM mascotas")
    suspend fun deleteAll()

    @Update
    suspend fun update(mascota: Mascota)

    @Delete
    suspend fun delete(mascota: Mascota)
}

@Dao
interface HistorialMedicoDao {
    @Query("SELECT * FROM historial_medico WHERE mascotaId = :mascotaId ORDER BY id DESC")
    fun getHistorialByMascota(mascotaId: Int): Flow<List<HistorialMedico>>

    @Query("SELECT * FROM historial_medico")
    suspend fun getAllSnapshot(): List<HistorialMedico>

    @Insert
    suspend fun insert(historial: HistorialMedico)

    @Query("DELETE FROM historial_medico")
    suspend fun deleteAll()

    @Update
    suspend fun update(historial: HistorialMedico)

    @Delete
    suspend fun delete(historial: HistorialMedico)
}

@Dao
interface RecordatorioDao {
    @Query("SELECT * FROM recordatorios WHERE mascotaId = :mascotaId ORDER BY id DESC")
    fun getRecordatoriosByMascota(mascotaId: Int): Flow<List<Recordatorio>>

    @Query("SELECT * FROM recordatorios WHERE mascotaId = :mascotaId")
    suspend fun getRecordatoriosSnapshotByMascota(mascotaId: Int): List<Recordatorio>

    @Query("SELECT * FROM recordatorios WHERE id = :id")
    suspend fun getById(id: Int): Recordatorio?

    @Query("SELECT * FROM recordatorios")
    suspend fun getAllSnapshot(): List<Recordatorio>

    @Insert
    suspend fun insert(recordatorio: Recordatorio): Long

    @Query("DELETE FROM recordatorios")
    suspend fun deleteAll()

    @Update
    suspend fun update(recordatorio: Recordatorio)

    @Delete
    suspend fun delete(recordatorio: Recordatorio)
}
