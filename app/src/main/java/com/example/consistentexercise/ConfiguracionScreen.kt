package com.example.consistentexercise

import android.content.Context
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.consistentexercise.ui.AjusteCantidad
import com.example.consistentexercise.ui.AzulClaro
import com.example.consistentexercise.ui.FilaAjuste
import com.example.consistentexercise.ui.PantallaConBorde
import com.example.consistentexercise.ui.SeccionAjustes
import com.example.consistentexercise.ui.SwitchAjuste
import com.example.consistentexercise.ui.theme.ConsistentExerciseTheme
import java.time.DayOfWeek
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ConfiguracionScreen(
    ajustes: Ajustes,
    onCambiarAjustes: ((Ajustes) -> Ajustes) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    PantallaConBorde(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, start = 20.dp, end = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onVolver,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    text = "Configuración",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.size(56.dp))
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SeccionAjustes(titulo = "Temporizador/ejercicio") {
                    Text(text = "Duración por defecto", color = Color.Black)
                    FilaAjuste(etiqueta = "Minutos") {
                        AjusteCantidad(
                            valor = ajustes.duracionMinutos,
                            onDecrementar = {
                                onCambiarAjustes {
                                    it.copy(duracionMinutos = (it.duracionMinutos - 1).coerceAtLeast(0))
                                }
                            },
                            onIncrementar = {
                                onCambiarAjustes {
                                    it.copy(duracionMinutos = (it.duracionMinutos + 1).coerceAtMost(99))
                                }
                            }
                        )
                    }
                    FilaAjuste(etiqueta = "Segundos") {
                        AjusteCantidad(
                            valor = ajustes.duracionSegundos,
                            onDecrementar = {
                                onCambiarAjustes {
                                    it.copy(duracionSegundos = (it.duracionSegundos - 1).coerceAtLeast(0))
                                }
                            },
                            onIncrementar = {
                                onCambiarAjustes {
                                    it.copy(duracionSegundos = (it.duracionSegundos + 1).coerceAtMost(59))
                                }
                            }
                        )
                    }
                    HorizontalDivider(color = AzulClaro.copy(alpha = 0.6f))
                    FilaAjuste(
                        etiqueta = "Intervalos",
                        descripcion = "Alterna ejercicio y pausa"
                    ) {
                        SwitchAjuste(
                            checked = ajustes.intervalosActivos,
                            onCheckedChange = { activo ->
                                onCambiarAjustes { it.copy(intervalosActivos = activo) }
                            }
                        )
                    }
                    if (ajustes.intervalosActivos) {
                        Text(text = "Duración de la pausa", color = Color.Black)
                        FilaAjuste(etiqueta = "Minutos") {
                            AjusteCantidad(
                                valor = ajustes.pausaMinutos,
                                onDecrementar = {
                                    onCambiarAjustes {
                                        it.copy(pausaMinutos = (it.pausaMinutos - 1).coerceAtLeast(0))
                                    }
                                },
                                onIncrementar = {
                                    onCambiarAjustes {
                                        it.copy(pausaMinutos = (it.pausaMinutos + 1).coerceAtMost(99))
                                    }
                                }
                            )
                        }
                        FilaAjuste(etiqueta = "Segundos") {
                            AjusteCantidad(
                                valor = ajustes.pausaSegundos,
                                onDecrementar = {
                                    onCambiarAjustes {
                                        it.copy(pausaSegundos = (it.pausaSegundos - 1).coerceAtLeast(0))
                                    }
                                },
                                onIncrementar = {
                                    onCambiarAjustes {
                                        it.copy(pausaSegundos = (it.pausaSegundos + 1).coerceAtMost(59))
                                    }
                                }
                            )
                        }
                        FilaAjuste(etiqueta = "Rondas") {
                            AjusteCantidad(
                                valor = ajustes.rondas,
                                onDecrementar = {
                                    onCambiarAjustes {
                                        it.copy(rondas = (it.rondas - 1).coerceAtLeast(1))
                                    }
                                },
                                onIncrementar = {
                                    onCambiarAjustes {
                                        it.copy(rondas = (it.rondas + 1).coerceAtMost(99))
                                    }
                                }
                            )
                        }
                    }
                }

                SeccionAjustes(titulo = "Constancia") {
                    FilaAjuste(
                        etiqueta = "Meta diaria",
                        descripcion = "Sesiones para validar el día"
                    ) {
                        AjusteCantidad(
                            valor = ajustes.metaDiariaSesiones,
                            onDecrementar = {
                                onCambiarAjustes {
                                    it.copy(
                                        metaDiariaSesiones = (it.metaDiariaSesiones - 1)
                                            .coerceAtLeast(1)
                                    )
                                }
                            },
                            onIncrementar = {
                                onCambiarAjustes {
                                    it.copy(
                                        metaDiariaSesiones = (it.metaDiariaSesiones + 1)
                                            .coerceAtMost(99)
                                    )
                                }
                            }
                        )
                    }
                    HorizontalDivider(color = AzulClaro.copy(alpha = 0.6f))
                    FilaAjuste(
                        etiqueta = "Meta semanal",
                        descripcion = "Sesiones completadas por semana"
                    ) {
                        SwitchAjuste(
                            checked = ajustes.metaSemanalActiva,
                            onCheckedChange = { activa ->
                                onCambiarAjustes { it.copy(metaSemanalActiva = activa) }
                            }
                        )
                    }
                    if (ajustes.metaSemanalActiva) {
                        FilaAjuste(etiqueta = "Sesiones por semana") {
                            AjusteCantidad(
                                valor = ajustes.metaSemanalSesiones,
                                onDecrementar = {
                                    onCambiarAjustes {
                                        it.copy(
                                            metaSemanalSesiones = (it.metaSemanalSesiones - 1)
                                                .coerceAtLeast(1)
                                        )
                                    }
                                },
                                onIncrementar = {
                                    onCambiarAjustes {
                                        it.copy(
                                            metaSemanalSesiones = (it.metaSemanalSesiones + 1)
                                                .coerceAtMost(99)
                                        )
                                    }
                                }
                            )
                        }
                    }
                    FilaAjuste(etiqueta = "Inicio de semana") {
                        SelectorInicioSemana(
                            inicio = ajustes.inicioSemana,
                            onCambiar = { dia ->
                                onCambiarAjustes { it.copy(inicioSemana = dia) }
                            }
                        )
                    }
                }

                SeccionAjustes(titulo = "Sonido y vibración") {
                    FilaAjuste(etiqueta = "Sonido de alarma") {
                        SwitchAjuste(
                            checked = ajustes.sonidoActivo,
                            onCheckedChange = { activo ->
                                onCambiarAjustes { it.copy(sonidoActivo = activo) }
                            }
                        )
                    }
                    if (ajustes.sonidoActivo) {
                        SelectorTono(
                            tonoUri = ajustes.tonoUri,
                            onCambiar = { uri ->
                                onCambiarAjustes { it.copy(tonoUri = uri) }
                            }
                        )
                    }
                    FilaAjuste(etiqueta = "Vibración al terminar") {
                        SwitchAjuste(
                            checked = ajustes.vibracionActiva,
                            onCheckedChange = { activa ->
                                onCambiarAjustes { it.copy(vibracionActiva = activa) }
                            }
                        )
                    }
                }

                SeccionAjustes(titulo = "Comportamiento") {
                    FilaAjuste(
                        etiqueta = "Auto-iniciar al entrar",
                        descripcion = "Empieza el temporizador al abrir la pantalla"
                    ) {
                        SwitchAjuste(
                            checked = ajustes.autoIniciar,
                            onCheckedChange = { activo ->
                                onCambiarAjustes { it.copy(autoIniciar = activo) }
                            }
                        )
                    }
                }
            }
        }
    }
}

