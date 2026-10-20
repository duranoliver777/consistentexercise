package com.example.consistentexercise

import java.time.DayOfWeek

data class Ajustes(
    val duracionMinutos: Int = 1,
    val duracionSegundos: Int = 0,
    val intervalosActivos: Boolean = false,
    val pausaMinutos: Int = 0,
    val pausaSegundos: Int = 30,
    val rondas: Int = 3,
    val metaDiariaSesiones: Int = 1,
    val metaSemanalActiva: Boolean = false,
    val metaSemanalSesiones: Int = 3,
    val inicioSemana: DayOfWeek = DayOfWeek.MONDAY,
    val sonidoActivo: Boolean = true,
    val tonoUri: String? = null,
    val vibracionActiva: Boolean = false,
    val autoIniciar: Boolean = false
) {
    val duracionEjercicioSeg: Int get() = duracionMinutos * 60 + duracionSegundos
    val pausaSeg: Int get() = pausaMinutos * 60 + pausaSegundos
}
