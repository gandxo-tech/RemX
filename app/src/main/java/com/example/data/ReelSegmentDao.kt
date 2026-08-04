package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReelSegmentDao {
    @Query("SELECT * FROM reel_segments WHERE reelId = :reelId ORDER BY startTime ASC")
    fun getSegmentsForReel(reelId: String): Flow<List<ReelSegment>>

    @Query("SELECT * FROM reel_segments WHERE reelId = :reelId ORDER BY startTime ASC")
    suspend fun getSegmentsForReelSync(reelId: String): List<ReelSegment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSegments(segments: List<ReelSegment>)

    @Update
    suspend fun updateSegment(segment: ReelSegment)

    @Query("DELETE FROM reel_segments WHERE reelId = :reelId")
    suspend fun deleteSegmentsForReel(reelId: String)
    
    @Query("SELECT rs.* FROM reel_segments rs INNER JOIN reels r ON rs.reelId = r.id WHERE r.userId = :userId")
    suspend fun getAllSegmentsForUser(userId: String): List<ReelSegment>
}
