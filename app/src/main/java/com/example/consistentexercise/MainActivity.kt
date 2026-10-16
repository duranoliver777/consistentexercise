package com.example.consistentexercise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.consistentexercise.ui.theme.ConsistentExerciseTheme

private enum class Pantalla { BIENVENIDA, TEMPORIZADOR, CONFIGURACION }

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
    var pantalla by rememberSaveable { mutableStateOf(Pantalla.BIENVENIDA) }
    var pantallaAnterior by rememberSaveable { mutableStateOf(Pantalla.BIENVENIDA) }
    val stateHolder = rememberSaveableStateHolder()

    BackHandler(enabled = pantalla != Pantalla.BIENVENIDA) {
        pantalla = if (pantalla == Pantalla.CONFIGURACION) pantallaAnterior else Pantalla.BIENVENIDA
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        stateHolder.SaveableStateProvider(pantalla) {
            val contenidoModifier = Modifier.padding(innerPadding)
            when (pantalla) {
                Pantalla.BIENVENIDA -> BienvenidaScreen(
                    onContinuar = { pantalla = Pantalla.TEMPORIZADOR },
                    onAbrirConfiguracion = {
                        pantallaAnterior = pantalla
                        pantalla = Pantalla.CONFIGURACION
                    },
                    modifier = contenidoModifier
                )
                Pantalla.TEMPORIZADOR -> TemporizadorScreen(
                    onAbrirConfiguracion = {
                        pantallaAnterior = pantalla
                        pantalla = Pantalla.CONFIGURACION
                    },
                    modifier = contenidoModifier
                )
                Pantalla.CONFIGURACION -> ConfiguracionScreen(
                    onVolver = { pantalla = pantallaAnterior },
                    modifier = contenidoModifier
                )
            }
        }
    }
}
