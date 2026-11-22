package com.aifilmstudio

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class VideoJobResult(val id: String, val status: String, val error: String? = null)

object VideoGenerationService {
    suspend fun createJob(baseUrl: String, token: String, prompt: String): VideoJobResult =
        withContext(Dispatchers.IO) {
            val body = JSONObject()
                .put("prompt", prompt)
                .put("num_frames", 49)
                .put("width", 832)
                .put("height", 480)
                .put("steps", 20)
            val json = request(
                url = baseUrl.trimEnd('/') + "/v1/videos",
                token = token,
                method = "POST",
                body = body.toString()
            )
            VideoJobResult(
                id = json.getString("id"),
                status = json.optString("status", "queued")
            )
        }

    suspend fun getJob(baseUrl: String, token: String, jobId: String): VideoJobResult =
        withContext(Dispatchers.IO) {
            val json = request(
                url = baseUrl.trimEnd('/') + "/v1/videos/$jobId",
                token = token,
                method = "GET"
            )
            VideoJobResult(
                id = json.optString("id", jobId),
                status = json.optString("status", "unknown"),
                error = json.optString("error").takeIf { it.isNotBlank() && it != "null" }
            )
        }

    private fun request(url: String, token: String, method: String, body: String? = null): JSONObject {
        require(url.startsWith("https://")) { "Güvenlik için sunucu adresi HTTPS olmalı." }
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 20_000
            readTimeout = 60_000
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Accept", "application/json")
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
        }
        try {
            if (body != null) {
                connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val response = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) {
                val detail = runCatching { JSONObject(response).optString("detail") }.getOrDefault(response)
                throw IllegalStateException("Sunucu ($code): $detail")
            }
            return JSONObject(response)
        } finally {
            connection.disconnect()
        }
    }
}
