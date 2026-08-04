package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import java.util.UUID

@Entity(
    tableName = "reel_segments",
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
data class ReelSegment(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val reelId: String,
    val startTime: Double = 0.0,
    val endTime: Double = 0.0,
    val transcript: String = "",
    val visualDescription: String = "",
    val ocrText: String = "",
    val summary: String = "",
    val keywords: List<String> = emptyList(),
    val embedding: List<Double> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

