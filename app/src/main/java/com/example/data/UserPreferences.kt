package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferences private constructor(context: Context) {
    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences("remx_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(getThemeMode())
    val themeMode: StateFlow<Int> = _themeMode.asStateFlow()

    fun getThemeMode(): Int {
        // 0 = System, 1 = Light, 2 = Dark
        return prefs.getInt("theme_mode", 0)
    }

    fun setThemeMode(mode: Int) {
        prefs.edit().putInt("theme_mode", mode).apply()
        _themeMode.value = mode
    }

    companion object {
        @Volatile
        private var INSTANCE: UserPreferences? = null

        fun getInstance(context: Context): UserPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserPreferences(context).also { INSTANCE = it }
            }
        }
    }
}
