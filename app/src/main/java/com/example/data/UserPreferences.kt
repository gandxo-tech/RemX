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

    private val _notificationsEnabled = MutableStateFlow(areNotificationsEnabled())
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _notifReels = MutableStateFlow(isNotifReelsEnabled())
    val notifReels: StateFlow<Boolean> = _notifReels.asStateFlow()

    private val _notifReminders = MutableStateFlow(isNotifRemindersEnabled())
    val notifReminders: StateFlow<Boolean> = _notifReminders.asStateFlow()

    private val _notifSuggestions = MutableStateFlow(isNotifSuggestionsEnabled())
    val notifSuggestions: StateFlow<Boolean> = _notifSuggestions.asStateFlow()

    fun getThemeMode(): Int {
        // 0 = System, 1 = Light, 2 = Dark
        return prefs.getInt("theme_mode", 0)
    }

    fun setThemeMode(mode: Int) {
        prefs.edit().putInt("theme_mode", mode).apply()
        _themeMode.value = mode
    }

    fun areNotificationsEnabled(): Boolean {
        return prefs.getBoolean("notifications_enabled", true)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
        _notificationsEnabled.value = enabled
    }

    fun isNotifReelsEnabled(): Boolean = prefs.getBoolean("notif_reels", true)
    fun setNotifReelsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("notif_reels", enabled).apply()
        _notifReels.value = enabled
    }

    fun isNotifRemindersEnabled(): Boolean = prefs.getBoolean("notif_reminders", true)
    fun setNotifRemindersEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("notif_reminders", enabled).apply()
        _notifReminders.value = enabled
    }

    fun isNotifSuggestionsEnabled(): Boolean = prefs.getBoolean("notif_suggestions", true)
    fun setNotifSuggestionsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("notif_suggestions", enabled).apply()
        _notifSuggestions.value = enabled
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
