package com.example.consistentexercise

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.consistentexercise.datos.SesionEntrenamiento
import com.example.consistentexercise.dominio.Estadisticas
import com.example.consistentexercise.dominio.Logro
import com.example.consistentexercise.dominio.ResumenConstancia
import com.example.consistentexercise.ui.AzulBoton
import com.example.consistentexercise.ui.AzulClaro
import com.example.consistentexercise.ui.CalendarioSeccion
import com.example.consistentexercise.ui.DialogoEditarSesion
import com.example.consistentexercise.ui.EstadisticasSeccion
import com.example.consistentexercise.ui.FilaSesion
import com.example.consistentexercise.ui.PantallaConBorde
import com.example.consistentexercise.ui.SeccionAjustes
import com.example.consistentexercise.ui.formatearDia
import com.example.consistentexercise.ui.theme.ConsistentExerciseTheme
import java.time.DayOfWeek
import java.time.LocalDate

@Composable
fun HistorialScreen(
    sesiones: List<SesionEntrenamiento>,
    estadisticas: Estadisticas,
    logros: List<Logro>,
    inicioSemana: DayOfWeek,
    hoy: LocalDate,
    onActualizarSesion: (SesionEntrenamiento) -> Unit,
    onEliminarSesion: (String) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pestana by rememberSaveable { mutableIntStateOf(0) }
    var sesionEditando by remember { mutableStateOf<SesionEntrenamiento?>(null) }
    var sesionEliminando by remember { mutableStateOf<SesionEntrenamiento?>(null) }

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
                    text = "Historial",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.size(56.dp))
            }

            PrimaryTabRow(
                selectedTabIndex = pestana,
                containerColor = Color.Transparent,
                contentColor = Color.Black,
                indicator = {
                    TabRowDefaults.PrimaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(pestana, matchContentSize = true),
                        width = Dp.Unspecified,
                        color = AzulBoton
                    )
                },
                divider = {
                    HorizontalDivider(color = AzulClaro.copy(alpha = 0.6f))
                }
            ) {
                listOf("Sesiones", "Calendario", "Estadísticas").forEachIndexed { indice, titulo ->
                    Tab(
                        selected = pestana == indice,
                        onClick = { pestana = indice },
                        text = {
                            Text(
                                text = titulo,
                                fontWeight = if (pestana == indice) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Normal
                                }
                            )
                        }
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when (pestana) {
                    0 -> ListaSesiones(
                        sesiones = sesiones,
                        onEditar = { sesionEditando = it },
                        onEliminar = { sesionEliminando = it }
                    )
                    1 -> CalendarioSeccion(
                        sesiones = sesiones,
                        metaDiaria = estadisticas.constancia.metaSesiones,
                        inicioSemana = inicioSemana,
                        hoy = hoy,
                        onEditarSesion = { sesionEditando = it },
                        onEliminarSesion = { sesionEliminando = it }
                    )
                    else -> EstadisticasSeccion(
                        estadisticas = estadisticas,
                        logros = logros
                    )
                }
            }
        }
    }

    sesionEditando?.let { sesion ->
        DialogoEditarSesion(
            sesion = sesion,
            onGuardar = { actualizada ->
                onActualizarSesion(actualizada)
                sesionEditando = null
            },
            onCancelar = { sesionEditando = null }
        )
    }

    sesionEliminando?.let { sesion ->
        AlertDialog(
            onDismissRequest = { sesionEliminando = null },
            containerColor = Color.White,
            title = {
                Text(
                    text = "Eliminar sesión",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "¿Seguro que quieres eliminar la sesión de las " +
                        "${com.example.consistentexercise.ui.formatearHora(sesion.finMs)}? " +
                        "La racha y las estadísticas se recalcularán.",
                    color = Color.Black
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onEliminarSesion(sesion.id)
                        sesionEliminando = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AzulBoton,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { sesionEliminando = null }) {
                    Text(text = "Cancelar", color = Color.Black)
                }
            }
        )
    }
}

@Composable
private fun ListaSesiones(
    sesiones: List<SesionEntrenamiento>,
    onEditar: (SesionEntrenamiento) -> Unit,
    onEliminar: (SesionEntrenamiento) -> Unit
) {
    if (sesiones.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Aún no hay sesiones completadas",
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    val grupos = sesiones
        .groupBy { LocalDate.parse(it.fechaLocal) }
        .toSortedMap(compareByDescending { it })

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 24.dp,
            end = 24.dp,
            top = 16.dp,
            bottom = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        grupos.forEach { (fecha, sesionesDelDia) ->
            item(key = fecha.toString()) {
                SeccionAjustes(titulo = formatearDia(fecha)) {
                    sesionesDelDia.forEachIndexed { indice, sesion ->
                        if (indice > 0) {
                            HorizontalDivider(color = AzulClaro.copy(alpha = 0.6f))
                        }
                        FilaSesion(
                            sesion = sesion,
                            onEditar = { onEditar(sesion) },
                            onEliminar = { onEliminar(sesion) }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HistorialPreview() {
    val fin = 1_700_000_000_000L
    val sesion = SesionEntrenamiento(
        id = "1",
        inicioMs = fin - 60_000,
        finMs = fin,
        fechaLocal = "2023-11-14",
        duracionSeg = 60,
        intervalos = true,
        rondas = 3,
        conteo = 12
    )
    ConsistentExerciseTheme {
        HistorialScreen(
            sesiones = listOf(sesion),
            estadisticas = Estadisticas(
                constancia = ResumenConstancia(1, 1, 1, 1),
                semanal = null,
                totalSesiones = 1,
                totalMinutos = 1,
                totalReps = 12,
                barras = emptyList()
            ),
            logros = emptyList(),
            inicioSemana = DayOfWeek.MONDAY,
            hoy = LocalDate.of(2023, 11, 14),
            onActualizarSesion = {},
            onEliminarSesion = {},
            onVolver = {}
        )
    }
}
