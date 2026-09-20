package com.example.cpen321application

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cpen321application.ui.PixelArtScreen
import com.example.cpen321application.ui.ServerInfoScreen
import com.example.cpen321application.ui.TimerScreen
import com.example.cpen321application.ui.theme.CPEN321ApplicationTheme

/** The four screens of the M1 app. */
private enum class Screen { HOME, SERVER_INFO, PIXELS, TIMER }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CPEN321ApplicationTheme {
                AppRoot()
            }
        }
    }
}

@Composable
private fun AppRoot() {
    var screen by remember { mutableStateOf(Screen.HOME) }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (screen != Screen.HOME) {
                TextButton(onClick = { screen = Screen.HOME }) {
                    Text("< Back")
                }
            }
            when (screen) {
                Screen.HOME -> HomeScreen(onNavigate = { screen = it })
                Screen.SERVER_INFO -> ServerInfoScreen()
                Screen.PIXELS -> PixelArtScreen()
                Screen.TIMER -> TimerScreen()
            }
        }
    }
}

@Composable
private fun HomeScreen(onNavigate: (Screen) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("CPEN 321 - M1", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(48.dp))

        Button(
            onClick = { onNavigate(Screen.SERVER_INFO) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("1. Login + Server")
        }
        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { onNavigate(Screen.PIXELS) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("2. Live Updates")
        }
        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { onNavigate(Screen.TIMER) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("3. Timer")
        }
    }
}
