package com.example.consistentexercise.dominio

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class ResumenSemanal(
    val inicio: LocalDate,
    val fin: LocalDate,
    val sesiones: Int,
    val metaSesiones: Int,
    val rachaActual: Int,
    val mejorRacha: Int
) {
    val metaCumplida: Boolean get() = sesiones >= metaSesiones
}

fun inicioDeSemana(fecha: LocalDate, inicio: DayOfWeek): LocalDate =
    fecha.with(TemporalAdjusters.previousOrSame(inicio))

fun contarEnRango(
    conteoPorDia: Map<LocalDate, Int>,
    inicio: LocalDate,
    fin: LocalDate
): Int = conteoPorDia.entries
    .filter { (fecha, _) -> !fecha.isBefore(inicio) && !fecha.isAfter(fin) }
    .sumOf { (_, sesiones) -> sesiones }

fun calcularResumenSemanal(
    conteoPorDia: Map<LocalDate, Int>,
    metaSesiones: Int,
    inicioSemana: DayOfWeek,
    hoy: LocalDate
): ResumenSemanal {
    val meta = metaSesiones.coerceAtLeast(1)
    val inicioActual = inicioDeSemana(hoy, inicioSemana)
    val finActual = inicioActual.plusDays(6)
    val sesionesActuales = contarEnRango(conteoPorDia, inicioActual, finActual)
    val sesionesPorSemana = conteoPorDia.entries
        .groupBy { (fecha, _) -> inicioDeSemana(fecha, inicioSemana) }
        .mapValues { (_, entradas) -> entradas.sumOf { (_, sesiones) -> sesiones } }
    val semanasValidas = sesionesPorSemana
        .filterValues { sesiones -> sesiones >= meta }
        .keys
        .sorted()

    return ResumenSemanal(
        inicio = inicioActual,
        fin = finActual,
        sesiones = sesionesActuales,
        metaSesiones = meta,
        rachaActual = rachaSemanalActual(semanasValidas, inicioActual),
        mejorRacha = mejorRachaSemanal(semanasValidas)
    )
}

private fun rachaSemanalActual(semanasValidas: List<LocalDate>, semanaActual: LocalDate): Int {
    val semanas = semanasValidas.toSet()
    var semana = if (semanaActual in semanas) semanaActual else semanaActual.minusWeeks(1)
    var racha = 0
    while (semana in semanas) {
        racha += 1
        semana = semana.minusWeeks(1)
    }
    return racha
}

private fun mejorRachaSemanal(semanasValidas: List<LocalDate>): Int {
    var mejor = 0
    var racha = 0
    var anterior: LocalDate? = null
    for (semana in semanasValidas) {
        racha = if (anterior != null && semana == anterior.plusWeeks(1)) racha + 1 else 1
        if (racha > mejor) mejor = racha
        anterior = semana
    }
    return mejor
}
