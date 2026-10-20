package com.example.consistentexercise.dominio

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

enum class EstadoDia { META_CUMPLIDA, ACTIVIDAD_PARCIAL, SIN_ACTIVIDAD, FUTURA }

data class CeldaCalendario(
    val fecha: LocalDate?,
    val sesiones: Int,
    val estado: EstadoDia
)

fun calcularCeldasMes(
    mes: YearMonth,
    inicioSemana: DayOfWeek,
    conteoPorDia: Map<LocalDate, Int>,
    metaDiaria: Int,
    hoy: LocalDate
): List<CeldaCalendario> {
    val meta = metaDiaria.coerceAtLeast(1)
    val primerDia = mes.atDay(1)
    val diasEnMes = mes.lengthOfMonth()
    val desplazamiento = (primerDia.dayOfWeek.value - inicioSemana.value + 7) % 7
    val totalCeldas = ((desplazamiento + diasEnMes + 6) / 7) * 7

    return List(totalCeldas) { indice ->
        val diaDelMes = indice - desplazamiento + 1
        if (diaDelMes < 1 || diaDelMes > diasEnMes) {
            CeldaCalendario(fecha = null, sesiones = 0, estado = EstadoDia.SIN_ACTIVIDAD)
        } else {
            val fecha = mes.atDay(diaDelMes)
            val sesiones = conteoPorDia[fecha] ?: 0
            CeldaCalendario(
                fecha = fecha,
                sesiones = sesiones,
                estado = when {
                    fecha.isAfter(hoy) -> EstadoDia.FUTURA
                    sesiones <= 0 -> EstadoDia.SIN_ACTIVIDAD
                    sesiones >= meta -> EstadoDia.META_CUMPLIDA
                    else -> EstadoDia.ACTIVIDAD_PARCIAL
                }
            )
        }
    }
}
