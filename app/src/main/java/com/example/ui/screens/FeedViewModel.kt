package com.example.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Reel
import com.example.data.ReelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed class FeedState {
    object Loading : FeedState()
    data class Success(val reels: List<Reel>) : FeedState()
    data class Error(val message: String) : FeedState()
}

class FeedViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ReelRepository
    
    private val _feedState = MutableStateFlow<FeedState>(FeedState.Loading)
    val feedState: StateFlow<FeedState> = _feedState.asStateFlow()
    
    init {
        val database = AppDatabase.getDatabase(application)
        repository = ReelRepository(database.reelDao())
        fetchReels()
    }

    private fun fetchReels() {
        viewModelScope.launch {
            repository.allReels
                .catch { e -> _feedState.value = FeedState.Error(e.message ?: "Erreur de chargement") }
                .collect { reels ->
                    _feedState.value = FeedState.Success(reels)
                }
        }
    }
    
    fun addReel(url: String) {
        viewModelScope.launch {
            val newReel = Reel(
                userId = "local_user",
                url = url,
                status = "pending"
            )
            repository.insert(newReel)
        }
    }
}

