package com.example.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.Reel
import com.example.data.ReelSegment
import com.example.data.ReelSegmentDao
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlin.math.sqrt

@JsonClass(generateAdapter = true)
data class MultimodalExtractionResult(
    val segments: List<ExtractedSegment>
)

@JsonClass(generateAdapter = true)
data class ExtractedSegment(
    val startTime: Double,
    val endTime: Double,
    val transcript: String,
    val visualDescription: String,
    val ocrText: String,
    val summary: String
)

object RAGPipeline {
    private val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    
    suspend fun extractAndIndexSegments(
        reel: Reel,
        metadata: ReelAnalysisResult,
        segmentDao: ReelSegmentDao,
        apiKey: String
    ) {
        try {
            // 1. Simulate the extraction of multimodal segments using Gemini
            val schema = ResponseSchema(
                type = "OBJECT",
                properties = mapOf(
                    "segments" to ResponseSchemaProperty(
                        type = "ARRAY",
                        description = "Liste des segments de la vidéo",
                        items = ResponseSchema(
                            type = "OBJECT",
                            properties = mapOf(
                                "startTime" to ResponseSchemaProperty(type = "NUMBER", description = "Temps de début en secondes"),
                                "endTime" to ResponseSchemaProperty(type = "NUMBER", description = "Temps de fin en secondes"),
                                "transcript" to ResponseSchemaProperty(type = "STRING", description = "Transcription de l'audio"),
                                "visualDescription" to ResponseSchemaProperty(type = "STRING", description = "Description de l'image/action"),
                                "ocrText" to ResponseSchemaProperty(type = "STRING", description = "Texte à l'écran"),
                                "summary" to ResponseSchemaProperty(type = "STRING", description = "Résumé du segment")
                            ),
                            required = listOf("startTime", "endTime", "transcript", "visualDescription", "ocrText", "summary")
                        )
                    )
                ),
                required = listOf("segments")
            )
            
            val prompt = """
                Tu es un extracteur de segments vidéo. Génère une liste de 3 à 5 segments fictifs mais hautement réalistes 
                pour une vidéo dont le titre est "${metadata.caption}" et le résumé est "${metadata.summary}".
                Les segments doivent s'enchaîner chronologiquement.
            """.trimIndent()
            
            val req = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                generationConfig = GenerationConfig(
                    responseMimeType = "application/json",
                    responseSchema = schema
                )
            )
            
            val response = RetrofitClient.service.generateContent(apiKey, req)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            
            if (!text.isNullOrBlank()) {
                val adapter = moshi.adapter(MultimodalExtractionResult::class.java)
                val extractionResult = adapter.fromJson(text)
                
                if (extractionResult != null && extractionResult.segments.isNotEmpty()) {
                    // 2. Generate embeddings for each segment
                    val reelSegments = mutableListOf<ReelSegment>()
                    
                    for (seg in extractionResult.segments) {
                        val textToEmbed = "Audio: ${seg.transcript} | Visuel: ${seg.visualDescription} | Texte écran: ${seg.ocrText} | Résumé: ${seg.summary}"
                        val embedReq = EmbedContentRequest(
                            model = "models/text-embedding-004",
                            content = Content(parts = listOf(Part(text = textToEmbed)))
                        )
                        
                        try {
                            val embedRes = RetrofitClient.service.embedContent(apiKey, embedReq)
                            val embeddingValues = embedRes.embedding?.values ?: emptyList()
                            
                            reelSegments.add(
                                ReelSegment(
                                    reelId = reel.id,
                                    startTime = seg.startTime,
                                    endTime = seg.endTime,
                                    transcript = seg.transcript,
                                    visualDescription = seg.visualDescription,
                                    ocrText = seg.ocrText,
                                    summary = seg.summary,
                                    embedding = embeddingValues
                                )
                            )
                        } catch (e: Exception) {
                            Log.e("RAG", "Error embedding segment", e)
                        }
                    }
                    
                    // 3. Index segments in Room
                    if (reelSegments.isNotEmpty()) {
                        segmentDao.insertSegments(reelSegments)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("RAG", "Error during segment extraction and indexing", e)
        }
    }
    
    // Cosine similarity
    fun cosineSimilarity(v1: List<Double>, v2: List<Double>): Double {
        if (v1.isEmpty() || v2.isEmpty() || v1.size != v2.size) return 0.0
        var dot = 0.0
        var norm1 = 0.0
        var norm2 = 0.0
        for (i in v1.indices) {
            dot += v1[i] * v2[i]
            norm1 += v1[i] * v1[i]
            norm2 += v2[i] * v2[i]
        }
        return dot / (sqrt(norm1) * sqrt(norm2))
    }
}
