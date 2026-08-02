package com.example.data

import kotlinx.coroutines.flow.Flow

class ReelRepository(private val reelDao: ReelDao) {
    val allReels: Flow<List<Reel>> = reelDao.getAllReels()

    fun getReelById(id: String): Flow<Reel?> = reelDao.getReelById(id)

    suspend fun insert(reel: Reel) = reelDao.insertReel(reel)

    suspend fun deleteById(id: String) = reelDao.deleteReelById(id)
}
