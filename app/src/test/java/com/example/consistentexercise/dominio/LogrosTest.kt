package com.example.consistentexercise.dominio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LogrosTest {

    @Test
    fun desbloqueaElPrimerLogroConUnaSesion() {
        val logros = calcularLogros(
            totalSesiones = 1,
            mejorRachaDiaria = 1,
            mejorRachaSemanal = 0,
            metaSemanalActiva = false,
            desbloqueados = emptySet()
        )

        val primero = logros.first { it.id == "primera_sesion" }
        val diez = logros.first { it.id == "diez_sesiones" }

        assertTrue(primero.desbloqueado)
        assertFalse(diez.desbloqueado)
        assertEquals(1, primero.progreso)
        assertEquals(10, diez.objetivo)
    }

    @Test
    fun incluyeLogrosSemanalesSoloConLaMetaActiva() {
        val sinSemanal = calcularLogros(
            totalSesiones = 5,
            mejorRachaDiaria = 2,
            mejorRachaSemanal = 4,
            metaSemanalActiva = false,
            desbloqueados = emptySet()
        )
        val conSemanal = calcularLogros(
            totalSesiones = 5,
            mejorRachaDiaria = 2,
            mejorRachaSemanal = 4,
            metaSemanalActiva = true,
            desbloqueados = emptySet()
        )

        assertFalse(sinSemanal.any { it.id == "semanas_4" })
        assertTrue(conSemanal.any { it.id == "semanas_4" && it.desbloqueado })
    }

    @Test
    fun mantieneDesbloqueadoUnLogroPersistido() {
        val logros = calcularLogros(
            totalSesiones = 1,
            mejorRachaDiaria = 1,
            mejorRachaSemanal = 0,
            metaSemanalActiva = false,
            desbloqueados = setOf("racha_7")
        )

        val racha = logros.first { it.id == "racha_7" }

        assertTrue(racha.desbloqueado)
        assertEquals(1f, racha.fraccion)
    }
}
