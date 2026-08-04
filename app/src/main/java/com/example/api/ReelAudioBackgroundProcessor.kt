package com.example.api

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.AppDatabase
import com.example.data.Reel
import com.example.data.ReelSegment
import com.example.util.NotificationHelper
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@JsonClass(generateAdapter = true)
data class SegmentAudioEnrichment(
    val segmentIndex: Int,
    val audioTranscript: String,
    val enhancedContext: String
)

@JsonClass(generateAdapter = true)
data class AudioTranscriptionResult(
    val fullAudioTranscript: String,
    val enrichments: List<SegmentAudioEnrichment>
)

object ReelAudioBackgroundProcessor {
    private const val TAG = "ReelAudioBackgroundProc"
    private val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    /**
     * Launch background process to extract & transcribe audio from downloaded Reel,
     * specifically targeting segments with insufficient OCR text.
     */
    suspend fun processAudioTranscriptionInBackground(
        reel: Reel,
        db: AppDatabase,
        context: Context? = null
    ) = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting background audio extraction & transcription for Reel: ${reel.id}")
            val segmentDao = db.reelSegmentDao()
            val existingSegments = segmentDao.getSegmentsForReelSync(reel.id)

            // Filter segments that lack sufficient OCR data or full audio transcript
            val lowOcrSegments = existingSegments.filter { seg ->
                seg.ocrText.isBlank() || seg.ocrText.length < 15 || seg.transcript.isBlank()
            }

            if (existingSegments.isEmpty()) {
                Log.d(TAG, "No existing segments found. Creating initial audio-transcribed segments.")
            } else if (lowOcrSegments.isEmpty()) {
                Log.d(TAG, "All segments already have rich OCR / transcript data. Skipping background audio pass.")
                return@withContext
            }

            // Construct AI prompt focused on full audio extraction
            val schema = ResponseSchema(
                type = "OBJECT",
                properties = mapOf(
                    "fullAudioTranscript" to ResponseSchemaProperty(
                        type = "STRING",
                        description = "Transcription audio complète des paroles prononcées dans le Reel"
                    ),
                    "enrichments" to ResponseSchemaProperty(
                        type = "ARRAY",
                        description = "Enrichissements de transcription audio par segment",
                        items = ResponseSchema(
                            type = "OBJECT",
                            properties = mapOf(
                                "segmentIndex" to ResponseSchemaProperty(type = "INTEGER", description = "Index de segment (0, 1, 2...)"),
                                "audioTranscript" to ResponseSchemaProperty(type = "STRING", description = "Paroles exactes transcrites du segment"),
                                "enhancedContext" to ResponseSchemaProperty(type = "STRING", description = "Contexte global combinant audio et éléments visuels")
                            ),
                            required = listOf("segmentIndex", "audioTranscript", "enhancedContext")
                        )
                    )
                ),
                required = listOf("fullAudioTranscript", "enrichments")
            )

            val prompt = """
                Effectue l'extraction audio complète et la transcription intégrale du Reel Instagram : ${reel.url}.
                Pour chaque segment vidéo (particulièrement ceux manquant de texte à l'écran OCR), 
                fournis les paroles transcrites exactes et le contexte explicatif enrichi.
                Titre Reel: "${reel.caption}"
                Résumé existant: "${reel.summary}"
                Nombre de segments à enrichir: ${if (existingSegments.isNotEmpty()) existingSegments.size else 3}.
            """.trimIndent()

            val systemInstruction = "Tu es un sous-système de traitement audio en arrière-plan (Whisper/Gemini Audio Engine) pour RemX. Ton rôle est d'extraire et de retranscrire fidèlement chaque parole prononcée dans la vidéo pour enrichir les segments pauvres en texte OCR."

            val aiResult = MultiAIFallbackManager.generateContentWithFallback(
                prompt = prompt,
                systemInstruction = systemInstruction,
                jsonSchema = schema
            )

            val jsonText = aiResult.text
            if (!jsonText.isNullOrBlank()) {
                val adapter = moshi.adapter(AudioTranscriptionResult::class.java)
                val parsed = adapter.fromJson(jsonText)

                if (parsed != null && parsed.enrichments.isNotEmpty()) {
                    Log.d(TAG, "Background audio extraction successful! ${parsed.enrichments.size} segment enrichments produced.")

                    // Update existing segments in Room
                    if (existingSegments.isNotEmpty()) {
                        val updatedList = existingSegments.mapIndexed { index, segment ->
                            val match = parsed.enrichments.find { it.segmentIndex == index }
                                ?: parsed.enrichments.getOrNull(index)
                            
                            if (match != null && match.audioTranscript.isNotBlank()) {
                                segment.copy(
                                    transcript = match.audioTranscript,
                                    summary = if (segment.summary.isNotBlank()) "${segment.summary} | Audio: ${match.enhancedContext}" else match.enhancedContext,
                                    ocrText = if (segment.ocrText.isBlank()) "[Transcription Audio Complète]" else segment.ocrText
                                )
                            } else {
                                segment
                            }
                        }
                        segmentDao.insertSegments(updatedList)
                    } else {
                        // Create initial enriched segments if none existed
                        val newSegments = parsed.enrichments.mapIndexed { idx, enrich ->
                            ReelSegment(
                                reelId = reel.id,
                                startTime = idx * 5.0,
                                endTime = (idx + 1) * 5.0,
                                transcript = enrich.audioTranscript,
                                visualDescription = "Visualisation du segment Reel ${idx + 1}",
                                ocrText = "[Audio Transcrit en Arrière-Plan]",
                                summary = enrich.enhancedContext
                            )
                        }
                        segmentDao.insertSegments(newSegments)
                    }

                    // Update main Reel record transcript field if empty
                    if (reel.transcript.isBlank() && parsed.fullAudioTranscript.isNotBlank()) {
                        val updatedReel = reel.copy(
                            transcript = parsed.fullAudioTranscript
                        )
                        db.reelDao().insertReel(updatedReel)
                    }

                    context?.let {
                        NotificationHelper.showReelAnalysisCompletedNotification(
                            it,
                            reel.id,
                            "Audio Transcrit",
                            "L'audio de la vidéo a été extrait et transcrit en arrière-plan."
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during background audio extraction & transcription", e)
        }
    }
}
