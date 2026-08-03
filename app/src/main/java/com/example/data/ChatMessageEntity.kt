package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val sessionId: String = "default_session",
    val sessionTitle: String = "Nouvelle discussion",
    val text: String,
    val role: String,
    val timestamp: Long = System.currentTimeMillis(),
    val referencedReelIds: List<String> = emptyList()
)

data class ChatSessionSummary(
    val sessionId: String,
    val sessionTitle: String,
    val lastTimestamp: Long,
    val messageCount: Int
)
