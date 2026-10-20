package com.example.consistentexercise.dominio

data class Logro(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val progreso: Int,
    val objetivo: Int,
    val desbloqueado: Boolean
) {
    val fraccion: Float
        get() = if (desbloqueado) {
            1f
        } else if (objetivo <= 0) {
            1f
        } else {
            (progreso.toFloat() / objetivo).coerceIn(0f, 1f)
        }
}

fun calcularLogros(
    totalSesiones: Int,
    mejorRachaDiaria: Int,
    mejorRachaSemanal: Int,
    metaSemanalActiva: Boolean,
    desbloqueados: Set<String>
): List<Logro> {
    val definiciones = buildList {
        add(Definicion("primera_sesion", "Primer paso", "Completa tu primera sesión", 1, totalSesiones))
        add(Definicion("diez_sesiones", "Constante", "Completa 10 sesiones", 10, totalSesiones))
        add(Definicion("cincuenta_sesiones", "Imparable", "Completa 50 sesiones", 50, totalSesiones))
        add(Definicion("cien_sesiones", "Centenario", "Completa 100 sesiones", 100, totalSesiones))
        add(Definicion("racha_3", "Racha de 3", "Encadena 3 días cumpliendo la meta", 3, mejorRachaDiaria))
        add(Definicion("racha_7", "Racha de 7", "Encadena 7 días cumpliendo la meta", 7, mejorRachaDiaria))
        add(Definicion("racha_30", "Racha de 30", "Encadena 30 días cumpliendo la meta", 30, mejorRachaDiaria))
        if (metaSemanalActiva) {
            add(Definicion("semana_1", "Semana perfecta", "Cumple la meta semanal una vez", 1, mejorRachaSemanal))
            add(Definicion("semanas_4", "Mes constante", "Cumple la meta semanal 4 semanas seguidas", 4, mejorRachaSemanal))
            add(Definicion("semanas_12", "Trimestre de hierro", "Cumple la meta semanal 12 semanas seguidas", 12, mejorRachaSemanal))
        }
    }

    return definiciones.map { definicion ->
        Logro(
            id = definicion.id,
            titulo = definicion.titulo,
            descripcion = definicion.descripcion,
            progreso = definicion.progreso.coerceAtMost(definicion.objetivo),
            objetivo = definicion.objetivo,
            desbloqueado = definicion.id in desbloqueados ||
                definicion.progreso >= definicion.objetivo
        )
    }
}

private data class Definicion(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val objetivo: Int,
    val progreso: Int
)
