package com.example.consistentexercise.datos

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

fun fechaLocalDe(ms: Long, zona: ZoneId = ZoneId.systemDefault()): LocalDate =
    Instant.ofEpochMilli(ms).atZone(zona).toLocalDate()

fun List<SesionEntrenamiento>.conteoPorDia(): Map<LocalDate, Int> =
    groupBy { LocalDate.parse(it.fechaLocal) }
        .mapValues { (_, sesiones) -> sesiones.size }
