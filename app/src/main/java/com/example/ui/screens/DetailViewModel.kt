package com.example.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Reel
import com.example.data.ReelRepository
import com.example.data.ReelSegment
import com.example.data.ReelSegmentDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed class DetailState {
    object Loading : DetailState()
    data class Success(val reel: Reel, val segments: List<ReelSegment> = emptyList()) : DetailState()
    data class Error(val message: String) : DetailState()
}

class DetailViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ReelRepository
    private val segmentDao: ReelSegmentDao
    
    private val _detailState = MutableStateFlow<DetailState>(DetailState.Loading)
    val detailState: StateFlow<DetailState> = _detailState.asStateFlow()
    
    init {
        val database = AppDatabase.getDatabase(application)
        repository = ReelRepository(database.reelDao())
        segmentDao = database.reelSegmentDao()
    }

    fun loadReel(reelId: String) {
        viewModelScope.launch {
            repository.getReelById(reelId)
                .catch { e -> _detailState.value = DetailState.Error(e.message ?: "Erreur") }
                .collect { reel ->
                    if (reel != null) {
                        viewModelScope.launch {
                            segmentDao.getSegmentsForReel(reelId).collect { segments ->
                                _detailState.value = DetailState.Success(reel, segments)
                            }
                        }
                    } else {
                        _detailState.value = DetailState.Error("Reel introuvable")
                    }
                }
        }
    }
    
    fun deleteReel(reelId: String, onDeleted: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteById(reelId)
                onDeleted()
            } catch (e: Exception) {
                _detailState.value = DetailState.Error("Erreur lors de la suppression")
            }
        }
    }

    fun updateReel(updatedReel: Reel) {
        viewModelScope.launch {
            try {
                repository.insert(updatedReel)
            } catch (e: Exception) {
                // handle error
            }
        }
    }
}
