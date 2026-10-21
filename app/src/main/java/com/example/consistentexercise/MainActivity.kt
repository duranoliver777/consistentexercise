package com.example.consistentexercise

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.consistentexercise.datos.BaseDatos
import com.example.consistentexercise.datos.LogrosRepositorio
import com.example.consistentexercise.datos.SesionEntrenamiento
import com.example.consistentexercise.datos.SesionRepositorio
import com.example.consistentexercise.datos.conteoPorDia
import com.example.consistentexercise.datos.fechaLocalDe
import com.example.consistentexercise.dominio.calcularEstadisticas
import com.example.consistentexercise.dominio.calcularLogros
import com.example.consistentexercise.ui.AzulBoton
import com.example.consistentexercise.ui.theme.ConsistentExerciseTheme
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

private fun crearAudioTrackAlarma(): AudioTrack {
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
    return track
}

private fun crearRingtone(contexto: Context, tonoUri: String?): Ringtone? {
    val candidatos = listOfNotNull(
        tonoUri?.let(Uri::parse),
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
    ).distinct()
    for (uri in candidatos) {
        val ringtone = runCatching {
            RingtoneManager.getRingtone(contexto, uri)
        }.getOrNull() ?: continue
        ringtone.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        ringtone.isLooping = true
        return ringtone
    }
    return null
}

private fun obtenerVibrador(contexto: Context): Vibrator =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val gestor = contexto.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        gestor.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        contexto.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

