package com.example.consistentexercise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.consistentexercise.ui.theme.ConsistentExerciseTheme

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
    var enTemporizador by rememberSaveable { mutableStateOf(false) }
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        if (enTemporizador) {
            TemporizadorScreen(modifier = Modifier.padding(innerPadding))
        } else {
            BienvenidaScreen(
                onContinuar = { enTemporizador = true },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
