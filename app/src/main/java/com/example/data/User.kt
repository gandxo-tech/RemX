package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "users")
@JsonClass(generateAdapter = true)
data class User(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val phoneNumber: String = "",
    val passwordHash: String = "",
    val avatarUrl: String = "",
    val isLoggedIn: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

