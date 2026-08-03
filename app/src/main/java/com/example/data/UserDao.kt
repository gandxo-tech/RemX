package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = 1 AND isLoggedIn = 1 LIMIT 1")
    fun getUser(): Flow<User?>

    @Query("SELECT * FROM users WHERE id = 1 LIMIT 1")
    suspend fun getUserSync(): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("UPDATE users SET isLoggedIn = 0 WHERE id = 1")
    suspend fun logoutUser()
}

