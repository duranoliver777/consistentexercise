package com.example.consistentexercise.datos

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [SesionEntrenamiento::class],
    version = 1,
    exportSchema = false
)
abstract class BaseDatos : RoomDatabase() {

    abstract fun sesionDao(): SesionDao

    companion object {
        @Volatile
        private var instancia: BaseDatos? = null

        fun obtener(contexto: Context): BaseDatos =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    contexto.applicationContext,
                    BaseDatos::class.java,
                    "consistent_exercise.db"
                ).build().also { instancia = it }
            }
    }
}
