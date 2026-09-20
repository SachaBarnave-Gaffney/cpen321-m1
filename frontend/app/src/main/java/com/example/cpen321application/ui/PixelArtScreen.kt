package com.example.cpen321application.ui

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.cpen321application.net.BackendApi
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject

private const val GRID = 16

/** Button 2: paints a 16x16 grid live from pixels relayed by our backend. */
@Composable
fun PixelArtScreen(modifier: Modifier = Modifier) {
    val cells = remember { mutableStateListOf<Color>().apply { repeat(GRID * GRID) { add(Color.White) } } }
    var status by remember { mutableStateOf("Connecting...") }
    var pixelCount by remember { mutableStateOf(0) }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    DisposableEffect(Unit) {
        val request = Request.Builder().url(BackendApi.webSocketUrl("/pixels")).build()
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                mainHandler.post { status = "Connected - waiting for pixels" }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                // Each message is one pixel: {"x":3,"y":7,"color":"#FFD700"}
                val parsed = runCatching {
                    val json = JSONObject(text)
                    Triple(
                        json.getInt("x"),
                        json.getInt("y"),
                        Color(android.graphics.Color.parseColor(json.getString("color"))),
                    )
                }.getOrNull() ?: return

                val (x, y, color) = parsed
                if (x !in 0 until GRID || y !in 0 until GRID) return

                mainHandler.post {
                    cells[y * GRID + x] = color
                    pixelCount++
                    status = "Live"
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                mainHandler.post { status = "Error: " + (t.message ?: t.javaClass.simpleName) }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                mainHandler.post { status = "Disconnected" }
            }
        }

        val socket = BackendApi.webSocketClient.newWebSocket(request, listener)
        onDispose { socket.close(1000, "Screen closed") }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Pixel Art (live)", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text("$status - $pixelCount pixels", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .border(2.dp, MaterialTheme.colorScheme.outline),
        ) {
            for (y in 0 until GRID) {
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    for (x in 0 until GRID) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .background(cells[y * GRID + x]),
                        )
                    }
                }
            }
        }
    }
}
