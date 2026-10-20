package com.example.consistentexercise.dominio

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConstanciaTest {

    private val hoy = LocalDate.of(2026, 10, 20)

    @Test
    fun sinSesionesTodoEmpiezaEnCero() {
        val resumen = calcularResumen(emptyMap(), metaSesiones = 1, hoy = hoy)

        assertEquals(0, resumen.sesionesHoy)
        assertEquals(0, resumen.rachaActual)
        assertEquals(0, resumen.mejorRacha)
        assertFalse(resumen.metaCumplidaHoy)
    }

    @Test
    fun unaSesionHoyCumpleLaMeta() {
        val resumen = calcularResumen(
            mapOf(hoy to 1),
            metaSesiones = 1,
            hoy = hoy
        )

        assertEquals(1, resumen.sesionesHoy)
        assertTrue(resumen.metaCumplidaHoy)
        assertEquals(1, resumen.rachaActual)
        assertEquals(1, resumen.mejorRacha)
    }

    @Test
    fun laMetaPuedeRequerirVariasSesiones() {
        val resumen = calcularResumen(
            mapOf(hoy to 2),
            metaSesiones = 3,
            hoy = hoy
        )

        assertEquals(2, resumen.sesionesHoy)
        assertFalse(resumen.metaCumplidaHoy)
        assertEquals(0, resumen.rachaActual)
    }

    @Test
    fun laRachaDeAyerSeMantieneSiHoyAunNoSeCumple() {
        val resumen = calcularResumen(
            mapOf(
                hoy.minusDays(1) to 1,
                hoy.minusDays(2) to 1
            ),
            metaSesiones = 1,
            hoy = hoy
        )

        assertEquals(0, resumen.sesionesHoy)
        assertEquals(2, resumen.rachaActual)
        assertEquals(2, resumen.mejorRacha)
    }

    @Test
    fun laRachaSeCortaSiAyerTampocoSeCumplio() {
        val resumen = calcularResumen(
            mapOf(hoy.minusDays(2) to 1),
            metaSesiones = 1,
            hoy = hoy
        )

        assertEquals(0, resumen.rachaActual)
        assertEquals(1, resumen.mejorRacha)
    }

    @Test
    fun laMejorRachaEsLaSecuenciaMasLarga() {
        val resumen = calcularResumen(
            mapOf(
                hoy.minusDays(10) to 1,
                hoy.minusDays(9) to 1,
                hoy.minusDays(8) to 1,
                hoy.minusDays(7) to 1,
                hoy.minusDays(3) to 1,
                hoy.minusDays(2) to 1
            ),
            metaSesiones = 1,
            hoy = hoy
        )

        assertEquals(0, resumen.rachaActual)
        assertEquals(4, resumen.mejorRacha)
    }
}
