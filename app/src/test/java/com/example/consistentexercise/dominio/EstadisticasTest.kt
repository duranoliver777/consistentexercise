package com.example.consistentexercise.dominio

import java.time.DayOfWeek
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EstadisticasTest {

    private val hoy = LocalDate.of(2026, 10, 20)
    private val lunes = LocalDate.of(2026, 10, 19)

    @Test
    fun calculaLosTotalesYLasBarrasSemanales() {
        val estadisticas = calcularEstadisticas(
            conteoPorDia = mapOf(
                lunes to 1,
                hoy to 2,
                lunes.minusWeeks(1) to 1
            ),
            totalDuracionSeg = 300,
            totalReps = 40,
            metaDiaria = 1,
            metaSemanalActiva = true,
            metaSemanal = 3,
            inicioSemana = DayOfWeek.MONDAY,
            hoy = hoy
        )

        assertEquals(4, estadisticas.totalSesiones)
        assertEquals(5, estadisticas.totalMinutos)
        assertEquals(40, estadisticas.totalReps)
        assertEquals(8, estadisticas.barras.size)
        assertEquals(3, estadisticas.barras.last().sesiones)
        assertEquals(3, estadisticas.semanal?.metaSesiones)
        assertEquals(1, estadisticas.semanal?.rachaActual)
    }

    @Test
    fun noCalculaResumenSemanalSiEstaDesactivado() {
        val estadisticas = calcularEstadisticas(
            conteoPorDia = mapOf(hoy to 1),
            totalDuracionSeg = 60,
            totalReps = 0,
            metaDiaria = 1,
            metaSemanalActiva = false,
            metaSemanal = 3,
            inicioSemana = DayOfWeek.MONDAY,
            hoy = hoy
        )

        assertNull(estadisticas.semanal)
        assertEquals(0, estadisticas.barras.last().meta ?: 0)
    }

    @Test
    fun laUltimaBarraEsLaSemanaActual() {
        val estadisticas = calcularEstadisticas(
            conteoPorDia = mapOf(hoy to 2),
            totalDuracionSeg = 120,
            totalReps = 10,
            metaDiaria = 1,
            metaSemanalActiva = true,
            metaSemanal = 2,
            inicioSemana = DayOfWeek.MONDAY,
            hoy = hoy
        )

        assertEquals(lunes, estadisticas.barras.last().inicio)
        assertEquals(2, estadisticas.barras.last().sesiones)
    }
}
