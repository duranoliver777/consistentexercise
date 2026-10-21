package com.example.consistentexercise

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TemporizadorEstadoTest {

    @Test
    fun sinIntervalosFinalizaAlLlegarACero() {
        val estado = TemporizadorEstado(minutosIniciales = 1, segundosIniciales = 0)

        estado.iniciar(0)
        estado.avanzar(60_000)

        assertFalse(estado.corriendo)
        assertTrue(estado.tiempoAgotado)
        assertEquals(0, estado.restante)
    }

    @Test
    fun alTerminarEjercicioPasaAPausa() {
        val estado = TemporizadorEstado(
            minutosIniciales = 1,
            segundosIniciales = 0,
            pausaInicial = 30,
            rondasIniciales = 2,
            intervalosIniciales = true
        )

        estado.iniciar(1_000)
        estado.avanzar(61_000)

        assertEquals(FaseTemporizador.PAUSA, estado.fase)
        assertEquals(1, estado.ronda)
        assertEquals(30, estado.restante)
        assertEquals(1, estado.cambioFase)
        assertTrue(estado.corriendo)
    }

    @Test
    fun alTerminarPausaEmpiezaSiguienteRonda() {
        val estado = TemporizadorEstado(
            minutosIniciales = 1,
            segundosIniciales = 0,
            pausaInicial = 30,
            rondasIniciales = 2,
            intervalosIniciales = true
        )

        estado.iniciar(0)
        estado.avanzar(60_000)
        estado.avanzar(90_000)

        assertEquals(FaseTemporizador.EJERCICIO, estado.fase)
        assertEquals(2, estado.ronda)
        assertEquals(60, estado.restante)
        assertEquals(2, estado.cambioFase)
        assertTrue(estado.corriendo)
    }

    @Test
    fun laUltimaRondaFinalizaSinPausaExtra() {
        val estado = TemporizadorEstado(
            minutosIniciales = 1,
            segundosIniciales = 0,
            pausaInicial = 30,
            rondasIniciales = 2,
            intervalosIniciales = true
        )

        estado.iniciar(0)
        estado.avanzar(60_000)
        estado.avanzar(90_000)
        estado.avanzar(150_000)

        assertFalse(estado.corriendo)
        assertTrue(estado.tiempoAgotado)
        assertEquals(0, estado.restante)
        assertEquals(FaseTemporizador.EJERCICIO, estado.fase)
        assertEquals(2, estado.ronda)
    }

    @Test
    fun pausaCeroEncadenaLasRondas() {
        val estado = TemporizadorEstado(
            minutosIniciales = 1,
            segundosIniciales = 0,
            pausaInicial = 0,
            rondasIniciales = 3,
            intervalosIniciales = true
        )

        estado.iniciar(0)
        estado.avanzar(60_000)

        assertEquals(FaseTemporizador.EJERCICIO, estado.fase)
        assertEquals(2, estado.ronda)
        assertEquals(60, estado.restante)
        assertEquals(1, estado.cambioFase)

        estado.avanzar(120_000)
        assertEquals(3, estado.ronda)

        estado.avanzar(180_000)
        assertTrue(estado.tiempoAgotado)
        assertEquals(3, estado.ronda)
    }

    @Test
    fun detenerConservaElTiempoRestante() {
        val estado = TemporizadorEstado(minutosIniciales = 1, segundosIniciales = 0)

        estado.iniciar(0)
        estado.detener(15_000)

        assertFalse(estado.corriendo)
        assertEquals(45, estado.restante)
        assertEquals(0L, estado.finEnMs)
    }

    @Test
    fun aplicarAjustesReiniciaYConservaElConteo() {
        val estado = TemporizadorEstado()
        estado.conteo = 4
        estado.iniciar(0)

        val ajustes = Ajustes(
            duracionMinutos = 2,
            duracionSegundos = 0,
            intervalosActivos = true,
            pausaMinutos = 0,
            pausaSegundos = 15,
            rondas = 5
        )
        estado.aplicarAjustes(ajustes, 1_000)

        assertEquals(2, estado.minutos)
        assertEquals(0, estado.segundos)
        assertEquals(15, estado.pausaSeg)
        assertEquals(5, estado.rondasTotales)
        assertTrue(estado.intervalosActivos)
        assertEquals(1, estado.ronda)
        assertEquals(FaseTemporizador.EJERCICIO, estado.fase)
        assertEquals(120, estado.restante)
        assertEquals(4, estado.conteo)
        assertTrue(estado.corriendo)
    }

    @Test
    fun aplicarAjustesConElTemporizadorDetenidoNoLoArranca() {
        val estado = TemporizadorEstado()
        estado.conteo = 2

        estado.aplicarAjustes(Ajustes(duracionMinutos = 3), 0)

        assertFalse(estado.corriendo)
        assertEquals(180, estado.restante)
        assertEquals(2, estado.conteo)
    }

    @Test
    fun autoIniciarSoloArrancaUnaSesionSinEmpezar() {
        val estado = TemporizadorEstado(minutosIniciales = 1, segundosIniciales = 0)

        estado.iniciarSiProcede(autoIniciar = true, ahoraMs = 0)
        assertTrue(estado.corriendo)

        estado.detener(10_000)
        estado.iniciarSiProcede(autoIniciar = true, ahoraMs = 20_000)
        assertFalse(estado.corriendo)

        estado.reiniciar()
        estado.iniciarSiProcede(autoIniciar = false, ahoraMs = 30_000)
        assertFalse(estado.corriendo)
    }

    @Test
    fun iniciarGeneraUnaSesionConIdEInicio() {
        val estado = TemporizadorEstado(minutosIniciales = 1, segundosIniciales = 0)

        estado.iniciar(ahoraMs = 1_000, ahoraParedMs = 10_000)

        assertTrue(estado.sesionIniciada)
        assertTrue(estado.idSesion.isNotBlank())
        assertEquals(10_000L, estado.inicioMs)
    }

    @Test
    fun reanudarConservaLaMismaSesion() {
        val estado = TemporizadorEstado(minutosIniciales = 1, segundosIniciales = 0)

        estado.iniciar(ahoraMs = 0, ahoraParedMs = 5_000)
        val id = estado.idSesion
        estado.detener(ahoraMs = 10_000)
        estado.iniciar(ahoraMs = 20_000, ahoraParedMs = 30_000)

        assertEquals(id, estado.idSesion)
        assertEquals(5_000L, estado.inicioMs)
    }

    @Test
    fun reiniciarDescartaLaSesionActual() {
        val estado = TemporizadorEstado(minutosIniciales = 1, segundosIniciales = 0)

        estado.iniciar(ahoraMs = 0, ahoraParedMs = 5_000)
        estado.reiniciar()

        assertFalse(estado.sesionIniciada)
        assertEquals("", estado.idSesion)
        assertEquals(0L, estado.inicioMs)
    }

    @Test
    fun completarYVolverAIniciarGeneraOtraSesion() {
        val estado = TemporizadorEstado(minutosIniciales = 1, segundosIniciales = 0)

        estado.iniciar(ahoraMs = 0, ahoraParedMs = 5_000)
        val primeraSesion = estado.idSesion
        estado.avanzar(60_000)

        estado.iniciar(ahoraMs = 100_000, ahoraParedMs = 200_000)

        assertTrue(estado.idSesion.isNotBlank())
        assertTrue(estado.idSesion != primeraSesion)
        assertEquals(200_000L, estado.inicioMs)
    }

    @Test
    fun laAlarmaConservaElConteoYSeReiniciaAlEmpezarOtraSesion() {
        val estado = TemporizadorEstado(minutosIniciales = 1, segundosIniciales = 0)
        estado.conteo = 7

        estado.iniciar(ahoraMs = 0, ahoraParedMs = 5_000)
        estado.avanzar(60_000)

        assertTrue(estado.tiempoAgotado)
        assertEquals(7, estado.conteo)

        estado.iniciar(ahoraMs = 100_000, ahoraParedMs = 200_000)

        assertEquals(0, estado.conteo)
        assertTrue(estado.corriendo)
    }

    @Test
    fun reiniciarTrasLaAlarmaMantieneElConteoHastaEmpezarDeNuevo() {
        val estado = TemporizadorEstado(minutosIniciales = 1, segundosIniciales = 0)
        estado.conteo = 5

        estado.iniciar(ahoraMs = 0, ahoraParedMs = 5_000)
        estado.avanzar(60_000)
        estado.reiniciar()

        assertEquals(5, estado.conteo)

        estado.iniciar(ahoraMs = 100_000, ahoraParedMs = 200_000)

        assertEquals(0, estado.conteo)
    }
}
