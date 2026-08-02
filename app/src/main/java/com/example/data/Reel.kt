package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import java.util.UUID

@Entity(tableName = "reels")
@JsonClass(generateAdapter = true)
data class Reel(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val url: String = "",
    val author: String = "",
    val caption: String = "",
    val thumbnailUrl: String = "",
    val transcript: String = "",
    val summary: String = "",
    val themes: List<String> = emptyList(),
    val embedding: List<Double> = emptyList(),
    val status: String = "pending", // "pending" | "done" | "error"
    val createdAt: Long = System.currentTimeMillis()
)
