package com.example.consistentexercise.datos

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SesionDaoTest {

    private lateinit var baseDatos: BaseDatos
    private lateinit var dao: SesionDao

    @Before
    fun crearBaseDeDatos() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        baseDatos = Room.inMemoryDatabaseBuilder(contexto, BaseDatos::class.java)
            .allowMainThreadQueries()
            .build()
        dao = baseDatos.sesionDao()
    }

    @After
    fun cerrarBaseDeDatos() {
        baseDatos.close()
    }

    @Test
    fun insertaYDevuelveLasSesionesMasRecientesPrimero() = runBlocking {
        dao.insertar(sesion(id = "antigua", finMs = 1_000))
        dao.insertar(sesion(id = "reciente", finMs = 2_000))

        val sesiones = dao.observarTodas().first()

        assertEquals(listOf("reciente", "antigua"), sesiones.map { it.id })
    }

    @Test
    fun elMismoIdNoSeDuplica() = runBlocking {
        val sesion = sesion(id = "unica", finMs = 1_000)

        dao.insertar(sesion)
        dao.insertar(sesion)

        assertEquals(1, dao.observarTodas().first().size)
    }

    @Test
    fun actualizaUnaSesionExistente() = runBlocking {
        val sesion = sesion(id = "editable", finMs = 1_000)
        dao.insertar(sesion)

        dao.actualizar(sesion.copy(conteo = 25, duracionSeg = 120))

        val actualizada = dao.observarTodas().first().first { it.id == "editable" }
        assertEquals(25, actualizada.conteo)
        assertEquals(120, actualizada.duracionSeg)
    }

    @Test
    fun eliminaUnaSesionPorId() = runBlocking {
        dao.insertar(sesion(id = "borrar", finMs = 1_000))
        dao.insertar(sesion(id = "mantener", finMs = 2_000))

        dao.eliminar("borrar")

        assertEquals(listOf("mantener"), dao.observarTodas().first().map { it.id })
    }

    private fun sesion(id: String, finMs: Long) = SesionEntrenamiento(
        id = id,
        inicioMs = finMs - 60_000,
        finMs = finMs,
        fechaLocal = "2026-10-20",
        duracionSeg = 60,
        intervalos = false,
        rondas = 0,
        conteo = 5
    )
}
