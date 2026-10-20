package com.example.consistentexercise

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import java.time.DayOfWeek
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ajustes")

class AjustesRepositorio(private val context: Context) {

    val ajustes: Flow<Ajustes> = context.dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferencias -> preferencias.leerAjustes() }

    suspend fun actualizar(cambio: (Ajustes) -> Ajustes) {
        context.dataStore.edit { preferencias ->
            preferencias.escribirAjustes(cambio(preferencias.leerAjustes()))
        }
    }

    private companion object {
        val CLAVE_DURACION_MINUTOS = intPreferencesKey("duracion_minutos")
        val CLAVE_DURACION_SEGUNDOS = intPreferencesKey("duracion_segundos")
        val CLAVE_INTERVALOS_ACTIVOS = booleanPreferencesKey("intervalos_activos")
        val CLAVE_PAUSA_MINUTOS = intPreferencesKey("pausa_minutos")
        val CLAVE_PAUSA_SEGUNDOS = intPreferencesKey("pausa_segundos")
        val CLAVE_RONDAS = intPreferencesKey("rondas")
        val CLAVE_META_DIARIA_SESIONES = intPreferencesKey("meta_diaria_sesiones")
        val CLAVE_META_SEMANAL_ACTIVA = booleanPreferencesKey("meta_semanal_activa")
        val CLAVE_META_SEMANAL_SESIONES = intPreferencesKey("meta_semanal_sesiones")
        val CLAVE_INICIO_SEMANA = stringPreferencesKey("inicio_semana")
        val CLAVE_SONIDO_ACTIVO = booleanPreferencesKey("sonido_activo")
        val CLAVE_TONO_URI = stringPreferencesKey("tono_uri")
        val CLAVE_VIBRACION_ACTIVA = booleanPreferencesKey("vibracion_activa")
        val CLAVE_AUTO_INICIAR = booleanPreferencesKey("auto_iniciar")

        fun Preferences.leerAjustes(): Ajustes = Ajustes(
            duracionMinutos = this[CLAVE_DURACION_MINUTOS] ?: 1,
            duracionSegundos = this[CLAVE_DURACION_SEGUNDOS] ?: 0,
            intervalosActivos = this[CLAVE_INTERVALOS_ACTIVOS] ?: false,
            pausaMinutos = this[CLAVE_PAUSA_MINUTOS] ?: 0,
            pausaSegundos = this[CLAVE_PAUSA_SEGUNDOS] ?: 30,
            rondas = this[CLAVE_RONDAS] ?: 3,
            metaDiariaSesiones = this[CLAVE_META_DIARIA_SESIONES] ?: 1,
            metaSemanalActiva = this[CLAVE_META_SEMANAL_ACTIVA] ?: false,
            metaSemanalSesiones = this[CLAVE_META_SEMANAL_SESIONES] ?: 3,
            inicioSemana = runCatching {
                DayOfWeek.valueOf(this[CLAVE_INICIO_SEMANA] ?: DayOfWeek.MONDAY.name)
            }.getOrDefault(DayOfWeek.MONDAY),
            sonidoActivo = this[CLAVE_SONIDO_ACTIVO] ?: true,
            tonoUri = this[CLAVE_TONO_URI],
            vibracionActiva = this[CLAVE_VIBRACION_ACTIVA] ?: false,
            autoIniciar = this[CLAVE_AUTO_INICIAR] ?: false
        )

        fun MutablePreferences.escribirAjustes(ajustes: Ajustes) {
            this[CLAVE_DURACION_MINUTOS] = ajustes.duracionMinutos
            this[CLAVE_DURACION_SEGUNDOS] = ajustes.duracionSegundos
            this[CLAVE_INTERVALOS_ACTIVOS] = ajustes.intervalosActivos
            this[CLAVE_PAUSA_MINUTOS] = ajustes.pausaMinutos
            this[CLAVE_PAUSA_SEGUNDOS] = ajustes.pausaSegundos
            this[CLAVE_RONDAS] = ajustes.rondas
            this[CLAVE_META_DIARIA_SESIONES] = ajustes.metaDiariaSesiones
            this[CLAVE_META_SEMANAL_ACTIVA] = ajustes.metaSemanalActiva
            this[CLAVE_META_SEMANAL_SESIONES] = ajustes.metaSemanalSesiones
            this[CLAVE_INICIO_SEMANA] = ajustes.inicioSemana.name
            this[CLAVE_SONIDO_ACTIVO] = ajustes.sonidoActivo
            if (ajustes.tonoUri == null) {
                remove(CLAVE_TONO_URI)
            } else {
                this[CLAVE_TONO_URI] = ajustes.tonoUri
            }
            this[CLAVE_VIBRACION_ACTIVA] = ajustes.vibracionActiva
            this[CLAVE_AUTO_INICIAR] = ajustes.autoIniciar
        }
    }
}
