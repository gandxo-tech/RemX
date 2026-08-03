package com.example.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.api.*
import com.example.data.AppDatabase
import com.example.data.ChatMessageDao
import com.example.data.ChatMessageEntity
import com.example.data.ChatSessionSummary
import com.example.data.Highlight
import com.example.data.RecentQuery
import com.example.data.Reel
import com.example.data.ReelRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Job
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
    val referencedReels: List<Reel> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ReelRepository(AppDatabase.getDatabase(application).reelDao())
    private val recentQueryDao = AppDatabase.getDatabase(application).recentQueryDao()
    private val highlightDao = AppDatabase.getDatabase(application).highlightDao()
    private val chatMessageDao = AppDatabase.getDatabase(application).chatMessageDao()
    
    val recentQueries = recentQueryDao.getRecentQueries()
    val highlights = highlightDao.getAllHighlights()
    val allReelsFlow = repository.allReels

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _sessions = MutableStateFlow<List<ChatSessionSummary>>(emptyList())
    val sessions: StateFlow<List<ChatSessionSummary>> = _sessions.asStateFlow()

    private val _currentSessionId = MutableStateFlow<String>(UUID.randomUUID().toString())
    val currentSessionId: StateFlow<String> = _currentSessionId.asStateFlow()

    private val _currentSessionTitle = MutableStateFlow<String>("Nouvelle discussion")
    val currentSessionTitle: StateFlow<String> = _currentSessionTitle.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    private var currentUserId: String? = null
    private var messageObserveJob: Job? = null
    private var sessionObserveJob: Job? = null

    private val apiKey = BuildConfig.GEMINI_API_KEY
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    fun loadHistoryForUser(userId: String) {
        if (currentUserId == userId) return
        currentUserId = userId

        // Observe all sessions for this user
        sessionObserveJob?.cancel()
        sessionObserveJob = viewModelScope.launch {
            chatMessageDao.getSessionsForUser(userId).collect { summaries ->
                _sessions.value = summaries
                // If current session is empty or doesn't exist in summaries, we remain on current session
                val active = summaries.find { it.sessionId == _currentSessionId.value }
                if (active != null) {
                    _currentSessionTitle.value = active.sessionTitle
                }
            }
        }

        // Load messages for current session
        observeSessionMessages(userId, _currentSessionId.value)
    }

    private fun observeSessionMessages(userId: String, sessionId: String) {
        messageObserveJob?.cancel()
        messageObserveJob = viewModelScope.launch {
            chatMessageDao.getMessagesForSession(userId, sessionId).collect { entities ->
                val reels = repository.allReels.first()
                val mapped = entities.map { entity ->
                    val referenced = reels.filter { entity.referencedReelIds.contains(it.id) }
                    ChatMessage(
                        id = entity.id,
                        text = entity.text,
                        role = if (entity.role == "USER") Role.USER else Role.MODEL,
                        referencedReels = referenced,
                        timestamp = entity.timestamp
                    )
                }
                _messages.value = mapped
            }
        }
    }

    fun startNewSession(userId: String) {
        val newId = UUID.randomUUID().toString()
        _currentSessionId.value = newId
        _currentSessionTitle.value = "Nouvelle discussion"
        _messages.value = emptyList()
        observeSessionMessages(userId, newId)
    }

    fun selectSession(sessionId: String, userId: String) {
        _currentSessionId.value = sessionId
        val session = _sessions.value.find { it.sessionId == sessionId }
        _currentSessionTitle.value = session?.sessionTitle ?: "Discussion"
        observeSessionMessages(userId, sessionId)
    }

    fun deleteSession(sessionId: String, userId: String) {
        viewModelScope.launch {
            chatMessageDao.deleteSession(userId, sessionId)
            if (_currentSessionId.value == sessionId) {
                startNewSession(userId)
            }
        }
    }

    fun clearAllHistory(userId: String) {
        viewModelScope.launch {
            chatMessageDao.clearHistoryForUser(userId)
            startNewSession(userId)
        }
    }

    fun saveHighlight(text: String) {
        viewModelScope.launch {
            highlightDao.insertHighlight(Highlight(text = text))
        }
    }

    // Structured output class
    private data class GeminiParsedResponse(
        val answer: String,
        val reelIds: List<String> = emptyList()
    )

    fun sendMessage(text: String, userId: String) {
        loadHistoryForUser(userId)
        
        // If this is the first message in the session, derive a title
        if (_messages.value.isEmpty()) {
            val autoTitle = text.take(35).trim().ifEmpty { "Discussion" }
            _currentSessionTitle.value = autoTitle
        }

        val activeSessionId = _currentSessionId.value
        val activeTitle = _currentSessionTitle.value

        val userMsg = ChatMessage(text = text, role = Role.USER)
        _messages.value = _messages.value + userMsg
        _isTyping.value = true

        viewModelScope.launch {
            try {
                // Persist user message in Room DB
                chatMessageDao.insertMessage(
                    ChatMessageEntity(
                        id = userMsg.id,
                        userId = userId,
                        sessionId = activeSessionId,
                        sessionTitle = activeTitle,
                        text = text,
                        role = "USER",
                        timestamp = userMsg.timestamp
                    )
                )

                // Save recent query
                recentQueryDao.insertQuery(RecentQuery(query = text))
                recentQueryDao.deleteOldQueries()

                val allUserReels = repository.allReels.first()

                if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                    val queryLower = text.lowercase()
                    val terms = queryLower.split(" ", ",", "'", "-", "?", "!").filter { it.length > 2 }

                    val matchedReels = if (terms.isEmpty()) {
                        allUserReels.take(4)
                    } else {
                        allUserReels.filter { reel ->
                            val content = "${reel.caption} ${reel.summary} ${reel.author} ${reel.themes.joinToString(" ")} ${reel.transcript}".lowercase()
                            terms.any { term -> content.contains(term) }
                        }
                    }

                    val answerText = if (allUserReels.isEmpty()) {
                        "Vous n'avez encore aucun Reel enregistré dans votre mémoire RemX. Partagez des liens Instagram pour les sauvegarder ici !"
                    } else if (matchedReels.isNotEmpty()) {
                        "Analyse locale RemX (${matchedReels.size} résultat(s) trouvé(s)) :\n" +
                                matchedReels.take(3).joinToString("\n• ", prefix = "• ") { reel ->
                                    if (reel.caption.isNotBlank()) "${reel.caption} (par @${reel.author})" else "${reel.summary} (par @${reel.author})"
                                }
                    } else {
                        "Aucun souvenir spécifique correspondant à \"$text\" n'a été trouvé dans vos souvenirs. Voici vos derniers Reels sauvegardés :"
                    }

                    val referenced = if (matchedReels.isNotEmpty()) matchedReels.take(4) else allUserReels.take(3)

                    val fallbackMsg = ChatMessage(
                        text = answerText,
                        role = Role.MODEL,
                        referencedReels = referenced
                    )
                    _messages.value = _messages.value + fallbackMsg
                    chatMessageDao.insertMessage(
                        ChatMessageEntity(
                            id = fallbackMsg.id,
                            userId = userId,
                            sessionId = activeSessionId,
                            sessionTitle = activeTitle,
                            text = fallbackMsg.text,
                            role = "MODEL",
                            timestamp = fallbackMsg.timestamp,
                            referencedReelIds = fallbackMsg.referencedReels.map { it.id }
                        )
                    )
                    return@launch
                }

                // 1. Semantic Embedding Search
                val relevantReels = mutableListOf<Reel>()
                
                try {
                    val embedReq = EmbedContentRequest(
                        model = "models/gemini-embedding-2-preview",
                        content = Content(parts = listOf(Part(text = text)))
                    )
                    val embedRes = RetrofitClient.service.embedContent(apiKey, embedReq)
                    val queryEmbedding = embedRes.embedding?.values ?: emptyList()

                    if (queryEmbedding.isNotEmpty()) {
                        val allWithEmbeddings = allUserReels.filter { 
                            it.embedding.isNotEmpty()
                        }

                        val scoredReels = allWithEmbeddings.map { reel ->
                            val score = cosineSimilarity(queryEmbedding, reel.embedding)
                            Pair(reel, score)
                        }

                        val semanticHits = scoredReels.filter { it.second > 0.3 }
                            .sortedByDescending { it.second }
                            .map { it.first }
                        
                        relevantReels.addAll(semanticHits)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // 2. Keyword Fallback
                val queryLower = text.lowercase()
                val terms = queryLower.split(" ", ",", "'", "-", "?", "!").filter { it.length > 2 }

                val keywordHits = allUserReels.filter { reel ->
                    val content = "${reel.caption} ${reel.summary} ${reel.author} ${reel.themes.joinToString(" ")} ${reel.transcript}".lowercase()
                    terms.any { term -> content.contains(term) }
                }

                for (reel in keywordHits) {
                    if (relevantReels.none { it.id == reel.id }) {
                        relevantReels.add(reel)
                    }
                }

                // If small collection (e.g. <= 10 reels), include all to give full contextual awareness
                if (allUserReels.isNotEmpty() && relevantReels.isEmpty()) {
                    relevantReels.addAll(allUserReels.take(5))
                }

                val topRelevant = relevantReels.take(6)

                // 3. Prompt Gemini with structured output request
                val systemInstruction = """
                    Tu es l'assistant de mémoire visuelle IA de RemX, conçu par GBAGUIDI Exaucé (Gandxo).
                    Ton rôle est d'aider l'utilisateur à retrouver ses souvenirs et Reels sauvegardés sur Instagram.
                    
                    Tu as accès aux Reels extraits de la mémoire locale de l'utilisateur.
                    
                    Instructions :
                    1. Réponds de façon chaleureuse, précise et structurée.
                    2. Si des Reels correspondent, cite l'auteur et les détails pertinents (recettes, astuces, tutoriels).
                    3. Si aucun Reel ne correspond exactement, donne des conseils utiles ou des pistes basées sur la mémoire disponible.
                    
                    Format JSON obligatoire :
                    {
                        "answer": "Texte de la réponse",
                        "reelIds": ["id1", "id2"]
                    }
                """.trimIndent()

                val contextStr = if (topRelevant.isNotEmpty()) {
                    topRelevant.joinToString(separator = "\n\n") {
                        "Reel ID: ${it.id}\nAuteur: ${it.author}\nCaption: ${it.caption}\nSummary: ${it.summary}\nTranscript (excerpt): ${it.transcript.take(300)}"
                    }
                } else {
                    "Aucun Reel sauvegardé en mémoire pour le moment."
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
                
                // 4. Parse JSON
                val adapter = moshi.adapter(GeminiParsedResponse::class.java)
                val parsed = try {
                    adapter.fromJson(responseText)
                } catch(e: Exception) {
                    null
                }

                val answerText = parsed?.answer ?: if (topRelevant.isNotEmpty()) {
                    "J'ai retrouvé ces éléments dans vos Reels sauvegardés !"
                } else {
                    "Je n'ai pas trouvé ce souvenir exact dans votre mémoire RemX. Partagez un nouveau Reel depuis Instagram pour l'analyser !"
                }
                val usedReelIds = parsed?.reelIds ?: topRelevant.map { it.id }
                val usedReels = topRelevant.filter { usedReelIds.contains(it.id) }

                val modelMsg = ChatMessage(
                    text = answerText,
                    role = Role.MODEL,
                    referencedReels = if (usedReels.isNotEmpty()) usedReels else topRelevant.take(2)
                )
                _messages.value = _messages.value + modelMsg

                // Persist model message in Room DB
                chatMessageDao.insertMessage(
                    ChatMessageEntity(
                        id = modelMsg.id,
                        userId = userId,
                        sessionId = activeSessionId,
                        sessionTitle = activeTitle,
                        text = answerText,
                        role = "MODEL",
                        timestamp = modelMsg.timestamp,
                        referencedReelIds = modelMsg.referencedReels.map { it.id }
                    )
                )

            } catch (e: Exception) {
                e.printStackTrace()
                val errorMsg = ChatMessage(text = "Oups, une erreur est survenue lors de la consultation de l'IA Gemini.", role = Role.MODEL)
                _messages.value = _messages.value + errorMsg
                
                chatMessageDao.insertMessage(
                    ChatMessageEntity(
                        id = errorMsg.id,
                        userId = userId,
                        sessionId = activeSessionId,
                        sessionTitle = activeTitle,
                        text = errorMsg.text,
                        role = "MODEL",
                        timestamp = errorMsg.timestamp
                    )
                )
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
