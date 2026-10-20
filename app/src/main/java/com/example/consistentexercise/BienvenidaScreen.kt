package com.example.consistentexercise

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.consistentexercise.dominio.ResumenConstancia
import com.example.consistentexercise.dominio.ResumenSemanal
import com.example.consistentexercise.ui.AzulBoton
import com.example.consistentexercise.ui.PantallaConBorde
import com.example.consistentexercise.ui.SeccionAjustes
import com.example.consistentexercise.ui.theme.ConsistentExerciseTheme

@Composable
fun BienvenidaScreen(
    resumen: ResumenConstancia,
    resumenSemanal: ResumenSemanal?,
    onContinuar: () -> Unit,
    onAbrirConfiguracion: () -> Unit,
    onAbrirHistorial: () -> Unit,
    modifier: Modifier = Modifier
) {
    PantallaConBorde(modifier = modifier) {
        IconButton(
            onClick = onAbrirHistorial,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 20.dp, start = 20.dp)
                .size(56.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.List,
                contentDescription = "Historial",
                tint = Color.Black,
                modifier = Modifier.size(28.dp)
            )
        }
        IconButton(
            onClick = onAbrirConfiguracion,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 20.dp, end = 20.dp)
                .size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "Configuración",
                tint = Color.Black,
                modifier = Modifier.size(28.dp)
            )
        }
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Bienvenido",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily(Font(R.font.dancing_script, weight = FontWeight.Bold))
            )
            Button(
                onClick = onContinuar,
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .fillMaxHeight(0.1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AzulBoton,
                    contentColor = Color.White
                )
            ) {
                Text(text = "Continuar", fontSize = 22.sp, fontStyle = FontStyle.Italic)
            }
            SeccionAjustes(
                titulo = "Constancia",
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                FilaResumen(
                    etiqueta = "Hoy",
                    valor = "${resumen.sesionesHoy} / ${resumen.metaSesiones} sesiones"
                )
                FilaResumen(
                    etiqueta = "Racha actual",
                    valor = "${resumen.rachaActual} ${if (resumen.rachaActual == 1) "día" else "días"}"
                )
                FilaResumen(
                    etiqueta = "Mejor racha",
                    valor = "${resumen.mejorRacha} ${if (resumen.mejorRacha == 1) "día" else "días"}"
                )
                if (resumenSemanal != null) {
                    FilaResumen(
                        etiqueta = "Esta semana",
                        valor = "${resumenSemanal.sesiones} / ${resumenSemanal.metaSesiones} sesiones"
                    )
                }
            }
        }
    }
}

@Composable
private fun FilaResumen(
    etiqueta: String,
    valor: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = etiqueta, color = Color.Black)
        Text(text = valor, color = Color.Black, fontWeight = FontWeight.Bold)
    }
}

@Preview(showBackground = true)
@Composable
fun BienvenidaPreview() {
    ConsistentExerciseTheme {
        BienvenidaScreen(
            resumen = ResumenConstancia(
                sesionesHoy = 1,
                metaSesiones = 2,
                rachaActual = 3,
                mejorRacha = 5
            ),
            resumenSemanal = ResumenSemanal(
                inicio = java.time.LocalDate.of(2026, 10, 19),
                fin = java.time.LocalDate.of(2026, 10, 25),
                sesiones = 2,
                metaSesiones = 3,
                rachaActual = 1,
                mejorRacha = 1
            ),
            onContinuar = {},
            onAbrirConfiguracion = {},
            onAbrirHistorial = {}
        )
    }
}
