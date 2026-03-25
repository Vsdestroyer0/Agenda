package com.example.agenda.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TimerScreen(viewModel: TimerViewModel) {
    val state = viewModel.uiState

    if (state.mostrarConfiguracion) {
        ConfiguracionScreen(viewModel)
    } else {
        EjecucionTimerScreen(viewModel)
    }
}

@Composable
fun ConfiguracionScreen(viewModel: TimerViewModel) {
    val state = viewModel.uiState
    var nombreActividad by remember { mutableStateOf("") }
    var duracionActividad by remember { mutableStateOf("") }
    var esCiclo by remember { mutableStateOf(false) }
    var ciclos by remember { mutableStateOf(state.totalCiclos.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Configurar Agenda",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = nombreActividad,
            onValueChange = { nombreActividad = it },
            label = { Text("Nombre de la actividad") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = duracionActividad,
                onValueChange = { duracionActividad = it },
                label = { Text("Segundos") },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = esCiclo, onCheckedChange = { esCiclo = it })
                Text("¿Es de ciclo?")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val segundos = duracionActividad.toLongOrNull() ?: 0L
                if (nombreActividad.isNotBlank() && segundos > 0) {
                    viewModel.agregarActividad(nombreActividad, segundos, esCiclo)
                    nombreActividad = ""
                    duracionActividad = ""
                    esCiclo = false
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Agregar Actividad")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Lista de Actividades",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp)
        ) {
            items(state.listaActividades) { actividad ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (actividad.esCiclo) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = actividad.nombre + if (actividad.esCiclo) " (Ciclo)" else " (Única)",
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = "${actividad.duracionSegundos} segundos", style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { viewModel.eliminarActividad(actividad.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                        }
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Repeticiones de ciclo: ", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = ciclos,
                onValueChange = { 
                    ciclos = it
                    it.toIntOrNull()?.let { num -> viewModel.configurarCiclos(num) }
                },
                modifier = Modifier.width(80.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.mostrarConfiguracion(false) },
            modifier = Modifier.fillMaxWidth(),
            enabled = state.listaActividades.isNotEmpty(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("COMENZAR AGENDA")
        }
    }
}

@Composable
fun EjecucionTimerScreen(viewModel: TimerViewModel) {
    val state = viewModel.uiState

    val minutos = state.tiempoRestante / 60
    val segundos = state.tiempoRestante % 60
    val tiempoTexto = "%02d:%02d".format(minutos, segundos)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (state.actividadActual?.esCiclo == true) {
            Text(
                text = "Ciclo ${state.cicloActual} de ${state.totalCiclos}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Text(
                text = "Actividad Única",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        
        Text(
            text = state.nombreActividad,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(280.dp)) {
            CircularProgressIndicator(
                progress = { state.progreso },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 14.dp,
                color = if (state.estaCorriendo) MaterialTheme.colorScheme.primary else Color.Gray,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Text(
                text = tiempoTexto,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp),
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(50.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = { viewModel.iniciarOPausar() },
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(if (state.estaCorriendo) Icons.Default.Refresh else Icons.Default.PlayArrow, null)
                Spacer(Modifier.width(8.dp))
                Text(if (state.estaCorriendo) "PAUSAR" else "INICIAR")
            }

            OutlinedButton(
                onClick = { viewModel.reiniciar() },
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("REINICIAR")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = { viewModel.mostrarConfiguracion(true) }) {
            Text("Volver a la configuración")
        }
    }
}
