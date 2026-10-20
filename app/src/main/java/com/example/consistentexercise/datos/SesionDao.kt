package com.example.consistentexercise.datos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SesionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(sesion: SesionEntrenamiento)

    @Update
    suspend fun actualizar(sesion: SesionEntrenamiento)

    @Query("DELETE FROM sesiones WHERE id = :id")
    suspend fun eliminar(id: String)

    @Query("SELECT * FROM sesiones ORDER BY finMs DESC")
    fun observarTodas(): Flow<List<SesionEntrenamiento>>
}
