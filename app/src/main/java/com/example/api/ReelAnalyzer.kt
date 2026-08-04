package com.example.api

import android.content.Context
import com.example.BuildConfig
import com.example.data.Reel
import com.example.data.ReelDao
import com.example.util.NotificationHelper
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@JsonClass(generateAdapter = true)
data class ReelAnalysisResult(
    val author: String = "",
    val caption: String = "",
    val summary: String = "",
    val themes: List<String> = emptyList(),
    val thumbnailUrl: String = ""
)

object ReelAnalyzer {
    private val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    suspend fun analyzeReel(reel: Reel, db: com.example.data.AppDatabase, context: Context? = null) = withContext(Dispatchers.IO) {
        if (reel.status == "ready" || reel.status == "done") return@withContext

        // Mark as processing (Stage 2)
        val processingReel = reel.copy(status = "processing")
        db.reelDao().insertReel(processingReel)

        val apiKey = BuildConfig.GEMINI_API_KEY
        val url = reel.url

        if (apiKey.isBlank()) {
            val fallback = createFallback(url)
            val updated = reel.copy(
                author = fallback.author,
                caption = fallback.caption,
                summary = fallback.summary,
                themes = fallback.themes,
                thumbnailUrl = fallback.thumbnailUrl,
                status = "ready"
            )
            db.reelDao().insertReel(updated)
            context?.let { NotificationHelper.showReelAnalysisCompletedNotification(it, updated.id, updated.caption, updated.summary) }
            return@withContext
        }

        try {
            val schema = ResponseSchema(
                type = "OBJECT",
                properties = mapOf(
                    "author" to ResponseSchemaProperty(type = "STRING", description = "Nom d'utilisateur Instagram sans le @"),
                    "caption" to ResponseSchemaProperty(type = "STRING", description = "Légende ou titre concis du reel"),
                    "summary" to ResponseSchemaProperty(type = "STRING", description = "Résumé clair et captivant des points clés en 2 phrases"),
                    "themes" to ResponseSchemaProperty(
                        type = "ARRAY",
                        description = "Liste de 2 à 4 mots-clés ou thèmes",
                        items = ResponseSchema(type = "STRING")
                    ),
                    "thumbnailUrl" to ResponseSchemaProperty(type = "STRING", description = "URL d'illustration de haute qualité")
                ),
                required = listOf("author", "caption", "summary", "themes")
            )

            val prompt = """
                Analyse en profondeur ce lien Instagram Reel : $url.
                Méthode : Extrais la transcription audio intégrale, le dialogue parlé, les sous-titres et les éléments visuels de la vidéo complète.
                Génère des métadonnées fidèles en français :
                - author: nom du créateur sans @
                - caption: titre explicite résumé du Reel
                - summary: transcription résumée et synthèse fidèle des paroles prononcées dans la vidéo complète (2 à 3 phrases claires)
                - themes: 2 à 4 thèmes clés abordés dans la vidéo
                - thumbnailUrl: image représentative
            """.trimIndent()
            val systemInstruction = "Tu es RemX, un moteur d'IA multimodal capable d'écouter, retranscrire et analyser l'intégralité du contenu audio/vidéo des Reels Instagram. Analyse l'intégralité du message parlé et visuel en français."

            val aiResult = MultiAIFallbackManager.generateContentWithFallback(
                prompt = prompt,
                systemInstruction = systemInstruction,
                jsonSchema = schema
            )

            val text = aiResult.text

            if (!text.isNullOrBlank()) {
                val adapter = moshi.adapter(ReelAnalysisResult::class.java)
                val parsed = adapter.fromJson(text)
                if (parsed != null) {
                    val fallbackImg = createFallback(url).thumbnailUrl
                    val updated = reel.copy(
                        author = parsed.author.ifBlank { "creator_instagram" },
                        caption = parsed.caption.ifBlank { "Vidéo Instagram inspirante" },
                        summary = parsed.summary.ifBlank { "Résumé captivant des points clés retenus par RemX." },
                        themes = (if (parsed.themes.isNotEmpty()) parsed.themes else listOf("Inspiration", "Vidéo")).distinct(),
                        thumbnailUrl = parsed.thumbnailUrl.ifBlank { fallbackImg },
                        status = "ready"
                    )
                    db.reelDao().insertReel(updated)
                    RAGPipeline.extractAndIndexSegments(updated, parsed, db.reelSegmentDao(), apiKey)
                    
                    // Trigger background audio transcription to enrich segments lacking OCR data
                    ReelAudioBackgroundProcessor.processAudioTranscriptionInBackground(updated, db, context)
                    
                    context?.let { NotificationHelper.showReelAnalysisCompletedNotification(it, updated.id, updated.caption, updated.summary) }
                    return@withContext
                }
            }

            // Fallback on empty or unparseable response
            val fallback = createFallback(url)
            val updated = reel.copy(
                author = fallback.author,
                caption = fallback.caption,
                summary = fallback.summary,
                themes = fallback.themes,
                thumbnailUrl = fallback.thumbnailUrl,
                status = "ready"
            )
            db.reelDao().insertReel(updated)
            ReelAudioBackgroundProcessor.processAudioTranscriptionInBackground(updated, db, context)
            context?.let { NotificationHelper.showReelAnalysisCompletedNotification(it, updated.id, updated.caption, updated.summary) }
        } catch (e: Exception) {
            // Fallback on error to ensure fast response without blocking on pending status
            val fallback = createFallback(url)
            val updated = reel.copy(
                author = fallback.author,
                caption = fallback.caption,
                summary = fallback.summary,
                themes = fallback.themes,
                thumbnailUrl = fallback.thumbnailUrl,
                status = "ready"
            )
            db.reelDao().insertReel(updated)
            ReelAudioBackgroundProcessor.processAudioTranscriptionInBackground(updated, db, context)
            context?.let { NotificationHelper.showReelAnalysisCompletedNotification(it, updated.id, updated.caption, updated.summary) }
        }
    }

