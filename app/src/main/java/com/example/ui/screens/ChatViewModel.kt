package com.example.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.api.*
import com.example.data.AppDatabase
import com.example.data.Highlight
import com.example.data.HighlightDao
import com.example.data.RecentQuery
import com.example.data.Reel
import com.example.data.ReelRepository
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.sqrt

enum class Role { USER, MODEL }

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val role: Role,
    val referencedReels: List<Reel> = emptyList()
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ReelRepository(AppDatabase.getDatabase(application).reelDao())
    private val recentQueryDao = AppDatabase.getDatabase(application).recentQueryDao()
    private val highlightDao = AppDatabase.getDatabase(application).highlightDao()
    
    val recentQueries = recentQueryDao.getRecentQueries()
    val highlights = highlightDao.getAllHighlights()

    fun saveHighlight(text: String) {
        viewModelScope.launch {
            highlightDao.insertHighlight(Highlight(text = text))
        }
    }

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    private val apiKey = BuildConfig.GEMINI_API_KEY
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    // Structured output class
    private data class GeminiParsedResponse(
        val answer: String,
        val reelIds: List<String> = emptyList()
    )

    fun sendMessage(text: String, userId: String) {
        val userMsg = ChatMessage(text = text, role = Role.USER)
        _messages.value = _messages.value + userMsg
        _isTyping.value = true

        viewModelScope.launch {
            try {
                // Save recent query
                recentQueryDao.insertQuery(RecentQuery(query = text))
                recentQueryDao.deleteOldQueries()

                // 1. Get embedding for the user's question
                val embedReq = EmbedContentRequest(
                    model = "models/gemini-embedding-2-preview",
                    content = Content(parts = listOf(Part(text = text)))
                )
                val embedRes = RetrofitClient.service.embedContent(apiKey, embedReq)
                val queryEmbedding = embedRes.embedding?.values ?: emptyList()

                var relevantReels = emptyList<Reel>()
                
                if (queryEmbedding.isNotEmpty()) {
                    // 2. Find nearest reels in Room
                    val allReels = repository.allReels.first().filter { 
                        it.userId == userId && it.status == "done" && it.embedding.isNotEmpty()
                    }

                    val scoredReels = allReels.map { reel ->
                        val score = cosineSimilarity(queryEmbedding, reel.embedding)
                        Pair(reel, score)
                    }

                    relevantReels = scoredReels.sortedByDescending { it.second }.take(5).map { it.first }
                }

                // 3. Prompt Gemini with structured output request
                val systemInstruction = """
                    Tu es un assistant IA pour l'application RemX. Ton but est d'aider l'utilisateur à retrouver ou à se souvenir de ce qu'il a enregistré.
                    Tu vas recevoir la question de l'utilisateur et une liste de "Reels" (vidéos/posts) pertinents trouvés dans sa base de données.
                    Réponds à la question en te basant UNIQUEMENT sur ces reels.
                    Si aucun reel ne semble pertinent, dis honnêtement que tu n'as rien trouvé qui correspond à sa demande, plutôt que d'inventer.
                    
                    Tu DOIS répondre au format JSON strict suivant :
                    {
                        "answer": "Ta réponse texte formatée",
                        "reelIds": ["id1", "id2"] // Les IDs des reels que tu as utilisés pour formuler ta réponse
                    }
                """.trimIndent()

                val contextStr = relevantReels.joinToString(separator = "\n\n") {
                    "Reel ID: ${it.id}\nCaption: ${it.caption}\nSummary: ${it.summary}\nTranscript (excerpt): ${it.transcript.take(300)}"
                }

                val prompt = "Question: $text\n\nReels disponibles:\n$contextStr"

                val schema = ResponseSchema(
                    type = "OBJECT",
                    properties = mapOf(
                        "answer" to ResponseSchemaProperty(type = "STRING", description = "La réponse à la question de l'utilisateur"),
                        "reelIds" to ResponseSchemaProperty(
                            type = "ARRAY",
                            items = ResponseSchema(type = "STRING", description = "L'ID du reel"),
                            description = "Les IDs des reels utilisés pour la réponse"
                        )
                    ),
                    required = listOf("answer", "reelIds")
                )

                val req = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                    systemInstruction = Content(parts = listOf(Part(text = systemInstruction))),
                    generationConfig = GenerationConfig(
                        responseMimeType = "application/json",
                        responseSchema = schema
                    )
                )

                val response = RetrofitClient.service.generateContent(apiKey, req)
                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "{}"
                
                // 4. Parse the JSON and create model message
                val adapter = moshi.adapter(GeminiParsedResponse::class.java)
                val parsed = try {
                    adapter.fromJson(responseText)
                } catch(e: Exception) {
                    null
                }

                val answerText = parsed?.answer ?: "Désolé, je n'ai pas pu générer une réponse."
                val usedReelIds = parsed?.reelIds ?: emptyList()
                val usedReels = relevantReels.filter { usedReelIds.contains(it.id) }

                val modelMsg = ChatMessage(text = answerText, role = Role.MODEL, referencedReels = usedReels)
                _messages.value = _messages.value + modelMsg

            } catch (e: Exception) {
                e.printStackTrace()
                val errorMsg = ChatMessage(text = "Oups, une erreur est survenue (${e.message}).", role = Role.MODEL)
                _messages.value = _messages.value + errorMsg
            } finally {
                _isTyping.value = false
            }
        }
    }

    private fun cosineSimilarity(v1: List<Double>, v2: List<Double>): Double {
        if (v1.size != v2.size || v1.isEmpty()) return 0.0
        var dotProduct = 0.0
        var normA = 0.0
        var normB = 0.0
        for (i in v1.indices) {
            dotProduct += v1[i] * v2[i]
            normA += v1[i] * v1[i]
            normB += v2[i] * v2[i]
        }
        return if (normA == 0.0 || normB == 0.0) 0.0 else dotProduct / (sqrt(normA) * sqrt(normB))
    }
}
