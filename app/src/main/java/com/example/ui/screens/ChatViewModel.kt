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
    private val _allReels = MutableStateFlow<List<Reel>>(emptyList())
    val allReels: StateFlow<List<Reel>> = _allReels.asStateFlow()

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

    private var reelsObserveJob: Job? = null
    private val apiKey = BuildConfig.GEMINI_API_KEY
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    init {
        // Reels will be loaded in loadHistoryForUser
    }

    fun loadHistoryForUser(userId: String) {
        if (currentUserId == userId) return
        currentUserId = userId
        
        reelsObserveJob?.cancel()
        reelsObserveJob = viewModelScope.launch {
            repository.getReelsForUser(userId).collect {
                _allReels.value = it
            }
        }

        // Observe all sessions for this user
        sessionObserveJob?.cancel()
        sessionObserveJob = viewModelScope.launch {
            chatMessageDao.getSessionsForUser(userId).collect { summaries ->
                _sessions.value = summaries
                val active = summaries.find { it.sessionId == _currentSessionId.value }
                if (active != null) {
                    _currentSessionTitle.value = active.sessionTitle
                } else if (summaries.isNotEmpty() && _messages.value.isEmpty()) {
                    // Auto-select the most recent session if we are currently on an empty new session
                    val recent = summaries.first()
                    _currentSessionId.value = recent.sessionId
                    _currentSessionTitle.value = recent.sessionTitle
                    observeSessionMessages(userId, recent.sessionId)
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
                val reels = _allReels.value
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

                val allUserReels = _allReels.value

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


                // Real RAG Implementation
                val application = getApplication<Application>()
                val relevantSegments = retrieveContextForQuery(application, text, userId, apiKey)
                
                // Collect relevant Reels based on segments
                val relevantReelIds = relevantSegments.map { it.reelId }.distinct()
                val finalReferencedReels = allUserReels.filter { relevantReelIds.contains(it.id) }
                
                val contextString = if (relevantSegments.isEmpty()) {
                    "Aucun segment vidéo pertinent trouvé pour cette requête."
                } else {
                    relevantSegments.joinToString("\n\n") { seg ->
                        val parentReel = finalReferencedReels.find { it.id == seg.reelId }
                        val title = parentReel?.caption ?: "Vidéo inconnue"
                        "--- Source: $title (${seg.startTime}s - ${seg.endTime}s) ---\n" +
                        "Transcript: ${seg.transcript}\n" +
                        "Visuel: ${seg.visualDescription}\n" +
                        "Texte OCR: ${seg.ocrText}"
                    }
                }

                // 3. System Prompt for Gemini
                val systemPrompt = """
                    Tu es RemX, un assistant IA agissant comme un Second Cerveau vidéo pour l'utilisateur.
                    Tu disposes d'extraits d'Instagram Reels sauvegardés par l'utilisateur (segments temporels, transcriptions et descriptions visuelles).
                    
                    RÈGLES IMPORTANTES :
                    1. Réponds EXCLUSIVEMENT en te basant sur le contexte fourni. 
                    2. Si la réponse n'est pas dans le contexte, dis clairement : "Je n'ai pas trouvé d'information à ce sujet dans tes Reels sauvegardés." N'invente jamais d'informations.
                    3. Cite tes sources en utilisant le titre de la vidéo et les timestamps (ex: "Dans le Reel X (12s - 25s), on voit que...").
                    4. Analyse les liens entre les vidéos si la question le demande.
                    5. Sois concis, clair, et utilise un ton amical.
                    6. Rédiges TOUJOURS en français.
                    
                    Voici les données disponibles (contexte) extraites des Reels de l'utilisateur :
                    $contextString
                """.trimIndent()

                val chatContents = _messages.value.dropLast(1).map {
                    Content(
                        role = if (it.role == Role.USER) "user" else "model",
                        parts = listOf(Part(text = it.text))
                    )
                }

                val aiResult = MultiAIFallbackManager.generateContentWithFallback(
                    prompt = text,
                    systemInstruction = systemPrompt,
                    history = chatContents
                )

                val modelResponseText = aiResult.text.ifBlank { "Je n'ai pas pu générer de réponse." }

                val modelMsg = ChatMessage(
                    text = modelResponseText,
                    role = Role.MODEL,
                    referencedReels = finalReferencedReels
                )
                
                _messages.value = _messages.value + modelMsg
                
                chatMessageDao.insertMessage(
                    ChatMessageEntity(
                        id = modelMsg.id,
                        userId = userId,
                        sessionId = activeSessionId,
                        sessionTitle = activeTitle,
                        text = modelMsg.text,
                        role = "MODEL",
                        timestamp = modelMsg.timestamp,
                        referencedReelIds = modelMsg.referencedReels.map { it.id }
                    )
                )

            } catch (e: Exception) {
                e.printStackTrace()
                _messages.value = _messages.value + ChatMessage(text = "Erreur de connexion. Vérifiez votre réseau.", role = Role.MODEL)
            } finally {
                _isTyping.value = false
            }
        }
    }
}
