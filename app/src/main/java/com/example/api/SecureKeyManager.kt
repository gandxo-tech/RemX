package com.example.api

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.example.BuildConfig
import java.nio.charset.StandardCharsets

object SecureKeyManager {
    private const val PREFS_NAME = "remx_secure_keys"
    private const val KEY_GEMINI = "key_gemini_enc"
    private const val KEY_GROQ = "key_groq_enc"
    private const val KEY_OPENROUTER = "key_openrouter_enc"
    private const val KEY_TOGETHER = "key_together_enc"

    @Volatile
    private var cachedGeminiKey: String? = null
    @Volatile
    private var cachedGroqKey: String? = null
    @Volatile
    private var cachedOpenRouterKey: String? = null
    @Volatile
    private var cachedTogetherKey: String? = null

    private const val DEFAULT_GEMINI_KEY = ""

    fun getGeminiKey(context: Context? = null): String {
        cachedGeminiKey?.let { if (it.isNotBlank()) return it }

        context?.let { ctx ->
            val stored = getStoredKey(ctx, KEY_GEMINI)
            if (!stored.isNullOrBlank()) {
                cachedGeminiKey = stored
                return stored
            }
        }

        val buildConfigKey = BuildConfig.GEMINI_API_KEY
        if (buildConfigKey.isNotBlank() && buildConfigKey != "MY_GEMINI_API_KEY") {
            cachedGeminiKey = buildConfigKey
            return buildConfigKey
        }

        cachedGeminiKey = DEFAULT_GEMINI_KEY
        return DEFAULT_GEMINI_KEY
    }

    private const val DEFAULT_GROQ_KEY = ""

    fun getGroqKey(context: Context? = null): String {
        cachedGroqKey?.let { if (it.isNotBlank()) return it }
        context?.let { ctx ->
            val stored = getStoredKey(ctx, KEY_GROQ)
            if (!stored.isNullOrBlank()) {
                cachedGroqKey = stored
                return stored
            }
        }
        cachedGroqKey = DEFAULT_GROQ_KEY
        return DEFAULT_GROQ_KEY
    }

    private const val DEFAULT_OPENROUTER_KEY = ""

    fun getOpenRouterKey(context: Context? = null): String {
        cachedOpenRouterKey?.let { if (it.isNotBlank()) return it }
        context?.let { ctx ->
            val stored = getStoredKey(ctx, KEY_OPENROUTER)
            if (!stored.isNullOrBlank()) {
                cachedOpenRouterKey = stored
                return stored
            }
        }
        cachedOpenRouterKey = DEFAULT_OPENROUTER_KEY
        return DEFAULT_OPENROUTER_KEY
    }

    private const val DEFAULT_TOGETHER_KEY = ""

    fun getTogetherKey(context: Context? = null): String {
        cachedTogetherKey?.let { if (it.isNotBlank()) return it }
        context?.let { ctx ->
            val stored = getStoredKey(ctx, KEY_TOGETHER)
            if (!stored.isNullOrBlank()) {
                cachedTogetherKey = stored
                return stored
            }
        }
        cachedTogetherKey = DEFAULT_TOGETHER_KEY
        return DEFAULT_TOGETHER_KEY
    }

    fun setGeminiKey(context: Context, key: String) {
        saveKey(context, KEY_GEMINI, key)
        cachedGeminiKey = key
    }

    fun setGroqKey(context: Context, key: String) {
        saveKey(context, KEY_GROQ, key)
        cachedGroqKey = key
    }

    fun setOpenRouterKey(context: Context, key: String) {
        saveKey(context, KEY_OPENROUTER, key)
        cachedOpenRouterKey = key
    }

    fun setTogetherKey(context: Context, key: String) {
        saveKey(context, KEY_TOGETHER, key)
        cachedTogetherKey = key
    }

    fun maskKey(key: String): String {
        if (key.isBlank()) return "[NONE]"
        if (key.length <= 8) return "****"
        return key.take(4) + "..." + key.takeLast(4)
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun saveKey(context: Context, prefKey: String, value: String) {
        val obfuscated = Base64.encodeToString(value.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
        getPrefs(context).edit().putString(prefKey, obfuscated).apply()
    }

    private fun getStoredKey(context: Context, prefKey: String): String? {
        val obfuscated = getPrefs(context).getString(prefKey, null) ?: return null
        return try {
            String(Base64.decode(obfuscated, Base64.NO_WRAP), StandardCharsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }
}
