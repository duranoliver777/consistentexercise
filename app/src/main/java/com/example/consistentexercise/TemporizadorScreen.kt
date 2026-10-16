package com.example.consistentexercise

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.consistentexercise.ui.AzulBoton
import com.example.consistentexercise.ui.AzulClaro
import com.example.consistentexercise.ui.PantallaConBorde
import com.example.consistentexercise.ui.theme.ConsistentExerciseTheme
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlin.math.PI
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

@Composable
fun TemporizadorScreen(
    onAbrirConfiguracion: () -> Unit,
    modifier: Modifier = Modifier
) {
    var minutos by rememberSaveable { mutableIntStateOf(1) }
    var segundos by rememberSaveable { mutableIntStateOf(0) }
    var restante by rememberSaveable { mutableIntStateOf(minutos * 60 + segundos) }
    var corriendo by rememberSaveable { mutableStateOf(false) }
    var tiempoAgotado by rememberSaveable { mutableStateOf(false) }
    var conteo by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(tiempoAgotado) {
        if (!tiempoAgotado) return@LaunchedEffect
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

    LaunchedEffect(corriendo, restante) {
        if (corriendo && restante > 0) {
            delay(1000)
            restante -= 1
            if (restante == 0) {
                corriendo = false
                tiempoAgotado = true
            }
        }
    }

    PantallaConBorde(modifier = modifier) {
        IconButton(
            onClick = onAbrirConfiguracion,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 20.dp, end = 20.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "Configuración",
                tint = Color.Black
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
            ) {
                Text(
                    text = "%02d:%02d".format(restante / 60, restante % 60),
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                AjusteTiempo(
                    titulo = "Minutos",
                    valor = minutos,
                    max = 99,
                    onCambiar = { nuevo ->
                        minutos = nuevo
                        if (!corriendo) restante = nuevo * 60 + segundos
                    }
                )
                AjusteTiempo(
                    titulo = "Segundos",
                    valor = segundos,
                    max = 59,
                    onCambiar = { nuevo ->
                        segundos = nuevo
                        if (!corriendo) restante = minutos * 60 + nuevo
                    }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = {
                            if (restante == 0) {
                                val total = minutos * 60 + segundos
                                if (total > 0) {
                                    restante = total
                                    corriendo = true
                                }
                            } else {
                                corriendo = !corriendo
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AzulBoton,
                            contentColor = Color.White
                        )
                    ) {
                        Text(text = if (corriendo) "Detener" else "Iniciar", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = {
                            corriendo = false
                            restante = minutos * 60 + segundos
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                        border = BorderStroke(width = 2.dp, color = AzulClaro)
                    ) {
                        Text(text = "Reiniciar", fontSize = 12.sp)
                    }
                }
            }
            SeccionTally(
                conteo = conteo,
                onCambiar = { conteo = (conteo + it).coerceAtLeast(0) }
            )
        }
    }

    if (tiempoAgotado) {
        AlertDialog(
            onDismissRequest = { tiempoAgotado = false },
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
                    onClick = { tiempoAgotado = false },
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SeccionTally(
    conteo: Int,
    onCambiar: (Int) -> Unit
) {
    val grupos = buildList {
        repeat(conteo / 5) { add(5) }
        if (conteo % 5 != 0) add(conteo % 5)
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Conteo: $conteo",
            color = Color.Black,
            fontWeight = FontWeight.Bold
        )
        if (grupos.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                grupos.forEach { GrupoTally(cantidad = it) }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { onCambiar(-1) },
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(72.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AzulBoton,
                    contentColor = Color.White
                )
            ) {
                Text(text = "-", fontSize = 18.sp)
            }
            Text(text = "%d".format(conteo), fontSize = 18.sp, color = Color.Black)
            Button(
                onClick = { onCambiar(1) },
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(72.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AzulBoton,
                    contentColor = Color.White
                )
            ) {
                Text(text = "+", fontSize = 18.sp)
            }
        }
    }
}

@Composable
private fun GrupoTally(
    cantidad: Int,
    color: Color = AzulBoton
) {
    if (cantidad <= 0) return
    val lineas = if (cantidad >= 5) 4 else cantidad
    val grosor: Dp = 3.dp
    val separacion: Dp = 7.dp
    val altura: Dp = 30.dp
    val ancho: Dp = separacion * (lineas - 1) + grosor
    Canvas(modifier = Modifier.size(width = ancho, height = altura)) {
        val sw = grosor.toPx()
        val sep = separacion.toPx()
        val alto = size.height
        repeat(lineas) { i ->
            val x = sw / 2 + i * sep
            drawLine(
                color = color,
                start = Offset(x, 0f),
                end = Offset(x, alto),
                strokeWidth = sw,
                cap = StrokeCap.Round
            )
        }
        if (cantidad >= 5) {
            drawLine(
                color = color,
                start = Offset(0f, alto),
                end = Offset(size.width, 0f),
                strokeWidth = sw,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun AjusteTiempo(
    titulo: String,
    valor: Int,
    max: Int,
    onCambiar: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = titulo, color = Color.Black)
        Button(
            onClick = { onCambiar((valor - 1).coerceAtLeast(0)) },
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.size(36.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AzulBoton,
                contentColor = Color.White
            )
        ) {
            Text(text = "-", fontSize = 18.sp)
        }
        Text(text = "%02d".format(valor), fontSize = 18.sp, color = Color.Black)
        Button(
            onClick = { onCambiar((valor + 1).coerceAtMost(max)) },
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.size(36.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AzulBoton,
                contentColor = Color.White
            )
        ) {
            Text(text = "+", fontSize = 18.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TemporizadorPreview() {
    ConsistentExerciseTheme {
        TemporizadorScreen(onAbrirConfiguracion = {})
    }
}
