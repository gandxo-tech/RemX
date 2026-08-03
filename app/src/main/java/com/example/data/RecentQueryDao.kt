package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentQueryDao {
    @Query("SELECT * FROM recent_queries ORDER BY timestamp DESC LIMIT 5")
    fun getRecentQueries(): Flow<List<RecentQuery>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuery(query: RecentQuery)

    @Query("DELETE FROM recent_queries WHERE id NOT IN (SELECT id FROM recent_queries ORDER BY timestamp DESC LIMIT 5)")
    suspend fun deleteOldQueries()
}
