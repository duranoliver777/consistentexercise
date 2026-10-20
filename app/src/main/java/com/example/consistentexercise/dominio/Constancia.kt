package com.example.consistentexercise.dominio

import java.time.LocalDate

data class ResumenConstancia(
    val sesionesHoy: Int,
    val metaSesiones: Int,
    val rachaActual: Int,
    val mejorRacha: Int
) {
    val metaCumplidaHoy: Boolean get() = sesionesHoy >= metaSesiones
}

fun calcularResumen(
    conteoPorDia: Map<LocalDate, Int>,
    metaSesiones: Int,
    hoy: LocalDate
): ResumenConstancia {
    val meta = metaSesiones.coerceAtLeast(1)
    val diasValidos = conteoPorDia
        .filterValues { sesiones -> sesiones >= meta }
        .keys
        .sorted()

    return ResumenConstancia(
        sesionesHoy = conteoPorDia[hoy] ?: 0,
        metaSesiones = meta,
        rachaActual = calcularRachaActual(diasValidos, hoy),
        mejorRacha = calcularMejorRacha(diasValidos)
    )
}

private fun calcularRachaActual(diasValidos: List<LocalDate>, hoy: LocalDate): Int {
    val dias = diasValidos.toSet()
    var dia = if (hoy in dias) hoy else hoy.minusDays(1)
    var racha = 0
    while (dia in dias) {
        racha += 1
        dia = dia.minusDays(1)
    }
    return racha
}

private fun calcularMejorRacha(diasValidos: List<LocalDate>): Int {
    var mejor = 0
    var racha = 0
    var anterior: LocalDate? = null
    for (dia in diasValidos) {
        racha = if (anterior != null && dia == anterior.plusDays(1)) racha + 1 else 1
        if (racha > mejor) mejor = racha
        anterior = dia
    }
    return mejor
}
