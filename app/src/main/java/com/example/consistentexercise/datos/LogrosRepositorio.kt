package com.example.consistentexercise.datos

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.logrosDataStore: DataStore<Preferences> by preferencesDataStore(name = "logros")

class LogrosRepositorio(private val context: Context) {

    val desbloqueados: Flow<Set<String>> = context.logrosDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferencias -> preferencias[CLAVE_DESBLOQUEADOS] ?: emptySet() }

    suspend fun desbloquear(ids: Set<String>) {
        if (ids.isEmpty()) return
        context.logrosDataStore.edit { preferencias ->
            val actuales = preferencias[CLAVE_DESBLOQUEADOS] ?: emptySet()
            preferencias[CLAVE_DESBLOQUEADOS] = actuales + ids
        }
    }

    private companion object {
        val CLAVE_DESBLOQUEADOS = stringSetPreferencesKey("desbloqueados")
    }
}
