package com.example.consistentexercise.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.consistentexercise.dominio.BarraSemana
import com.example.consistentexercise.dominio.Estadisticas
import com.example.consistentexercise.dominio.Logro
import java.time.format.DateTimeFormatter

@Composable
fun EstadisticasSeccion(
    estadisticas: Estadisticas,
    logros: List<Logro>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SeccionAjustes(titulo = "Totales") {
            FilaDato("Sesiones completadas", estadisticas.totalSesiones.toString())
            FilaDato("Tiempo total", "${estadisticas.totalMinutos} min")
            FilaDato("Repeticiones", estadisticas.totalReps.toString())
        }

        SeccionAjustes(titulo = "Rachas") {
            FilaDato("Racha actual", formatearDias(estadisticas.constancia.rachaActual))
            FilaDato("Mejor racha", formatearDias(estadisticas.constancia.mejorRacha))
            estadisticas.semanal?.let { semanal ->
                FilaDato("Racha semanal", formatearSemanas(semanal.rachaActual))
                FilaDato("Mejor racha semanal", formatearSemanas(semanal.mejorRacha))
            }
        }

        estadisticas.semanal?.let { semanal ->
            SeccionAjustes(titulo = "Esta semana") {
                FilaDato(
                    "Progreso",
                    "${semanal.sesiones} / ${semanal.metaSesiones} sesiones"
                )
                BarraProgreso(
                    fraccion = semanal.sesiones.toFloat() / semanal.metaSesiones,
                    completada = semanal.metaCumplida
                )
                Text(
                    text = "${formatearFechaCorta(semanal.inicio)} – ${formatearFechaCorta(semanal.fin)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black.copy(alpha = 0.7f)
                )
            }
        }

        SeccionAjustes(titulo = "Últimas 8 semanas") {
            GraficoBarras(barras = estadisticas.barras)
        }

        SeccionAjustes(titulo = "Logros") {
            logros.forEach { logro ->
                FilaLogro(logro)
            }
        }
    }
}

@Composable
private fun FilaDato(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = etiqueta, color = Color.Black)
        Text(text = valor, color = Color.Black, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BarraProgreso(fraccion: Float, completada: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .background(AzulClaro.copy(alpha = 0.45f), RoundedCornerShape(5.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraccion.coerceIn(0f, 1f))
                .height(10.dp)
                .background(
                    if (completada) AzulBoton else AzulClaro,
                    RoundedCornerShape(5.dp)
                )
        )
    }
}

@Composable
private fun GraficoBarras(barras: List<BarraSemana>) {
    val maximo = maxOf(
        barras.maxOfOrNull { it.sesiones } ?: 0,
        barras.mapNotNull { it.meta }.maxOrNull() ?: 0,
        1
    )
    val alturaMaxima = 110.dp
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        barras.forEach { barra ->
            val cumpleMeta = barra.meta != null && barra.sesiones >= barra.meta
            val altura = if (barra.sesiones <= 0) {
                2.dp
            } else {
                (alturaMaxima * (barra.sesiones.toFloat() / maximo)).coerceAtLeast(2.dp)
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = barra.sesiones.toString(),
                    fontSize = 10.sp,
                    color = Color.Black
                )
                Box(
                    modifier = Modifier
                        .height(alturaMaxima)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(altura)
                            .background(
                                if (cumpleMeta) AzulBoton else AzulClaro,
                                RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                            )
                    )
                }
                Text(
                    text = formatearFechaCorta(barra.inicio),
                    fontSize = 8.sp,
                    color = Color.Black.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun FilaLogro(logro: Logro) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = logro.titulo,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
            if (logro.desbloqueado) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Conseguido",
                    tint = AzulBoton,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = "${logro.progreso} / ${logro.objetivo}",
                    fontSize = 12.sp,
                    color = Color.Black.copy(alpha = 0.7f)
                )
            }
        }
        Text(
            text = logro.descripcion,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Black.copy(alpha = 0.7f)
        )
        BarraProgreso(
            fraccion = logro.fraccion,
            completada = logro.desbloqueado
        )
    }
}

private fun formatearFechaCorta(fecha: java.time.LocalDate): String =
    fecha.format(DateTimeFormatter.ofPattern("dd/MM"))

private fun formatearDias(cantidad: Int): String =
    "$cantidad ${if (cantidad == 1) "día" else "días"}"

private fun formatearSemanas(cantidad: Int): String =
    "$cantidad ${if (cantidad == 1) "semana" else "semanas"}"
