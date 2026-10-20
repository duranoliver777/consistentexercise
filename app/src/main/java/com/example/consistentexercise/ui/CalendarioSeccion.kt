package com.example.consistentexercise.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.consistentexercise.datos.SesionEntrenamiento
import com.example.consistentexercise.datos.conteoPorDia
import com.example.consistentexercise.dominio.CeldaCalendario
import com.example.consistentexercise.dominio.EstadoDia
import com.example.consistentexercise.dominio.calcularCeldasMes
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CalendarioSeccion(
    sesiones: List<SesionEntrenamiento>,
    metaDiaria: Int,
    inicioSemana: DayOfWeek,
    hoy: LocalDate,
    onEditarSesion: (SesionEntrenamiento) -> Unit,
    onEliminarSesion: (SesionEntrenamiento) -> Unit,
    modifier: Modifier = Modifier
) {
    val conteo = remember(sesiones) { sesiones.conteoPorDia() }
    var mesGuardado by rememberSaveable { mutableStateOf(YearMonth.from(hoy).toString()) }
    var diaSeleccionadoGuardado by rememberSaveable { mutableStateOf<String?>(null) }
    val mes = YearMonth.parse(mesGuardado)
    val diaSeleccionado = diaSeleccionadoGuardado?.let(LocalDate::parse)
    val celdas = remember(mes, inicioSemana, conteo, metaDiaria, hoy) {
        calcularCeldasMes(mes, inicioSemana, conteo, metaDiaria, hoy)
    }
    val sesionesDelDia = remember(sesiones, diaSeleccionado) {
        diaSeleccionado?.let { fecha ->
            sesiones.filter { it.fechaLocal == fecha.toString() }
        }.orEmpty()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    mesGuardado = mes.minusMonths(1).toString()
                    diaSeleccionadoGuardado = null
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Mes anterior",
                    tint = Color.Black
                )
            }
            Text(
                text = nombreMes(mes),
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    mesGuardado = mes.plusMonths(1).toString()
                    diaSeleccionadoGuardado = null
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Mes siguiente",
                    tint = Color.Black
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            repeat(7) { indice ->
                val dia = inicioSemana.plus(indice.toLong())
                Text(
                    text = nombreDiaCorto(dia),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        celdas.chunked(7).forEach { fila ->
            Row(modifier = Modifier.fillMaxWidth()) {
                fila.forEach { celda ->
                    CeldaDia(
                        celda = celda,
                        esHoy = celda.fecha == hoy,
                        seleccionada = celda.fecha != null && celda.fecha == diaSeleccionado,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            diaSeleccionadoGuardado = celda.fecha?.toString()
                        }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Leyenda(color = AzulBoton, texto = "Meta cumplida")
            Leyenda(color = AzulClaro.copy(alpha = 0.55f), texto = "Parcial")
            Leyenda(color = Color.White, texto = "Sin actividad", borde = true)
        }

        Spacer(modifier = Modifier.size(4.dp))

        if (diaSeleccionado == null) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Toca un día para ver sus sesiones",
                    color = Color.Black.copy(alpha = 0.7f)
                )
            }
        } else {
            Text(
                text = formatearDia(diaSeleccionado),
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
            if (sesionesDelDia.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (diaSeleccionado.isAfter(hoy)) {
                            "Día futuro"
                        } else {
                            "Sin sesiones este día"
                        },
                        color = Color.Black.copy(alpha = 0.7f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        SeccionAjustes(
                            titulo = if (sesionesDelDia.size == 1) {
                                "1 sesión"
                            } else {
                                "${sesionesDelDia.size} sesiones"
                            }
                        ) {
                            sesionesDelDia.forEachIndexed { indice, sesion ->
                                if (indice > 0) {
                                    HorizontalDivider(color = AzulClaro.copy(alpha = 0.6f))
                                }
                                FilaSesion(
                                    sesion = sesion,
                                    onEditar = { onEditarSesion(sesion) },
                                    onEliminar = { onEliminarSesion(sesion) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CeldaDia(
    celda: CeldaCalendario,
    esHoy: Boolean,
    seleccionada: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    if (celda.fecha == null) {
        Box(
            modifier = modifier
                .padding(2.dp)
                .aspectRatio(1f)
        )
        return
    }
    val fecha = celda.fecha

    val colorFondo = when (celda.estado) {
        EstadoDia.META_CUMPLIDA -> AzulBoton
        EstadoDia.ACTIVIDAD_PARCIAL -> AzulClaro.copy(alpha = 0.55f)
        EstadoDia.SIN_ACTIVIDAD -> Color.White
        EstadoDia.FUTURA -> AzulClaro.copy(alpha = 0.12f)
    }
    val colorTexto = when (celda.estado) {
        EstadoDia.META_CUMPLIDA -> Color.White
        EstadoDia.FUTURA -> Color.Black.copy(alpha = 0.35f)
        else -> Color.Black
    }
    val borde = when {
        seleccionada -> 2.dp to Color.Black
        esHoy -> 2.dp to AzulBoton
        else -> 1.dp to AzulClaro.copy(alpha = 0.5f)
    }
    val forma = RoundedCornerShape(8.dp)

    Box(
        modifier = modifier
            .padding(2.dp)
            .aspectRatio(1f)
            .clip(forma)
            .background(colorFondo)
            .border(width = borde.first, color = borde.second, shape = forma)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = fecha.dayOfMonth.toString(),
                color = colorTexto,
                fontSize = 13.sp,
                fontWeight = if (esHoy) FontWeight.Bold else FontWeight.Normal
            )
            if (celda.sesiones > 0) {
                Text(
                    text = celda.sesiones.toString(),
                    color = colorTexto.copy(alpha = 0.8f),
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
private fun Leyenda(
    color: Color,
    texto: String,
    borde: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
                .then(
                    if (borde) {
                        Modifier.border(1.dp, AzulClaro, RoundedCornerShape(3.dp))
                    } else {
                        Modifier
                    }
                )
        )
        Text(
            text = texto,
            fontSize = 11.sp,
            color = Color.Black.copy(alpha = 0.8f)
        )
    }
}

private fun nombreMes(mes: YearMonth): String {
    val locale = Locale.forLanguageTag("es")
    val texto = mes.format(DateTimeFormatter.ofPattern("MMMM yyyy", locale))
    return texto.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
}

private fun nombreDiaCorto(dia: DayOfWeek): String = when (dia) {
    DayOfWeek.MONDAY -> "Lu"
    DayOfWeek.TUESDAY -> "Ma"
    DayOfWeek.WEDNESDAY -> "Mi"
    DayOfWeek.THURSDAY -> "Ju"
    DayOfWeek.FRIDAY -> "Vi"
    DayOfWeek.SATURDAY -> "Sá"
    DayOfWeek.SUNDAY -> "Do"
}
