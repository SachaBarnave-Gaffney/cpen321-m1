package com.example.cpen321application.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cpen321application.BuildConfig
import com.example.cpen321application.auth.SignedInUser
import com.example.cpen321application.auth.signInWithGoogle
import com.example.cpen321application.net.BackendApi
import com.example.cpen321application.net.ServerInfo
import com.example.cpen321application.util.currentLocalTime
import kotlinx.coroutines.launch

/** Button 1: Google sign-in, then the server/client details from the backend. */
@Composable
fun ServerInfoScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var user by remember { mutableStateOf<SignedInUser?>(null) }
    var info by remember { mutableStateOf<ServerInfo?>(null) }
    var clientTime by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Login + Server Info", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(24.dp))

        if (user == null) {
            Button(
                enabled = !loading,
                onClick = {
                    scope.launch {
                        loading = true
                        error = null
                        try {
                            user = signInWithGoogle(context, BuildConfig.GOOGLE_CLIENT_ID)
                            clientTime = currentLocalTime()
                            info = BackendApi.fetchServerInfo()
                        } catch (e: Exception) {
                            error = signInErrorMessage(e)
                        } finally {
                            loading = false
                        }
                    }
                },
            ) {
                Text("Sign in with Google")
            }
        }

        if (loading) {
            Spacer(Modifier.height(24.dp))
            CircularProgressIndicator()
        }

        val currentInfo = info
        val currentUser = user
        if (currentInfo != null && currentUser != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    InfoRow("Signed in as", currentUser.displayName)
                    InfoRow("Server public IP", currentInfo.serverIp)
                    InfoRow("Client IP", currentInfo.clientIp)
                    InfoRow("Server local time", currentInfo.serverTime)
                    InfoRow("Client local time", clientTime)
                    InfoRow("My name (from API)", currentInfo.serverName)
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                scope.launch {
                    loading = true
                    try {
                        clientTime = currentLocalTime()
                        info = BackendApi.fetchServerInfo()
                    } catch (e: Exception) {
                        error = e.message
                    } finally {
                        loading = false
                    }
                }
            }) {
                Text("Refresh")
            }
        }

        val currentError = error
        if (currentError != null) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Error: $currentError",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            modifier = Modifier.weight(0.45f),
            style = MaterialTheme.typography.labelLarge,
        )
        Text(
            text = value,
            modifier = Modifier.weight(0.55f),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

/** Turns Credential Manager failures into something a user can act on. */
private fun signInErrorMessage(e: Exception): String = when (e) {
    is androidx.credentials.exceptions.NoCredentialException ->
        "No Google account on this device. Add one in Settings > Passwords & accounts, then try again."
    is androidx.credentials.exceptions.GetCredentialCancellationException ->
        "Sign-in was cancelled."
    else -> e.message ?: e.javaClass.simpleName
}
