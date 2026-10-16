package com.example.consistentexercise

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
import com.example.consistentexercise.ui.AzulBoton
import com.example.consistentexercise.ui.PantallaConBorde
import com.example.consistentexercise.ui.theme.ConsistentExerciseTheme

@Composable
fun BienvenidaScreen(
    onContinuar: () -> Unit,
    onAbrirConfiguracion: () -> Unit,
    modifier: Modifier = Modifier
) {
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
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BienvenidaPreview() {
    ConsistentExerciseTheme {
        BienvenidaScreen(onContinuar = {}, onAbrirConfiguracion = {})
    }
}
