package com.example.consistentexercise.dominio

import java.time.DayOfWeek
import java.time.LocalDate

data class BarraSemana(
    val inicio: LocalDate,
    val sesiones: Int,
    val meta: Int?
)

data class Estadisticas(
    val constancia: ResumenConstancia,
    val semanal: ResumenSemanal?,
    val totalSesiones: Int,
    val totalMinutos: Int,
    val totalReps: Int,
    val barras: List<BarraSemana>
)

fun calcularEstadisticas(
    conteoPorDia: Map<LocalDate, Int>,
    totalDuracionSeg: Int,
    totalReps: Int,
    metaDiaria: Int,
    metaSemanalActiva: Boolean,
    metaSemanal: Int,
    inicioSemana: DayOfWeek,
    hoy: LocalDate,
    semanasGrafico: Int = 8
): Estadisticas {
    val inicioSemanaActual = inicioDeSemana(hoy, inicioSemana)
    val metaSemanalEfectiva = if (metaSemanalActiva) metaSemanal.coerceAtLeast(1) else null
    val barras = (semanasGrafico - 1 downTo 0).map { semanasAtras ->
        val inicio = inicioSemanaActual.minusWeeks(semanasAtras.toLong())
        BarraSemana(
            inicio = inicio,
            sesiones = contarEnRango(conteoPorDia, inicio, inicio.plusDays(6)),
            meta = metaSemanalEfectiva
        )
    }

    return Estadisticas(
        constancia = calcularResumen(conteoPorDia, metaDiaria, hoy),
        semanal = if (metaSemanalActiva) {
            calcularResumenSemanal(conteoPorDia, metaSemanal, inicioSemana, hoy)
        } else {
            null
        },
        totalSesiones = conteoPorDia.values.sum(),
        totalMinutos = if (totalDuracionSeg <= 0) 0 else (totalDuracionSeg + 59) / 60,
        totalReps = totalReps,
        barras = barras
    )
}
