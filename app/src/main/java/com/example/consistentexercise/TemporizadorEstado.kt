package com.example.consistentexercise

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import java.util.UUID
import kotlin.math.ceil

enum class FaseTemporizador { EJERCICIO, PAUSA }

class TemporizadorEstado(
    minutosIniciales: Int = 1,
    segundosIniciales: Int = 0,
    pausaInicial: Int = 30,
    rondasIniciales: Int = 3,
    intervalosIniciales: Boolean = false
) {
    var minutos by mutableIntStateOf(minutosIniciales)
    var segundos by mutableIntStateOf(segundosIniciales)
    var restante by mutableIntStateOf(minutosIniciales * 60 + segundosIniciales)
    var corriendo by mutableStateOf(false)
    var tiempoAgotado by mutableStateOf(false)
    var conteo by mutableIntStateOf(0)
    var finEnMs by mutableLongStateOf(0L)
    var fase by mutableStateOf(FaseTemporizador.EJERCICIO)
    var ronda by mutableIntStateOf(1)
    var rondasTotales by mutableIntStateOf(rondasIniciales)
    var intervalosActivos by mutableStateOf(intervalosIniciales)
    var pausaSeg by mutableIntStateOf(pausaInicial)
    var cambioFase by mutableIntStateOf(0)
    var sesionIniciada by mutableStateOf(false)
    var idSesion by mutableStateOf("")
    var inicioMs by mutableLongStateOf(0L)

    val duracionEjercicioSeg: Int get() = minutos * 60 + segundos

    fun iniciar(ahoraMs: Long, ahoraParedMs: Long = System.currentTimeMillis()) {
        if (corriendo) return
        if (tiempoAgotado || restante <= 0) reiniciar()
        if (duracionEjercicioSeg <= 0 && fase == FaseTemporizador.EJERCICIO) return
        if (restante <= 0) return
        if (!sesionIniciada) {
            idSesion = UUID.randomUUID().toString()
            inicioMs = ahoraParedMs
            sesionIniciada = true
        }
        finEnMs = ahoraMs + restante * 1000L
        corriendo = true
    }

    fun detener(ahoraMs: Long) {
        if (!corriendo) return
        avanzar(ahoraMs)
        if (!corriendo) return
        restante = ceil((finEnMs - ahoraMs) / 1000.0).toInt().coerceAtLeast(0)
        corriendo = false
        finEnMs = 0L
    }

    fun reiniciar() {
        corriendo = false
        finEnMs = 0L
        tiempoAgotado = false
        fase = FaseTemporizador.EJERCICIO
        ronda = 1
        restante = duracionEjercicioSeg
        cambioFase = 0
        sesionIniciada = false
        idSesion = ""
        inicioMs = 0L
    }

    fun iniciarSiProcede(
        autoIniciar: Boolean,
        ahoraMs: Long,
        ahoraParedMs: Long = System.currentTimeMillis()
    ) {
        val sesionSinEmpezar = !corriendo &&
            !tiempoAgotado &&
            !sesionIniciada &&
            ronda == 1 &&
            fase == FaseTemporizador.EJERCICIO &&
            restante == duracionEjercicioSeg
        if (autoIniciar && sesionSinEmpezar && duracionEjercicioSeg > 0) {
            iniciar(ahoraMs, ahoraParedMs)
        }
    }

    fun aplicarAjustes(
        ajustes: Ajustes,
        ahoraMs: Long,
        ahoraParedMs: Long = System.currentTimeMillis()
    ) {
        val estabaCorriendo = corriendo
        minutos = ajustes.duracionMinutos
        segundos = ajustes.duracionSegundos
        pausaSeg = ajustes.pausaSeg
        rondasTotales = ajustes.rondas
        intervalosActivos = ajustes.intervalosActivos
        reiniciar()
        if (estabaCorriendo && duracionEjercicioSeg > 0) iniciar(ahoraMs, ahoraParedMs)
    }

    fun avanzar(ahoraMs: Long) {
        while (corriendo && ahoraMs >= finEnMs) {
            val finFase = finEnMs
            if (!intervalosActivos) {
                finalizar()
                return
            }
            when (fase) {
                FaseTemporizador.EJERCICIO -> {
                    val siguienteEjercicio = duracionEjercicioSeg
                    if (ronda >= rondasTotales) {
                        finalizar()
                        return
                    }
                    if (siguienteEjercicio <= 0) {
                        finalizar()
                        return
                    }
                    if (pausaSeg > 0) {
                        fase = FaseTemporizador.PAUSA
                        restante = pausaSeg
                        finEnMs = finFase + pausaSeg * 1000L
                    } else {
                        ronda += 1
                        fase = FaseTemporizador.EJERCICIO
                        restante = siguienteEjercicio
                        finEnMs = finFase + siguienteEjercicio * 1000L
                    }
                }
                FaseTemporizador.PAUSA -> {
                    val siguienteEjercicio = duracionEjercicioSeg
                    if (siguienteEjercicio <= 0) {
                        finalizar()
                        return
                    }
                    ronda += 1
                    fase = FaseTemporizador.EJERCICIO
                    restante = siguienteEjercicio
                    finEnMs = finFase + siguienteEjercicio * 1000L
                }
            }
            cambioFase += 1
            restante = ceil((finEnMs - ahoraMs) / 1000.0).toInt().coerceAtLeast(0)
        }
        if (corriendo) {
            restante = ceil((finEnMs - ahoraMs) / 1000.0).toInt().coerceAtLeast(0)
        }
    }

    private fun finalizar() {
        restante = 0
        corriendo = false
        finEnMs = 0L
        tiempoAgotado = true
    }

    companion object {
        val Saver: Saver<TemporizadorEstado, Any> = listSaver(
            save = {
                listOf(
                    it.minutos,
                    it.segundos,
                    it.restante,
                    it.corriendo,
                    it.tiempoAgotado,
                    it.conteo,
                    it.finEnMs,
                    it.fase.name,
                    it.ronda,
                    it.rondasTotales,
                    it.intervalosActivos,
                    it.pausaSeg,
                    it.cambioFase,
                    it.sesionIniciada,
                    it.idSesion,
                    it.inicioMs
                )
            },
            restore = {
                TemporizadorEstado(
                    minutosIniciales = it[0] as Int,
                    segundosIniciales = it[1] as Int,
                    pausaInicial = it[11] as Int,
                    rondasIniciales = it[9] as Int,
                    intervalosIniciales = it[10] as Boolean
                ).apply {
                    restante = it[2] as Int
                    corriendo = it[3] as Boolean
                    tiempoAgotado = it[4] as Boolean
                    conteo = it[5] as Int
                    finEnMs = it[6] as Long
                    fase = FaseTemporizador.valueOf(it[7] as String)
                    ronda = it[8] as Int
                    cambioFase = 0
                    sesionIniciada = it[13] as Boolean
                    idSesion = it[14] as String
                    inicioMs = it[15] as Long
                }
            }
        )
    }
}

@Composable
fun recordarTemporizadorEstado(): TemporizadorEstado =
    rememberSaveable(saver = TemporizadorEstado.Saver) { TemporizadorEstado() }
