package com.example.consistentexercise.datos

import kotlinx.coroutines.flow.Flow

class SesionRepositorio(private val dao: SesionDao) {

    val sesiones: Flow<List<SesionEntrenamiento>> = dao.observarTodas()

    suspend fun guardar(sesion: SesionEntrenamiento) {
        dao.insertar(sesion)
    }

    suspend fun actualizar(sesion: SesionEntrenamiento) {
        dao.actualizar(sesion)
    }

    suspend fun eliminar(id: String) {
        dao.eliminar(id)
    }
}
