package com.example.consistentexercise.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AjusteTiempo(
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

@Composable
fun AjusteCantidad(
    valor: Int,
    onDecrementar: () -> Unit,
    onIncrementar: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onDecrementar,
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
            onClick = onIncrementar,
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

@Composable
fun SeccionAjustes(
    titulo: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, AzulClaro)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
            content()
        }
    }
}

@Composable
fun FilaAjuste(
    etiqueta: String,
    descripcion: String? = null,
    control: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = etiqueta, color = Color.Black)
            if (descripcion != null) {
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black.copy(alpha = 0.7f)
                )
            }
        }
        control()
    }
}

@Composable
fun SwitchAjuste(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = AzulBoton,
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = AzulClaro.copy(alpha = 0.6f),
            uncheckedBorderColor = AzulClaro
        )
    )
}
