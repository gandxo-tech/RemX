package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.data.AppDatabase
import com.example.data.Reel
import com.example.data.ReelRepository
import kotlinx.coroutines.launch

class ShareActivity : ComponentActivity() {
    private lateinit var repository: ReelRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = AppDatabase.getDatabase(this)
        repository = ReelRepository(database.reelDao())

        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            handleSendText(intent)
        } else {
            finish()
        }
    }

    private fun handleSendText(intent: Intent) {
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (sharedText != null) {
            val url = extractInstagramUrl(sharedText)
            if (url != null) {
                saveReelToLocal(url)
            } else {
                Toast.makeText(this, "Aucun lien Instagram détecté", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            finish()
        }
    }

    private fun extractInstagramUrl(text: String): String? {
        val regex = Regex("""https?://(www\.)?instagram\.com/(reel|p|reels)/[\w-]+""")
        val match = regex.find(text)
        return match?.value
    }

    private fun saveReelToLocal(url: String) {
        val newReel = Reel(
            userId = "local_user",
            url = url,
            status = "pending"
        )
        
        lifecycleScope.launch {
            try {
                repository.insert(newReel)
                Toast.makeText(this@ShareActivity, "Reel enregistré ✓", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@ShareActivity, "Erreur lors de l'enregistrement", Toast.LENGTH_SHORT).show()
            } finally {
                finish()
            }
        }
    }
}
