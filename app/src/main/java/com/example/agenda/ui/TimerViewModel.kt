package com.example.agenda.ui

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agenda.model.Actividad
import kotlinx.coroutines.*

class TimerViewModel : ViewModel() {

    var uiState by mutableStateOf(TimerUiState())
        private set

    private var timerJob: Job? = null

    fun agregarActividad(nombre: String, segundos: Long, esCiclo: Boolean) {
        val nuevaActividad = Actividad(
            id = if (uiState.listaActividades.isEmpty()) 0 else uiState.listaActividades.maxOf { it.id } + 1,
            nombre = nombre,
            duracionSegundos = segundos,
            esCiclo = esCiclo
        )
        val nuevaLista = uiState.listaActividades + nuevaActividad
        uiState = uiState.copy(
            listaActividades = nuevaLista,
            tiempoRestante = if (nuevaLista.size == 1) segundos else uiState.tiempoRestante,
            progreso = if (nuevaLista.size == 1) 1f else uiState.progreso
        )
    }

    fun configurarCiclos(total: Int) {
        uiState = uiState.copy(totalCiclos = total)
    }

    fun mostrarConfiguracion(mostrar: Boolean) {
        if (mostrar) {
            timerJob?.cancel()
            uiState = uiState.copy(estaCorriendo = false, mostrarConfiguracion = true)
        } else {
            uiState = uiState.copy(mostrarConfiguracion = false)
            reiniciarEstadoTimer()
        }
    }

    private fun reiniciarEstadoTimer() {
        val primeraActividad = uiState.listaActividades.firstOrNull()
        uiState = uiState.copy(
            indiceActividadActual = 0,
            tiempoRestante = primeraActividad?.duracionSegundos ?: 0L,
            progreso = 1f,
            cicloActual = 1,
            estaCorriendo = false
        )
    }

    fun iniciarOPausar() {
        if (uiState.estaCorriendo) {
            timerJob?.cancel()
            uiState = uiState.copy(estaCorriendo = false)
        } else {
            if (uiState.listaActividades.isEmpty()) return
            
            uiState = uiState.copy(estaCorriendo = true)
            timerJob = viewModelScope.launch {
                ejecutarTemporizador()
            }
        }
    }

    private suspend fun ejecutarTemporizador() {
        while (uiState.cicloActual <= uiState.totalCiclos) {
            // En el primer ciclo ejecutamos todas. En los siguientes, solo las de ciclo.
            val actividadesAFiltrar = if (uiState.cicloActual == 1) {
                uiState.listaActividades
            } else {
                uiState.listaActividades.filter { it.esCiclo }
            }

            if (actividadesAFiltrar.isEmpty()) break

            // Empezamos desde el índice actual (por si se pausó)
            for (i in uiState.indiceActividadActual until actividadesAFiltrar.size) {
                val actividad = actividadesAFiltrar[i]
                
                // Actualizamos el índice real para que la UI sepa qué actividad se muestra
                // Nota: Aquí hay un detalle, si filtramos, el 'indiceActividadActual' 
                // debería referirse a la lista filtrada.
                
                if (uiState.tiempoRestante <= 0) {
                    uiState = uiState.copy(
                        tiempoRestante = actividad.duracionSegundos,
                        progreso = 1f
                    )
                }

                while (uiState.tiempoRestante > 0) {
                    delay(1000L)
                    val nuevoTiempo = uiState.tiempoRestante - 1
                    uiState = uiState.copy(
                        tiempoRestante = nuevoTiempo,
                        progreso = nuevoTiempo.toFloat() / actividad.duracionSegundos.toFloat()
                    )
                }

                // Actividad terminada
                delay(500)
                if (i + 1 < actividadesAFiltrar.size) {
                    val siguienteActividad = actividadesAFiltrar[i + 1]
                    uiState = uiState.copy(
                        indiceActividadActual = i + 1,
                        tiempoRestante = siguienteActividad.duracionSegundos,
                        progreso = 1f
                    )
                } else {
                    uiState = uiState.copy(indiceActividadActual = i + 1)
                }
            }

            // Terminó la lista de este ciclo
            val siguienteCiclo = uiState.cicloActual + 1
            if (siguienteCiclo <= uiState.totalCiclos) {
                // Verificar si hay actividades para el siguiente ciclo
                val hayMasActividades = uiState.listaActividades.any { it.esCiclo }
                if (hayMasActividades) {
                    val primeraDeCiclo = uiState.listaActividades.first { it.esCiclo }
                    uiState = uiState.copy(
                        cicloActual = siguienteCiclo,
                        indiceActividadActual = 0,
                        tiempoRestante = primeraDeCiclo.duracionSegundos,
                        progreso = 1f
                    )
                } else {
                    break
                }
            } else {
                break
            }
        }
        uiState = uiState.copy(estaCorriendo = false)
    }

    fun reiniciar() {
        timerJob?.cancel()
        reiniciarEstadoTimer()
    }

    fun eliminarActividad(id: Int) {
        val nuevaLista = uiState.listaActividades.filter { it.id != id }
        uiState = uiState.copy(listaActividades = nuevaLista)
    }
}
