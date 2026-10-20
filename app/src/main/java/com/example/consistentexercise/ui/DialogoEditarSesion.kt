package com.example.consistentexercise.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.consistentexercise.datos.SesionEntrenamiento
import com.example.consistentexercise.ui.theme.ConsistentExerciseTheme
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoEditarSesion(
    sesion: SesionEntrenamiento,
    onGuardar: (SesionEntrenamiento) -> Unit,
    onCancelar: () -> Unit
) {
    val fechaInicial = LocalDate.parse(sesion.fechaLocal)
    val horaInicial = Instant.ofEpochMilli(sesion.finMs)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
        .withSecond(0)
        .withNano(0)
    var fecha by remember(sesion.id) { mutableStateOf(fechaInicial) }
    var hora by remember(sesion.id) { mutableStateOf(horaInicial) }
    var minutos by remember(sesion.id) { mutableIntStateOf(sesion.duracionSeg / 60) }
    var segundos by remember(sesion.id) { mutableIntStateOf(sesion.duracionSeg % 60) }
    var rondas by remember(sesion.id) {
        mutableIntStateOf(if (sesion.intervalos) sesion.rondas.coerceAtLeast(1) else 1)
    }
    var conteo by remember(sesion.id) { mutableIntStateOf(sesion.conteo) }
    var mostrarFecha by remember { mutableStateOf(false) }
    var mostrarHora by remember { mutableStateOf(false) }
    val duracionTotal = minutos * 60 + segundos

    AlertDialog(
        onDismissRequest = onCancelar,
        containerColor = Color.White,
        title = {
            Text(
                text = "Editar sesión",
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { mostrarFecha = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                    border = BorderStroke(2.dp, AzulClaro)
                ) {
                    Text(text = "Fecha: ${formatearFecha(fecha)}")
                }
                OutlinedButton(
                    onClick = { mostrarHora = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                    border = BorderStroke(2.dp, AzulClaro)
                ) {
                    Text(text = "Hora: ${formatearHoraLocal(hora)}")
                }
                Text(text = "Duración", color = Color.Black, fontWeight = FontWeight.Bold)
                FilaAjuste(etiqueta = "Minutos") {
                    AjusteCantidad(
                        valor = minutos,
                        onDecrementar = { minutos = (minutos - 1).coerceAtLeast(0) },
                        onIncrementar = { minutos = (minutos + 1).coerceAtMost(99) }
                    )
                }
                FilaAjuste(etiqueta = "Segundos") {
                    AjusteCantidad(
                        valor = segundos,
                        onDecrementar = { segundos = (segundos - 1).coerceAtLeast(0) },
                        onIncrementar = { segundos = (segundos + 1).coerceAtMost(59) }
                    )
                }
                if (sesion.intervalos) {
                    FilaAjuste(etiqueta = "Rondas") {
                        AjusteCantidad(
                            valor = rondas,
                            onDecrementar = { rondas = (rondas - 1).coerceAtLeast(1) },
                            onIncrementar = { rondas = (rondas + 1).coerceAtMost(99) }
                        )
                    }
                }
                FilaAjuste(etiqueta = "Reps") {
                    AjusteCantidad(
                        valor = conteo,
                        onDecrementar = { conteo = (conteo - 1).coerceAtLeast(0) },
                        onIncrementar = { conteo = (conteo + 1).coerceAtMost(9999) }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finMs = ZonedDateTime.of(fecha, hora, ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                    onGuardar(
                        sesion.copy(
                            inicioMs = finMs - duracionTotal * 1000L,
                            finMs = finMs,
                            fechaLocal = fecha.toString(),
                            duracionSeg = duracionTotal,
                            rondas = if (sesion.intervalos) rondas else 0,
                            conteo = conteo
                        )
                    )
                },
                enabled = duracionTotal > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AzulBoton,
                    contentColor = Color.White
                )
            ) {
                Text(text = "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text(text = "Cancelar", color = Color.Black)
            }
        }
    )

    if (mostrarFecha) {
        ConsistentExerciseTheme(darkTheme = false) {
            val estadoFecha = rememberDatePickerState(
                initialSelectedDateMillis = fecha
                    .atStartOfDay(ZoneOffset.UTC)
                    .toInstant()
                    .toEpochMilli()
            )
            DatePickerDialog(
                onDismissRequest = { mostrarFecha = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            estadoFecha.selectedDateMillis?.let { millis ->
                                fecha = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneOffset.UTC)
                                    .toLocalDate()
                            }
                            mostrarFecha = false
                        }
                    ) {
                        Text(text = "Aceptar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarFecha = false }) {
                        Text(text = "Cancelar")
                    }
                }
            ) {
                DatePicker(state = estadoFecha)
            }
        }
    }

    if (mostrarHora) {
        ConsistentExerciseTheme(darkTheme = false) {
            val estadoHora = rememberTimePickerState(
                initialHour = hora.hour,
                initialMinute = hora.minute,
                is24Hour = true
            )
            AlertDialog(
                onDismissRequest = { mostrarHora = false },
                containerColor = Color.White,
                title = {
                    Text(text = "Hora", color = Color.Black, fontWeight = FontWeight.Bold)
                },
                text = {
                    TimePicker(state = estadoHora)
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            hora = LocalTime.of(estadoHora.hour, estadoHora.minute)
                            mostrarHora = false
                        }
                    ) {
                        Text(text = "Aceptar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarHora = false }) {
                        Text(text = "Cancelar")
                    }
                }
            )
        }
    }
}

private fun formatearFecha(fecha: LocalDate): String =
    fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("es")))

private fun formatearHoraLocal(hora: LocalTime): String =
    hora.format(DateTimeFormatter.ofPattern("HH:mm"))
