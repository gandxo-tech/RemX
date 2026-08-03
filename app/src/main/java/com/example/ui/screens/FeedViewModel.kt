package com.example.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Reel
import com.example.data.ReelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.example.api.ReelAnalyzer

sealed class FeedState {
    object Loading : FeedState()
    data class Success(val reels: List<Reel>) : FeedState()
    data class Error(val message: String) : FeedState()
}

class FeedViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ReelRepository
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _rawReels = MutableStateFlow<List<Reel>>(emptyList())
    private val _isLoading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)

    val feedState: StateFlow<FeedState> = combine(_rawReels, _searchQuery, _isLoading, _error) { reels, query, isLoading, error ->
        if (isLoading && reels.isEmpty()) {
            FeedState.Loading
        } else if (error != null && reels.isEmpty()) {
            FeedState.Error(error)
        } else {
            val filtered = if (query.isBlank()) {
                reels
            } else {
                val cleanQuery = query.trim()
                reels.filter { reel ->
                    reel.caption.contains(cleanQuery, ignoreCase = true) ||
                    reel.themes.any { theme -> theme.contains(cleanQuery, ignoreCase = true) }
                }
            }
            FeedState.Success(filtered)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FeedState.Loading)
    
    init {
        val database = AppDatabase.getDatabase(application)
        repository = ReelRepository(database.reelDao())
        fetchReels()
    }

    private fun fetchReels() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.allReels
                .catch { e ->
                    _error.value = e.message ?: "Erreur de chargement"
                    _isLoading.value = false
                }
                .collect { reels ->
                    _rawReels.value = reels
                    _isLoading.value = false

                    // Auto-analyze any pending reels
                    reels.filter { it.status == "pending" }.forEach { pendingReel ->
                        viewModelScope.launch {
                            val context = getApplication<Application>().applicationContext
                            val dao = AppDatabase.getDatabase(getApplication()).reelDao()
                            ReelAnalyzer.analyzeReel(pendingReel, dao, context)
                        }
                    }
                }
        }
    }
    
    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }
    
    fun addReel(url: String) {
        viewModelScope.launch {
            val newReel = Reel(
                userId = "local_user",
                url = url,
                status = "pending"
            )
            val context = getApplication<Application>().applicationContext
            val dao = AppDatabase.getDatabase(getApplication()).reelDao()
            repository.insert(newReel)
            ReelAnalyzer.analyzeReel(newReel, dao, context)
        }
    }

    fun deleteReel(reel: Reel) {
        viewModelScope.launch {
            repository.deleteById(reel.id)
        }
    }
}

