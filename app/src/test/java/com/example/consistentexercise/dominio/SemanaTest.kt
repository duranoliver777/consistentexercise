package com.example.consistentexercise.dominio

import java.time.DayOfWeek
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SemanaTest {

    private val lunes = LocalDate.of(2026, 10, 19)
    private val hoy = LocalDate.of(2026, 10, 20)

    @Test
    fun calculaElInicioDeSemanaSegunElDiaConfigurado() {
        assertEquals(lunes, inicioDeSemana(hoy, DayOfWeek.MONDAY))
        assertEquals(
            LocalDate.of(2026, 10, 18),
            inicioDeSemana(hoy, DayOfWeek.SUNDAY)
        )
    }

    @Test
    fun laSemanaActualCuentaTodasLasSesionesDesdeSuInicio() {
        val resumen = calcularResumenSemanal(
            conteoPorDia = mapOf(lunes to 1, hoy to 1),
            metaSesiones = 2,
            inicioSemana = DayOfWeek.MONDAY,
            hoy = hoy
        )

        assertEquals(2, resumen.sesiones)
        assertTrue(resumen.metaCumplida)
        assertEquals(1, resumen.rachaActual)
    }

    @Test
    fun laActividadPreviaCuentaAunqueLaMetaSeActiveAMitadDeSemana() {
        val resumen = calcularResumenSemanal(
            conteoPorDia = mapOf(lunes to 1, hoy to 1),
            metaSesiones = 2,
            inicioSemana = DayOfWeek.MONDAY,
            hoy = hoy
        )

        assertEquals(lunes, resumen.inicio)
        assertEquals(2, resumen.sesiones)
        assertEquals(1, resumen.rachaActual)
    }

    @Test
    fun laSemanaEnCursoNoRompLaRachaHastaQueTermina() {
        val semanaPasada = lunes.minusWeeks(1)
        val resumen = calcularResumenSemanal(
            conteoPorDia = mapOf(
                semanaPasada to 3,
                hoy to 1
            ),
            metaSesiones = 3,
            inicioSemana = DayOfWeek.MONDAY,
            hoy = hoy
        )

        assertFalse(resumen.metaCumplida)
        assertEquals(1, resumen.rachaActual)
        assertEquals(1, resumen.mejorRacha)
    }

    @Test
    fun calculaLaMejorRachaSemanal() {
        val resumen = calcularResumenSemanal(
            conteoPorDia = mapOf(
                lunes.minusWeeks(3) to 3,
                lunes.minusWeeks(2) to 3,
                lunes.minusWeeks(1) to 3,
                hoy to 1
            ),
            metaSesiones = 3,
            inicioSemana = DayOfWeek.MONDAY,
            hoy = hoy
        )

        assertEquals(3, resumen.mejorRacha)
        assertEquals(3, resumen.rachaActual)
    }
}