private data class TonoAlarma(val titulo: String, val uri: String)

private fun obtenerTonosAlarma(contexto: Context): List<TonoAlarma> {
    val gestor = RingtoneManager(contexto)
    gestor.setType(RingtoneManager.TYPE_ALARM)
    val predeterminada = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
    val tonos = mutableListOf<TonoAlarma>()
    if (predeterminada != null) {
        val titulo = runCatching {
            RingtoneManager.getRingtone(contexto, predeterminada)?.getTitle(contexto)
        }.getOrNull() ?: "Alarma predeterminada"
        tonos.add(TonoAlarma(titulo, predeterminada.toString()))
    }
    val cursor = gestor.cursor
    for (i in 0 until cursor.count) {
        val uri = gestor.getRingtoneUri(i) ?: continue
        if (tonos.any { it.uri == uri.toString() }) continue
        val titulo = runCatching {
            gestor.getRingtone(i).getTitle(contexto)
        }.getOrNull() ?: "Tono ${i + 1}"
        tonos.add(TonoAlarma(titulo, uri.toString()))
    }
    return tonos
}

@Composable
private fun SelectorInicioSemana(
    inicio: DayOfWeek,
    onCambiar: (DayOfWeek) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { expandido = true },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
            border = BorderStroke(width = 2.dp, color = AzulClaro)
        ) {
            Text(text = nombreDia(inicio))
        }
        DropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false },
            containerColor = Color.White
        ) {
            DayOfWeek.entries.forEach { dia ->
                DropdownMenuItem(
                    text = { Text(text = nombreDia(dia), color = Color.Black) },
                    onClick = {
                        onCambiar(dia)
                        expandido = false
                    },
                    colors = MenuDefaults.itemColors(textColor = Color.Black)
                )
            }
        }
    }
}

