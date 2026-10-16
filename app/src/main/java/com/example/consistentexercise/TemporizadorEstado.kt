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

class TemporizadorEstado(
    minutosIniciales: Int = 1,
    segundosIniciales: Int = 0
) {
    var minutos by mutableIntStateOf(minutosIniciales)
    var segundos by mutableIntStateOf(segundosIniciales)
    var restante by mutableIntStateOf(minutosIniciales * 60 + segundosIniciales)
    var corriendo by mutableStateOf(false)
    var tiempoAgotado by mutableStateOf(false)
    var conteo by mutableIntStateOf(0)
    var finEnMs by mutableLongStateOf(0L)

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
                    it.finEnMs
                )
            },
            restore = {
                TemporizadorEstado(it[0] as Int, it[1] as Int).apply {
                    restante = it[2] as Int
                    corriendo = it[3] as Boolean
                    tiempoAgotado = it[4] as Boolean
                    conteo = it[5] as Int
                    finEnMs = it[6] as Long
                }
            }
        )
    }
}

@Composable
fun recordarTemporizadorEstado(): TemporizadorEstado =
    rememberSaveable(saver = TemporizadorEstado.Saver) { TemporizadorEstado() }
