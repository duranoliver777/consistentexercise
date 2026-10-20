package com.example.consistentexercise.datos

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sesiones",
    indices = [Index(value = ["fechaLocal"])]
)
data class SesionEntrenamiento(
    @PrimaryKey val id: String,
    val inicioMs: Long,
    val finMs: Long,
    val fechaLocal: String,
    val duracionSeg: Int,
    val intervalos: Boolean,
    val rondas: Int,
    val conteo: Int
)
