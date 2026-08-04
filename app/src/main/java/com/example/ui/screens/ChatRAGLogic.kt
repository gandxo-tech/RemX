package com.example.ui.screens

import android.app.Application
import android.util.Log
import com.example.api.Content
import com.example.api.EmbedContentRequest
import com.example.api.Part
import com.example.api.RAGPipeline
import com.example.api.RetrofitClient
import com.example.data.AppDatabase
import com.example.data.ReelSegment

suspend fun retrieveContextForQuery(application: Application, query: String, userId: String, apiKey: String): List<ReelSegment> {
    try {
        val db = AppDatabase.getDatabase(application)
        val segments = db.reelSegmentDao().getAllSegmentsForUser(userId)
        
        if (segments.isEmpty()) return emptyList()
        
        // 1. Embed query
        val embedReq = EmbedContentRequest(
            model = "models/text-embedding-004",
            content = Content(parts = listOf(Part(text = query)))
        )
        val embedRes = RetrofitClient.service.embedContent(apiKey, embedReq)
        val queryEmbedding = embedRes.embedding?.values ?: return emptyList()
        
        // 2. Score segments
        val scoredSegments = segments.map { segment ->
            val score = RAGPipeline.cosineSimilarity(queryEmbedding, segment.embedding)
            Pair(segment, score)
        }
        
        // 3. Sort and take top K (e.g., top 5)
        return scoredSegments
            .sortedByDescending { it.second }
            .take(5)
            .map { it.first }
    } catch (e: Exception) {
        Log.e("ChatRAGLogic", "Error retrieving context", e)
        return emptyList()
    }
}
