package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE userId = :userId AND sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(userId: String, sessionId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE userId = :userId ORDER BY timestamp ASC")
    fun getAllMessagesForUser(userId: String): Flow<List<ChatMessageEntity>>

    @Query("""
        SELECT 
            sessionId,
            MAX(sessionTitle) as sessionTitle,
            MAX(timestamp) as lastTimestamp,
            COUNT(*) as messageCount
        FROM chat_messages 
        WHERE userId = :userId 
        GROUP BY sessionId 
        ORDER BY lastTimestamp DESC
    """)
    fun getSessionsForUser(userId: String): Flow<List<ChatSessionSummary>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE userId = :userId AND sessionId = :sessionId")
    suspend fun deleteSession(userId: String, sessionId: String)

    @Query("DELETE FROM chat_messages WHERE userId = :userId")
    suspend fun clearHistoryForUser(userId: String)

    @Query("DELETE FROM chat_messages WHERE id = :id")
    suspend fun deleteMessageById(id: String)
}