private fun nombreDia(dia: DayOfWeek): String = when (dia) {
    DayOfWeek.MONDAY -> "Lunes"
    DayOfWeek.TUESDAY -> "Martes"
    DayOfWeek.WEDNESDAY -> "Miércoles"
    DayOfWeek.THURSDAY -> "Jueves"
    DayOfWeek.FRIDAY -> "Viernes"
    DayOfWeek.SATURDAY -> "Sábado"
    DayOfWeek.SUNDAY -> "Domingo"
}

@Composable
private fun SelectorTono(
    tonoUri: String?,
    onCambiar: (String?) -> Unit
) {
    val contexto = LocalContext.current
    val tonos = remember { obtenerTonosAlarma(contexto) }
    val seleccionado = tonos.firstOrNull { it.uri == tonoUri } ?: tonos.firstOrNull()
    var expandido by remember { mutableStateOf(false) }
    var reproduciendo by remember { mutableStateOf<Ringtone?>(null) }
    val alcance = rememberCoroutineScope()

    fun detenerPrevisualizacion() {
        reproduciendo?.stop()
        reproduciendo = null
    }

    DisposableEffect(Unit) {
        onDispose { reproduciendo?.stop() }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Tono de alarma", color = Color.Black)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { expandido = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                    border = BorderStroke(width = 2.dp, color = AzulClaro)
                ) {
                    Text(
                        text = seleccionado?.titulo ?: "Alarma predeterminada",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                DropdownMenu(
                    expanded = expandido,
                    onDismissRequest = { expandido = false },
                    containerColor = Color.White
                ) {
                    tonos.forEach { tono ->
                        DropdownMenuItem(
                            text = { Text(text = tono.titulo, color = Color.Black) },
                            onClick = {
                                onCambiar(tono.uri)
                                expandido = false
                            },
                            colors = MenuDefaults.itemColors(textColor = Color.Black)
                        )
                    }
                }
            }
            OutlinedButton(
                onClick = {
                    detenerPrevisualizacion()
                    val uri = seleccionado?.uri?.let(Uri::parse)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    val ringtone = uri?.let {
                        runCatching { RingtoneManager.getRingtone(contexto, it) }.getOrNull()
                    }
                    ringtone?.apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build()
                        )
                        isLooping = false
                        play()
                    }
                    reproduciendo = ringtone
                    if (ringtone != null) {
                        alcance.launch {
                            delay(4000)
                            if (reproduciendo === ringtone) detenerPrevisualizacion()
                        }
                    }
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                border = BorderStroke(width = 2.dp, color = AzulClaro)
            ) {
                Text(text = "Probar")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConfiguracionPreview() {
    ConsistentExerciseTheme {
        ConfiguracionScreen(
            ajustes = Ajustes(),
            onCambiarAjustes = {},
            onVolver = {}
        )
    }
}
