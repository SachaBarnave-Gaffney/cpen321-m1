package com.example.cpen321application.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cpen321application.net.fetchRandomFact
import kotlinx.coroutines.delay

/** Button 3: a countdown timer that reveals a random fun fact when it ends. */
@Composable
fun TimerScreen(modifier: Modifier = Modifier) {
    var minutesText by remember { mutableStateOf("0") }
    var secondsText by remember { mutableStateOf("10") }
    var remaining by remember { mutableStateOf(0) }
    var running by remember { mutableStateOf(false) }
    var fact by remember { mutableStateOf<String?>(null) }
    var loadingFact by remember { mutableStateOf(false) }

    LaunchedEffect(running) {
        if (!running) return@LaunchedEffect
        while (remaining > 0) {
            delay(1000)
            remaining -= 1
        }
        // Fetch BEFORE clearing `running`: changing that state restarts this
        // LaunchedEffect, which would cancel the request half-way.
        loadingFact = true
        fact = try {
            fetchRandomFact()
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            "Time's up! (Could not load a fact: " + (e.message ?: "unknown error") + ")"
        }
        loadingFact = false
        running = false
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Timer + Surprise", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = minutesText,
                    onValueChange = { minutesText = it.filter { c -> c.isDigit() }.take(3) },
                    label = { Text("Minutes") },
                    singleLine = true,
                    enabled = !running,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = secondsText,
                    onValueChange = { secondsText = it.filter { c -> c.isDigit() }.take(2) },
                    label = { Text("Seconds") },
                    singleLine = true,
                    enabled = !running,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text = formatRemaining(remaining),
                style = MaterialTheme.typography.displayMedium,
            )
            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    enabled = !running,
                    onClick = {
                        val total =
                            (minutesText.toIntOrNull() ?: 0) * 60 + (secondsText.toIntOrNull() ?: 0)
                        if (total > 0) {
                            fact = null
                            remaining = total
                            running = true
                        }
                    },
                ) {
                    Text("Start")
                }
                Button(
                    enabled = running,
                    onClick = {
                        running = false
                        remaining = 0
                    },
                ) {
                    Text("Cancel")
                }
            }

            if (loadingFact) {
                Spacer(Modifier.height(24.dp))
                Text("Loading your surprise...")
            }

            val currentFact = fact
            if (currentFact != null) {
                Spacer(Modifier.height(24.dp))
                SurpriseCard(currentFact)
            }
        }

        // Confetti burst on top of the screen, restarted for each new surprise.
        val factForConfetti = fact
        if (factForConfetti != null) {
            ConfettiOverlay(restartKey = factForConfetti, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun SurpriseCard(fact: String) {
    // Gentle pulse so the reveal feels like a surprise.
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "scale",
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Time's up!", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            Text("Random fact of the moment", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                text = fact,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

private fun formatRemaining(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return minutes.toString().padStart(2, '0') + ":" + seconds.toString().padStart(2, '0')
}
