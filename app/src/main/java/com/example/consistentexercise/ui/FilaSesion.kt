package com.example.consistentexercise.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.consistentexercise.datos.SesionEntrenamiento
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun FilaSesion(
    sesion: SesionEntrenamiento,
    onEditar: (() -> Unit)? = null,
    onEliminar: (() -> Unit)? = null
) {
    val detalles = buildList {
        add(formatearDuracion(sesion.duracionSeg))
        if (sesion.intervalos) add("${sesion.rondas} rondas")
        if (sesion.conteo > 0) add("${sesion.conteo} reps")
    }
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatearHora(sesion.finMs),
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.End
            ) {
                if (onEditar != null) {
                    IconButton(
                        onClick = onEditar,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Editar sesión",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                if (onEliminar != null) {
                    IconButton(
                        onClick = onEliminar,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Eliminar sesión",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
        Text(
            text = detalles.joinToString(" · "),
            style = MaterialTheme.typography.bodySmall,
            color = Color.Black.copy(alpha = 0.7f)
        )
    }
}

fun formatearDia(fecha: LocalDate): String {
    val locale = Locale.forLanguageTag("es")
    val texto = fecha.format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", locale))
    return texto.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
}

fun formatearHora(ms: Long): String =
    Instant.ofEpochMilli(ms)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
        .format(DateTimeFormatter.ofPattern("HH:mm"))

fun formatearDuracion(segundos: Int): String =
    "%d:%02d".format(segundos / 60, segundos % 60)
