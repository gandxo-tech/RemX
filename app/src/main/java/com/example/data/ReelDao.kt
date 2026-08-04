package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReelDao {
    @Query("SELECT * FROM reels WHERE userId = :userId ORDER BY createdAt DESC")
    fun getReelsForUser(userId: String): Flow<List<Reel>>
    
    @Query("SELECT * FROM reels ORDER BY createdAt DESC")
    fun getAllReels(): Flow<List<Reel>>

    @Query("SELECT * FROM reels WHERE id = :id")
    fun getReelById(id: String): Flow<Reel?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReel(reel: Reel)

    @Query("DELETE FROM reels WHERE id = :id")
    suspend fun deleteReelById(id: String)
}
