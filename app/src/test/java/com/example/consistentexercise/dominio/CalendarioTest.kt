package com.example.consistentexercise.dominio

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalendarioTest {

    private val hoy = LocalDate.of(2026, 10, 20)

    @Test
    fun alineaElPrimerDiaSegunElInicioDeSemana() {
        val celdas = calcularCeldasMes(
            mes = YearMonth.of(2026, 10),
            inicioSemana = DayOfWeek.MONDAY,
            conteoPorDia = emptyMap(),
            metaDiaria = 1,
            hoy = hoy
        )

        assertNull(celdas[0].fecha)
        assertNull(celdas[1].fecha)
        assertNull(celdas[2].fecha)
        assertEquals(LocalDate.of(2026, 10, 1), celdas[3].fecha)
        assertEquals(0, celdas.size % 7)
    }

    @Test
    fun cambiaElDesplazamientoConOtroInicioDeSemana() {
        val celdas = calcularCeldasMes(
            mes = YearMonth.of(2026, 10),
            inicioSemana = DayOfWeek.SUNDAY,
            conteoPorDia = emptyMap(),
            metaDiaria = 1,
            hoy = hoy
        )

        assertNull(celdas[3].fecha)
        assertEquals(LocalDate.of(2026, 10, 1), celdas[4].fecha)
    }

    @Test
    fun asignaEstadoSegunSesionesYMeta() {
        val celdas = calcularCeldasMes(
            mes = YearMonth.of(2026, 10),
            inicioSemana = DayOfWeek.MONDAY,
            conteoPorDia = mapOf(
                LocalDate.of(2026, 10, 19) to 1,
                LocalDate.of(2026, 10, 20) to 2
            ),
            metaDiaria = 2,
            hoy = hoy
        )

        val parcial = celdas.first { it.fecha == LocalDate.of(2026, 10, 19) }
        val cumplida = celdas.first { it.fecha == LocalDate.of(2026, 10, 20) }
        val futura = celdas.first { it.fecha == LocalDate.of(2026, 10, 21) }

        assertEquals(EstadoDia.ACTIVIDAD_PARCIAL, parcial.estado)
        assertEquals(EstadoDia.META_CUMPLIDA, cumplida.estado)
        assertEquals(EstadoDia.FUTURA, futura.estado)
        assertTrue(celdas.count { it.fecha != null } == 31)
    }
}
