package com.example.cpen321application.net

import com.example.cpen321application.BuildConfig
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/** Everything Button 1 shows about the server. */
data class ServerInfo(
    val serverIp: String,
    val clientIp: String,
    val serverTime: String,
    val serverName: String,
)

object BackendApi {
    private val baseUrl: String = BuildConfig.API_BASE_URL.trimEnd('/')

    /** Normal requests: short timeouts. */
    val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    /** WebSocket: no read timeout, and a ping so the connection stays alive. */
    val webSocketClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    private suspend fun getJson(path: String): JSONObject = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(baseUrl + path).build()
        httpClient.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            check(response.isSuccessful) { "HTTP ${response.code} from $path" }
            JSONObject(body)
        }
    }

    /** Calls the three M1 APIs and combines them. */
    suspend fun fetchServerInfo(): ServerInfo {
        val ip = getJson("/api/ip")
        val time = getJson("/api/time")
        val name = getJson("/api/name")
        return ServerInfo(
            serverIp = ip.getString("serverIp"),
            clientIp = ip.getString("clientIp"),
            serverTime = time.getString("serverTime"),
            serverName = name.getString("firstName") + " " + name.getString("lastName"),
        )
    }

    /** https://host -> wss://host/path, so the socket uses the same server. */
    fun webSocketUrl(path: String): String =
        baseUrl.replaceFirst("https://", "wss://").replaceFirst("http://", "ws://") + path
}
