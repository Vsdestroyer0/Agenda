package com.example.agenda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.agenda.ui.TimerScreen
import com.example.agenda.ui.TimerViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val timerViewModel: TimerViewModel = viewModel()

            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    TimerScreen(timerViewModel)
                }
            }
        }
    }
}
