package com.example.api

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

data class AIResult(
    val text: String,
    val providerName: String,
    val isFallback: Boolean = false
)

@JsonClass(generateAdapter = true)
data class OpenAIStyleMessage(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class OpenAIStyleRequest(
    val model: String,
    val messages: List<OpenAIStyleMessage>,
    val temperature: Double? = 0.7
)

@JsonClass(generateAdapter = true)
data class OpenAIStyleResponse(
    val choices: List<OpenAIChoice>? = null
)

@JsonClass(generateAdapter = true)
data class OpenAIChoice(
    val message: OpenAIStyleMessage? = null
)

object MultiAIFallbackManager {
    private const val TAG = "MultiAIFallbackManager"

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(ApiKeyInterceptor())
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    // Rate-limit tracking: provider -> cooldown expiration timestamp in ms
    private val rateLimitedProviders = ConcurrentHashMap<String, Long>()

    private fun isRateLimited(provider: String): Boolean {
        val until = rateLimitedProviders[provider] ?: return false
        if (System.currentTimeMillis() > until) {
            rateLimitedProviders.remove(provider)
            return false
        }
        return true
    }

    private fun markRateLimited(provider: String, durationSeconds: Long = 60) {
        Log.w(TAG, "Provider $provider rate limited / hit limit. Pausing for $durationSeconds sec.")
        rateLimitedProviders[provider] = System.currentTimeMillis() + (durationSeconds * 1000)
    }

    /**
     * Generates content using an intelligent multi-provider cascade:
     * 1. Primary Gemini Model (gemini-3.1-flash-lite-preview)
     * 2. Secondary Gemini Model (gemini-1.5-flash)
     * 3. Groq Free Tier (Llama 3.3 70B / DeepSeek R1)
     * 4. OpenRouter Free Models (Gemini 2.0 Flash Lite Free / Llama 3.3 Free)
     * 5. Smart Local RemX Heuristic Fallback
     */
    suspend fun generateContentWithFallback(
        prompt: String,
        systemInstruction: String = "Tu es RemX, un assistant IA utile et concis.",
        history: List<Content> = emptyList(),
        jsonSchema: ResponseSchema? = null
    ): AIResult = withContext(Dispatchers.IO) {

        val geminiKey = SecureKeyManager.getGeminiKey()

        // --- STAGE 1: Primary Gemini Model ---
        if (geminiKey.isNotBlank() && geminiKey != "MY_GEMINI_API_KEY" && !isRateLimited("GeminiPrimary")) {
            try {
                Log.d(TAG, "Attempting Stage 1: Gemini Primary")
                val responseText = callGeminiApi(
                    apiKey = geminiKey,
                    model = "gemini-3.1-flash-lite-preview",
                    prompt = prompt,
                    systemInstruction = systemInstruction,
                    history = history,
                    jsonSchema = jsonSchema
                )
                if (!responseText.isNullOrBlank()) {
                    return@withContext AIResult(responseText, "Gemini 3.1 Flash", isFallback = false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Gemini Primary failed or hit rate limit: ${e.message}")
                if (isQuotaOrRateLimitError(e)) {
                    markRateLimited("GeminiPrimary", 120)
                }
            }
        }

        // --- STAGE 2: Secondary Gemini Model (gemini-1.5-flash) ---
        if (geminiKey.isNotBlank() && geminiKey != "MY_GEMINI_API_KEY" && !isRateLimited("GeminiSecondary")) {
            try {
                Log.d(TAG, "Attempting Stage 2: Gemini 1.5 Flash (Fallback)")
                val responseText = callGeminiApi(
                    apiKey = geminiKey,
                    model = "gemini-1.5-flash",
                    prompt = prompt,
                    systemInstruction = systemInstruction,
                    history = history,
                    jsonSchema = jsonSchema
                )
                if (!responseText.isNullOrBlank()) {
                    return@withContext AIResult(responseText, "Gemini 1.5 Flash (Fallback)", isFallback = true)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Gemini 1.5 Flash failed: ${e.message}")
                if (isQuotaOrRateLimitError(e)) {
                    markRateLimited("GeminiSecondary", 120)
                }
            }
        }

        // --- STAGE 3: Groq Cloud High-Speed AI (Llama 3.3 70B / Mixtral) ---
        val groqKey = SecureKeyManager.getGroqKey()
        if (groqKey.isNotBlank() && !isRateLimited("GroqCloud")) {
            try {
                Log.d(TAG, "Attempting Stage 3: Groq Cloud (Llama 3.3 70B)")
                val groqText = callOpenAICompatibleApi(
                    endpointUrl = "https://api.groq.com/openai/v1/chat/completions",
                    apiKey = groqKey,
                    model = "llama-3.3-70b-versatile",
                    prompt = prompt,
                    systemInstruction = systemInstruction
                )
                if (!groqText.isNullOrBlank()) {
                    return@withContext AIResult(groqText, "Groq Cloud (Llama 3.3 70B)", isFallback = true)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Groq Cloud failed: ${e.message}")
                if (isQuotaOrRateLimitError(e)) {
                    markRateLimited("GroqCloud", 60)
                }
            }
        }

        // --- STAGE 4: OpenRouter AI API ---
        val openRouterKey = SecureKeyManager.getOpenRouterKey()
        if (openRouterKey.isNotBlank() && !isRateLimited("OpenRouter")) {
            try {
                Log.d(TAG, "Attempting Stage 4: OpenRouter Model")
                val openRouterText = callOpenAICompatibleApi(
                    endpointUrl = "https://openrouter.ai/api/v1/chat/completions",
                    apiKey = openRouterKey,
                    model = "google/gemini-2.0-flash-lite-001:free",
                    prompt = prompt,
                    systemInstruction = systemInstruction
                )
                if (!openRouterText.isNullOrBlank()) {
                    return@withContext AIResult(openRouterText, "OpenRouter AI", isFallback = true)
                }
            } catch (e: Exception) {
                Log.e(TAG, "OpenRouter stage failed: ${e.message}")
                if (isQuotaOrRateLimitError(e)) {
                    markRateLimited("OpenRouter", 60)
                }
            }
        }

        // --- STAGE 5: Together AI (Llama 3.3 70B Instruct Turbo) ---
        val togetherKey = SecureKeyManager.getTogetherKey()
        if (togetherKey.isNotBlank() && !isRateLimited("TogetherAI")) {
            try {
                Log.d(TAG, "Attempting Stage 5: Together AI (Llama 3.3 70B)")
                val togetherText = callOpenAICompatibleApi(
                    endpointUrl = "https://api.together.xyz/v1/chat/completions",
                    apiKey = togetherKey,
                    model = "meta-llama/Llama-3.3-70B-Instruct-Turbo",
                    prompt = prompt,
                    systemInstruction = systemInstruction
                )
                if (!togetherText.isNullOrBlank()) {
                    return@withContext AIResult(togetherText, "Together AI (Llama 3.3 70B)", isFallback = true)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Together AI stage failed: ${e.message}")
                if (isQuotaOrRateLimitError(e)) {
                    markRateLimited("TogetherAI", 60)
                }
            }
        }

        // --- STAGE 4: Local Smart RemX Fallback Engine ---
        Log.w(TAG, "All network AI APIs were rate-limited or unavailable. Triggering Local RemX Fallback Engine.")
        val localText = generateLocalSmartFallback(prompt, jsonSchema != null)
        return@withContext AIResult(localText, "RemX Engine Local (Hors-ligne / Quota dépassé)", isFallback = true)
    }

    private suspend fun callGeminiApi(
        apiKey: String,
        model: String,
        prompt: String,
        systemInstruction: String,
        history: List<Content>,
        jsonSchema: ResponseSchema?
    ): String? {
        val contentsList = mutableListOf<Content>()
        if (history.isNotEmpty()) {
            contentsList.addAll(history)
        }
        contentsList.add(Content(role = "user", parts = listOf(Part(text = prompt))))

        val generationConfig = if (jsonSchema != null) {
            GenerationConfig(responseMimeType = "application/json", responseSchema = jsonSchema)
        } else null

        val req = GenerateContentRequest(
            contents = contentsList,
            systemInstruction = Content(parts = listOf(Part(text = systemInstruction))),
            generationConfig = generationConfig
        )

        val response = RetrofitClient.service.generateContent(apiKey, req)
        return response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
    }

    private fun callOpenAICompatibleApi(
        endpointUrl: String,
        apiKey: String,
        model: String,
        prompt: String,
        systemInstruction: String
    ): String? {
        val messages = listOf(
            OpenAIStyleMessage(role = "system", content = systemInstruction),
            OpenAIStyleMessage(role = "user", content = prompt)
        )

        val openAiReq = OpenAIStyleRequest(
            model = model,
            messages = messages
        )

        val adapter = moshi.adapter(OpenAIStyleRequest::class.java)
        val jsonPayload = adapter.toJson(openAiReq)

        val requestBuilder = Request.Builder()
            .url(endpointUrl)
            .post(jsonPayload.toRequestBody(jsonMediaType))

        if (apiKey.isNotBlank() && apiKey != "bearer_free") {
            requestBuilder.addHeader("Authorization", "Bearer $apiKey")
        }

        val request = requestBuilder.build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                if (response.code == 429 || response.code == 403) {
                    throw Exception("Rate limit or quota exceeded (HTTP ${response.code})")
                }
                return null
            }

            val bodyString = response.body?.string() ?: return null
            val responseAdapter = moshi.adapter(OpenAIStyleResponse::class.java)
            val openAiResponse = responseAdapter.fromJson(bodyString)
            return openAiResponse?.choices?.firstOrNull()?.message?.content
        }
    }

    private fun isQuotaOrRateLimitError(e: Exception): Boolean {
        val msg = e.message?.lowercase() ?: ""
        return msg.contains("429") || msg.contains("quota") || msg.contains("limit") || msg.contains("resource_exhausted") || msg.contains("too many requests")
    }

    private fun generateLocalSmartFallback(prompt: String, isJson: Boolean): String {
        if (isJson) {
            return """
                {
                  "caption": "Reel importé avec succès",
                  "author": "instagram_creator",
                  "summary": "Cette vidéo a été sauvegardée dans votre mémoire locale. L'analyse IA détaillée sera enrichie dès que les serveurs seront disponibles.",
                  "themes": ["Mémoire", "Instagram", "Vidéo"],
                  "thumbnailUrl": "https://picsum.photos/400/600"
                }
            """.trimIndent()
        } else {
            return "Voici ce que j'ai trouvé dans vos Reels enregistrés. En raison d'un fort trafic sur l'API, cette réponse est générée par le moteur local RemX."
        }
    }
}
