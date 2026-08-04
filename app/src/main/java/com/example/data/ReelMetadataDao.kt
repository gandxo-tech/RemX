package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReelMetadataDao {
    @Query("SELECT * FROM reel_metadata WHERE reelId = :reelId LIMIT 1")
    fun getMetadataForReel(reelId: String): Flow<ReelMetadata?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetadata(metadata: ReelMetadata)

    @Query("DELETE FROM reel_metadata WHERE reelId = :reelId")
    suspend fun deleteMetadataForReel(reelId: String)
}
