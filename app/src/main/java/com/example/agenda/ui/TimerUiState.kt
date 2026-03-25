package com.example.agenda.ui

import com.example.agenda.model.Actividad

data class TimerUiState(
    val listaActividades: List<Actividad> = emptyList(),
    val indiceActividadActual: Int = 0,
    val tiempoRestante: Long = 0L,
    val estaCorriendo: Boolean = false,
    val progreso: Float = 1f,
    val cicloActual: Int = 1,
    val totalCiclos: Int = 1,
    val mostrarConfiguracion: Boolean = true
) {
    val actividadActual: Actividad? = if (listaActividades.isNotEmpty()) listaActividades[indiceActividadActual] else null
    val nombreActividad: String = actividadActual?.nombre ?: "Sin Actividades"
}