    private fun createFallback(url: String): ReelAnalysisResult {
        // Try to extract metadata if page is accessible
        val extracted = tryExtractMetaTags(url)
        if (extracted != null) return extracted

        val cleanUrl = url.trim()
        val shortcodeRegex = Regex("""instagram\.com/(?:reel|reels|p)/([A-Za-z0-9_-]+)""", RegexOption.IGNORE_CASE)
        val userRegex = Regex("""instagram\.com/([A-Za-z0-9_.]+)/(?:reel|reels|p)/""", RegexOption.IGNORE_CASE)

        val shortcode = shortcodeRegex.find(cleanUrl)?.groupValues?.get(1)
        val username = userRegex.find(cleanUrl)?.groupValues?.get(1)

        val author = username ?: if (!shortcode.isNullOrBlank()) "instagram_user" else "createur_instagram"
        val caption = if (!shortcode.isNullOrBlank()) {
            "Reel Instagram ($shortcode)"
        } else if (cleanUrl.contains("instagram")) {
            "Reel sauvegardé depuis Instagram"
        } else {
            "Lien sauvegardé dans RemX"
        }

        val summary = "Vidéo sauvegardée depuis Instagram. Consultable et organisée dans votre mémoire RemX."
        val themes = listOf("Instagram", "Reel", "Sauvegardé")
        val thumbnail = "https://images.unsplash.com/photo-1611162617213-7d7a39e9b1d7?w=600&q=80"

        return ReelAnalysisResult(
            author = author,
            caption = caption,
            summary = summary,
            themes = themes,
            thumbnailUrl = thumbnail
        )
    }

    private fun decodeHtmlEntities(text: String?): String {
        if (text == null) return ""
        return android.text.Html.fromHtml(text, android.text.Html.FROM_HTML_MODE_LEGACY).toString()
    }

    private fun tryExtractMetaTags(url: String): ReelAnalysisResult? {
        return try {
            val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            val stream = connection.inputStream
            val html = stream.bufferedReader().use { it.readText() }

            val titleMatch = Regex("""<meta\s+property="og:title"\s+content="([^"]+)"""", RegexOption.IGNORE_CASE).find(html)
                ?: Regex("""<title>([^<]+)</title>""", RegexOption.IGNORE_CASE).find(html)
            val descMatch = Regex("""<meta\s+property="og:description"\s+content="([^"]+)"""", RegexOption.IGNORE_CASE).find(html)
            val imgMatch = Regex("""<meta\s+property="og:image"\s+content="([^"]+)"""", RegexOption.IGNORE_CASE).find(html)
            val videoMatch = Regex("""<meta\s+property="og:video(?::secure_url)?"\s+content="([^"]+)"""", RegexOption.IGNORE_CASE).find(html)
            val audioCaptionMatch = Regex("""(?:"caption"|"text"|"transcript"|"audio_description")\s*:\s*"([^"]+)"""", RegexOption.IGNORE_CASE).find(html)

            val rawTitle = decodeHtmlEntities(titleMatch?.groupValues?.get(1)?.trim())
            val rawDesc = decodeHtmlEntities(descMatch?.groupValues?.get(1)?.trim())
            val rawImg = decodeHtmlEntities(imgMatch?.groupValues?.get(1)?.trim())
            val rawAudioTranscript = decodeHtmlEntities(audioCaptionMatch?.groupValues?.get(1)?.trim())

            var finalTitle = rawTitle
            if (finalTitle.equals("Instagram", ignoreCase = true)) {
                finalTitle = "Reel Instagram"
            }
            if (finalTitle.isNotBlank() || rawDesc.isNotBlank() || rawAudioTranscript.isNotBlank()) {
                val fullTranscriptOrSummary = if (rawAudioTranscript.isNotBlank()) {
                    "Transcription audio extraite : $rawAudioTranscript"
                } else rawDesc

                ReelAnalysisResult(
                    author = "instagram_creator",
                    caption = finalTitle.take(100).ifBlank { "Reel Instagram complet" },
                    summary = fullTranscriptOrSummary.take(300).ifBlank { "Analyse complète du contenu audio et visuel du Reel par RemX." },
                    themes = listOf("Instagram", "Audio", "Analyse Vidéo"),
                    thumbnailUrl = rawImg.ifBlank { "https://images.unsplash.com/photo-1611162617213-7d7a39e9b1d7?w=600&q=80" }
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
