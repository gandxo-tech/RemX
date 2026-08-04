package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import java.util.UUID

@Entity(
    tableName = "reel_metadata",
    foreignKeys = [
        ForeignKey(
            entity = Reel::class,
            parentColumns = ["id"],
            childColumns = ["reelId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("reelId")]
)
@JsonClass(generateAdapter = true)
data class ReelMetadata(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val reelId: String,
    val author: String = "",
    val title: String = "",
    val description: String = "",
    val durationSeconds: Double = 0.0,
    val language: String = "fr",
    val keywords: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
