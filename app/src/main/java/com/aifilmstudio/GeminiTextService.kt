package com.aifilmstudio

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Gemini text generation using the user's own API key.
 * No key is embedded in the APK or persisted by this class.
 */
object GeminiTextService {
    suspend fun generateScript(apiKey: String, storyIdea: String): String =
        withContext(Dispatchers.IO) {
            require(apiKey.isNotBlank()) { "API anahtarı boş." }
            require(storyIdea.isNotBlank()) { "Hikâye fikri boş." }

            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" +
                URLEncoder.encode(apiKey, "UTF-8")
            val prompt = """
                Türkçe bir film senaryosu yaz.
                Hikâye fikri: $storyIdea

                Çıktıyı şu yapıda hazırla:
                1. Film adı ve kısa logline
                2. Ana karakterler ve değişmeyen görsel özellikleri
                3. En az 3 sahne; her sahnede mekân, zaman, ışık, kamera ve aksiyon
                4. Karakter adlarıyla doğal Türkçe diyaloglar
                5. Her sahnenin sonunda sonraki sahneye bağlanan hareket/konum notu
                Karakter görünümünü ve kıyafetini sahneler boyunca tutarlı tut.
            """.trimIndent()

            val requestBody = JSONObject()
                .put("contents", JSONArray().put(
                    JSONObject().put("parts", JSONArray().put(JSONObject().put("text", prompt)))
                ))
                .put("generationConfig", JSONObject()
                    .put("temperature", 0.8)
                    .put("maxOutputTokens", 4096)
                )

            val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 20000
                readTimeout = 90000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }

            try {
                connection.outputStream.use { it.write(requestBody.toString().toByteArray(Charsets.UTF_8)) }
                val status = connection.responseCode
                val stream = if (status in 200..299) connection.inputStream else connection.errorStream
                val response = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                if (status !in 200..299) {
                    val message = runCatching {
                        JSONObject(response).optJSONObject("error")?.optString("message")
                    }.getOrNull().orEmpty()
                    throw IllegalStateException(
                        when (status) {
                            400 -> "İstek reddedildi. API anahtarını ve model erişimini kontrol et. $message"
                            403 -> "API anahtarının erişim izni yok. $message"
                            429 -> "Ücretsiz kota veya hız sınırı dolmuş. Daha sonra tekrar dene. $message"
                            else -> "Gemini HTTP $status. $message"
                        }
                    )
                }

                val json = JSONObject(response)
                val parts = json.optJSONArray("candidates")
                    ?.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")
                    ?: throw IllegalStateException("Gemini boş yanıt döndürdü.")
                val output = buildString {
                    for (i in 0 until parts.length()) {
                        val text = parts.optJSONObject(i)?.optString("text").orEmpty()
                        if (text.isNotBlank()) {
                            if (isNotEmpty()) append("\n")
                            append(text)
                        }
                    }
                }
                output.ifBlank { throw IllegalStateException("Gemini yanıtında metin bulunamadı.") }
            } finally {
                connection.disconnect()
            }
        }
}
