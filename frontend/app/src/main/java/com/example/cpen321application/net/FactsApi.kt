package com.example.cpen321application.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONObject

/** Button 3's surprise: a random useless fact from a free public API. */
suspend fun fetchRandomFact(): String = withContext(Dispatchers.IO) {
    val request = Request.Builder()
        .url("https://uselessfacts.jsph.pl/api/v2/facts/random?language=en")
        .build()
    BackendApi.httpClient.newCall(request).execute().use { response ->
        val body = response.body?.string().orEmpty()
        check(response.isSuccessful) { "HTTP ${response.code} from facts API" }
        JSONObject(body).getString("text")
    }
}
