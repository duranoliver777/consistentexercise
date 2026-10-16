package com.example.consistentexercise

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.consistentexercise.ui.AzulBoton
import com.example.consistentexercise.ui.theme.ConsistentExerciseTheme
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.sin

private const val FRECUENCIA_MUESTREO = 44100

private fun generarSonidoDespertador(): ShortArray {
    val frecuencia = 1300
    val msBip = 50
    val msSilencio = 50
    val msInactividad = 900
    val msFade = 12
    val msCiclo = 3 * msBip + 2 * msSilencio + msInactividad
    val totalMuestras = FRECUENCIA_MUESTREO * msCiclo / 1000
    val buffer = ShortArray(totalMuestras)
    val finBips = 4 * msBip + 2 * msSilencio
    for (i in 0 until totalMuestras) {
        val ms = (i * 1000L / FRECUENCIA_MUESTREO).toInt()
        if (ms < finBips) {
            val msEnBip = ms % (msBip + msSilencio)
            if (msEnBip < msBip) {
                val envolvente = when {
                    msEnBip < msFade -> msEnBip.toDouble() / msFade
                    msEnBip > msBip - msFade -> (msBip - msEnBip).toDouble() / msFade
                    else -> 1.0
                }
                val t = i.toDouble() / FRECUENCIA_MUESTREO
                val muestra = sin(2.0 * PI * frecuencia * t) * 0.5 * envolvente
                buffer[i] = (muestra * Short.MAX_VALUE).toInt().toShort()
            }
        }
    }
    return buffer
}

private enum class Pantalla { BIENVENIDA, TEMPORIZADOR, CONFIGURACION }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ConsistentExerciseTheme {
                AppConsistentExercise()
            }
        }
    }
}

@Composable
fun AppConsistentExercise() {
    var pantalla by rememberSaveable { mutableStateOf(Pantalla.BIENVENIDA) }
    var pantallaAnterior by rememberSaveable { mutableStateOf(Pantalla.BIENVENIDA) }
    val stateHolder = rememberSaveableStateHolder()
    val temporizador = recordarTemporizadorEstado()

    EfectosTemporizador(temporizador)

    BackHandler(enabled = pantalla != Pantalla.BIENVENIDA) {
        pantalla = if (pantalla == Pantalla.CONFIGURACION) pantallaAnterior else Pantalla.BIENVENIDA
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        stateHolder.SaveableStateProvider(pantalla) {
            val contenidoModifier = Modifier.padding(innerPadding)
            when (pantalla) {
                Pantalla.BIENVENIDA -> BienvenidaScreen(
                    onContinuar = { pantalla = Pantalla.TEMPORIZADOR },
                    onAbrirConfiguracion = {
                        pantallaAnterior = pantalla
                        pantalla = Pantalla.CONFIGURACION
                    },
                    modifier = contenidoModifier
                )
                Pantalla.TEMPORIZADOR -> TemporizadorScreen(
                    estado = temporizador,
                    onAbrirConfiguracion = {
                        pantallaAnterior = pantalla
                        pantalla = Pantalla.CONFIGURACION
                    },
                    modifier = contenidoModifier
                )
                Pantalla.CONFIGURACION -> ConfiguracionScreen(
                    onVolver = { pantalla = pantallaAnterior },
                    modifier = contenidoModifier
                )
            }
        }
    }

    if (temporizador.tiempoAgotado) {
        AlertDialog(
            onDismissRequest = { temporizador.tiempoAgotado = false },
            containerColor = Color.White,
            title = {
                Text(
                    text = "Tiempo agotado",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            },
            confirmButton = {
                Button(
                    onClick = { temporizador.tiempoAgotado = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AzulBoton,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "Aceptar", fontSize = 12.sp)
                }
            }
        )
    }
}

@Composable
private fun EfectosTemporizador(estado: TemporizadorEstado) {
    LaunchedEffect(estado.corriendo, estado.finEnMs) {
        if (!estado.corriendo || estado.finEnMs == 0L) return@LaunchedEffect
        while (true) {
            val quedanMs = estado.finEnMs - SystemClock.elapsedRealtime()
            if (quedanMs <= 0L) {
                estado.restante = 0
                estado.corriendo = false
                estado.tiempoAgotado = true
                break
            }
            estado.restante = ceil(quedanMs / 1000.0).toInt()
            delay(200)
        }
    }

    LaunchedEffect(estado.tiempoAgotado) {
        if (!estado.tiempoAgotado) return@LaunchedEffect
        val pcm = generarSonidoDespertador()
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(FRECUENCIA_MUESTREO)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(pcm.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        track.write(pcm, 0, pcm.size)
        track.setLoopPoints(0, pcm.size, -1)
        track.play()
        try {
            awaitCancellation()
        } finally {
            track.stop()
            track.release()
        }
    }
}