private enum class Pantalla { BIENVENIDA, TEMPORIZADOR, CONFIGURACION, HISTORIAL }

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
    val contexto = LocalContext.current
    val repositorio = remember { AjustesRepositorio(contexto.applicationContext) }
    val ajustes by repositorio.ajustes.collectAsState(initial = Ajustes())
    val sesionRepositorio = remember {
        SesionRepositorio(BaseDatos.obtener(contexto.applicationContext).sesionDao())
    }
    val sesiones by sesionRepositorio.sesiones.collectAsState(initial = emptyList())
    val logrosRepositorio = remember { LogrosRepositorio(contexto.applicationContext) }
    val logrosDesbloqueados by logrosRepositorio.desbloqueados.collectAsState(initial = emptySet())
    val alcance = rememberCoroutineScope()

    var hoyEpoch by rememberSaveable { mutableStateOf(LocalDate.now().toEpochDay()) }
    val hoy = LocalDate.ofEpochDay(hoyEpoch)
    val conteoPorDia = remember(sesiones) { sesiones.conteoPorDia() }
    val totalDuracionSeg = remember(sesiones) { sesiones.sumOf { it.duracionSeg } }
    val totalReps = remember(sesiones) { sesiones.sumOf { it.conteo } }
    val estadisticas = remember(
        conteoPorDia,
        totalDuracionSeg,
        totalReps,
        ajustes.metaDiariaSesiones,
        ajustes.metaSemanalActiva,
        ajustes.metaSemanalSesiones,
        ajustes.inicioSemana,
        hoy
    ) {
        calcularEstadisticas(
            conteoPorDia = conteoPorDia,
            totalDuracionSeg = totalDuracionSeg,
            totalReps = totalReps,
            metaDiaria = ajustes.metaDiariaSesiones,
            metaSemanalActiva = ajustes.metaSemanalActiva,
            metaSemanal = ajustes.metaSemanalSesiones,
            inicioSemana = ajustes.inicioSemana,
            hoy = hoy
        )
    }
    val logros = remember(estadisticas, logrosDesbloqueados, ajustes.metaSemanalActiva) {
        calcularLogros(
            totalSesiones = estadisticas.totalSesiones,
            mejorRachaDiaria = estadisticas.constancia.mejorRacha,
            mejorRachaSemanal = estadisticas.semanal?.mejorRacha ?: 0,
            metaSemanalActiva = ajustes.metaSemanalActiva,
            desbloqueados = logrosDesbloqueados
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            val ahora = ZonedDateTime.now()
            val manana = ahora.toLocalDate().plusDays(1).atStartOfDay(ahora.zone)
            delay(Duration.between(ahora, manana).toMillis() + 1_000)
            hoyEpoch = LocalDate.now().toEpochDay()
        }
    }

    LaunchedEffect(logros, logrosDesbloqueados) {
        val nuevos = logros
            .filter { logro -> logro.desbloqueado && logro.id !in logrosDesbloqueados }
            .map { it.id }
            .toSet()
        if (nuevos.isNotEmpty()) logrosRepositorio.desbloquear(nuevos)
    }

    var pantalla by rememberSaveable { mutableStateOf(Pantalla.BIENVENIDA) }
    var pantallaAnterior by rememberSaveable { mutableStateOf(Pantalla.BIENVENIDA) }
    val stateHolder = rememberSaveableStateHolder()
    val temporizador = recordarTemporizadorEstado()

    LaunchedEffect(
        ajustes.duracionEjercicioSeg,
        ajustes.intervalosActivos,
        ajustes.pausaSeg,
        ajustes.rondas
    ) {
        temporizador.aplicarAjustes(ajustes, SystemClock.elapsedRealtime())
    }

    LaunchedEffect(temporizador.tiempoAgotado, temporizador.idSesion) {
        if (!temporizador.tiempoAgotado) return@LaunchedEffect
        val sesion = crearSesionCompletada(temporizador, System.currentTimeMillis())
        if (sesion != null) {
            withContext(NonCancellable) { sesionRepositorio.guardar(sesion) }
        }
    }

    EfectosTemporizador(temporizador, ajustes)

    BackHandler(enabled = pantalla != Pantalla.BIENVENIDA) {
        pantalla = when (pantalla) {
            Pantalla.CONFIGURACION, Pantalla.HISTORIAL -> pantallaAnterior
            else -> Pantalla.BIENVENIDA
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        stateHolder.SaveableStateProvider(pantalla) {
            val contenidoModifier = Modifier.padding(innerPadding)
            when (pantalla) {
                Pantalla.BIENVENIDA -> BienvenidaScreen(
                    resumen = estadisticas.constancia,
                    resumenSemanal = estadisticas.semanal,
                    onContinuar = { pantalla = Pantalla.TEMPORIZADOR },
                    onAbrirConfiguracion = {
                        pantallaAnterior = pantalla
                        pantalla = Pantalla.CONFIGURACION
                    },
                    onAbrirHistorial = {
                        pantallaAnterior = pantalla
                        pantalla = Pantalla.HISTORIAL
                    },
                    modifier = contenidoModifier
                )
                Pantalla.TEMPORIZADOR -> TemporizadorScreen(
                    estado = temporizador,
                    autoIniciar = ajustes.autoIniciar,
                    onAbrirConfiguracion = {
                        pantallaAnterior = pantalla
                        pantalla = Pantalla.CONFIGURACION
                    },
                    onAbrirHistorial = {
                        pantallaAnterior = pantalla
                        pantalla = Pantalla.HISTORIAL
                    },
                    modifier = contenidoModifier
                )
                Pantalla.CONFIGURACION -> ConfiguracionScreen(
                    ajustes = ajustes,
                    onCambiarAjustes = { cambio ->
                        alcance.launch { repositorio.actualizar(cambio) }
                    },
                    onVolver = { pantalla = pantallaAnterior },
                    modifier = contenidoModifier
                )
                Pantalla.HISTORIAL -> HistorialScreen(
                    sesiones = sesiones,
                    estadisticas = estadisticas,
                    logros = logros,
                    inicioSemana = ajustes.inicioSemana,
                    hoy = hoy,
                    onActualizarSesion = { sesion ->
                        alcance.launch { sesionRepositorio.actualizar(sesion) }
                    },
                    onEliminarSesion = { id ->
                        alcance.launch { sesionRepositorio.eliminar(id) }
                    },
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
            text = {
                Text(
                    text = if (temporizador.intervalosActivos) {
                        "Has completado ${temporizador.rondasTotales} rondas."
                    } else {
                        "Se terminó el tiempo configurado."
                    },
                    color = Color.Black
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

private fun crearSesionCompletada(
    estado: TemporizadorEstado,
    finMs: Long
): SesionEntrenamiento? {
    if (estado.idSesion.isBlank() || estado.inicioMs <= 0L) return null
    val descansos = if (estado.intervalosActivos) {
        (estado.rondasTotales - 1).coerceAtLeast(0)
    } else {
        0
    }
    val duracionSeg = if (estado.intervalosActivos) {
        estado.rondasTotales * estado.duracionEjercicioSeg + descansos * estado.pausaSeg
    } else {
        estado.duracionEjercicioSeg
    }
    return SesionEntrenamiento(
        id = estado.idSesion,
        inicioMs = estado.inicioMs,
        finMs = finMs,
        fechaLocal = fechaLocalDe(finMs).toString(),
        duracionSeg = duracionSeg,
        intervalos = estado.intervalosActivos,
        rondas = if (estado.intervalosActivos) estado.rondasTotales else 0,
        conteo = estado.conteo
    )
}

@Composable
private fun EfectosTemporizador(estado: TemporizadorEstado, ajustes: Ajustes) {    val contexto = LocalContext.current

    LaunchedEffect(estado.corriendo, estado.finEnMs) {
        if (!estado.corriendo || estado.finEnMs == 0L) return@LaunchedEffect
        while (true) {
            estado.avanzar(SystemClock.elapsedRealtime())
            if (!estado.corriendo) break
            delay(200)
        }
    }

    LaunchedEffect(estado.cambioFase) {
        if (estado.cambioFase == 0 || estado.tiempoAgotado) return@LaunchedEffect
        if (ajustes.sonidoActivo) {
            val tono = ToneGenerator(AudioManager.STREAM_ALARM, 80)
            try {
                tono.startTone(ToneGenerator.TONE_PROP_BEEP2, 200)
                delay(250)
            } finally {
                tono.release()
            }
        }
        if (ajustes.vibracionActiva) {
            obtenerVibrador(contexto).vibrate(
                VibrationEffect.createOneShot(250, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        }
    }

    LaunchedEffect(estado.tiempoAgotado, ajustes.sonidoActivo, ajustes.tonoUri, ajustes.vibracionActiva) {
        if (!estado.tiempoAgotado) return@LaunchedEffect
        var ringtone: Ringtone? = null
        var track: AudioTrack? = null
        var vibrador: Vibrator? = null

        if (ajustes.sonidoActivo) {
            ringtone = crearRingtone(contexto, ajustes.tonoUri)
            if (ringtone != null) {
                ringtone.play()
            } else {
                track = crearAudioTrackAlarma().apply { play() }
            }
        }
        if (ajustes.vibracionActiva) {
            vibrador = obtenerVibrador(contexto)
            val patron = longArrayOf(0, 700, 400, 700, 400)
            vibrador.vibrate(VibrationEffect.createWaveform(patron, 0))
        }

        try {
            awaitCancellation()
        } finally {
            ringtone?.stop()
            track?.let {
                it.stop()
                it.release()
            }
            vibrador?.cancel()
        }
    }
}
