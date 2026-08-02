package com.example.ui.screens

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Reel
import com.example.data.ReelRepository
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ReelRepository
    private val moshi: Moshi
    private val listAdapter: JsonAdapter<List<Reel>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ReelRepository(database.reelDao())
        
        moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val type = Types.newParameterizedType(List::class.java, Reel::class.java)
        listAdapter = moshi.adapter(type)
    }

    fun exportData(uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val reels = repository.allReels.first()
                val json = listAdapter.toJson(reels)
                
                val outputStream: OutputStream? = getApplication<Application>().contentResolver.openOutputStream(uri)
                outputStream?.use { it.write(json.toByteArray()) }
                
                onResult(true)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false)
            }
        }
    }

    fun importData(uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val inputStream: InputStream? = getApplication<Application>().contentResolver.openInputStream(uri)
                val json = inputStream?.bufferedReader().use { it?.readText() }
                
                if (json != null) {
                    val reels = listAdapter.fromJson(json)
                    reels?.forEach { reel ->
                        repository.insert(reel)
                    }
                    onResult(true)
                } else {
                    onResult(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false)
            }
        }
    }
}
