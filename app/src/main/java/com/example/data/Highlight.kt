package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import java.util.UUID

@Entity(tableName = "highlights")
@JsonClass(generateAdapter = true)
data class Highlight(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
