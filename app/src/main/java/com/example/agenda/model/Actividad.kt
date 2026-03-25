package com.example.agenda.model

data class Actividad(
    val id: Int = 0,
    val nombre: String,
    val duracionSegundos: Long,
    val esCiclo: Boolean = false
)