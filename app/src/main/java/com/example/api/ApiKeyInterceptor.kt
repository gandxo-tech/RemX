package com.example.api

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class ApiKeyInterceptor : Interceptor {

    companion object {
        private const val TAG = "ApiKeyInterceptor"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url
        val host = originalUrl.host

        val requestBuilder = originalRequest.newBuilder()

        // Handle Google Gemini API
        if (host.contains("generativelanguage.googleapis.com")) {
            val currentKeyParam = originalUrl.queryParameter("key")
            if (currentKeyParam.isNullOrBlank() || currentKeyParam == "MY_GEMINI_API_KEY") {
                val activeGeminiKey = SecureKeyManager.getGeminiKey()
                if (activeGeminiKey.isNotBlank()) {
                    val masked = SecureKeyManager.maskKey(activeGeminiKey)
                    Log.d(TAG, "Injecting obfuscated Gemini key ($masked) into request: ${originalUrl.encodedPath}")

                    val newUrl = originalUrl.newBuilder()
                        .removeAllQueryParameters("key")
                        .addQueryParameter("key", activeGeminiKey)
                        .build()

                    requestBuilder.url(newUrl)
                }
            }
        }

        // Handle OpenRouter API
        if (host.contains("openrouter.ai")) {
            val authHeader = originalRequest.header("Authorization")
            if (authHeader.isNullOrBlank() || authHeader.contains("bearer_free")) {
                val activeOpenRouterKey = SecureKeyManager.getOpenRouterKey()
                if (activeOpenRouterKey.isNotBlank()) {
                    val masked = SecureKeyManager.maskKey(activeOpenRouterKey)
                    Log.d(TAG, "Injecting obfuscated OpenRouter key ($masked) into request header.")
                    requestBuilder.header("Authorization", "Bearer $activeOpenRouterKey")
                }
            }
        }

        // Handle Groq API
        if (host.contains("groq.com")) {
            val authHeader = originalRequest.header("Authorization")
            if (authHeader.isNullOrBlank()) {
                val activeGroqKey = SecureKeyManager.getGroqKey()
                if (activeGroqKey.isNotBlank()) {
                    val masked = SecureKeyManager.maskKey(activeGroqKey)
                    Log.d(TAG, "Injecting obfuscated Groq key ($masked) into request header.")
                    requestBuilder.header("Authorization", "Bearer $activeGroqKey")
                }
            }
        }

        // Handle Together AI API
        if (host.contains("together.xyz") || host.contains("together.ai")) {
            val authHeader = originalRequest.header("Authorization")
            if (authHeader.isNullOrBlank()) {
                val activeTogetherKey = SecureKeyManager.getTogetherKey()
                if (activeTogetherKey.isNotBlank()) {
                    val masked = SecureKeyManager.maskKey(activeTogetherKey)
                    Log.d(TAG, "Injecting obfuscated Together AI key ($masked) into request header.")
                    requestBuilder.header("Authorization", "Bearer $activeTogetherKey")
                }
            }
        }

        return chain.proceed(requestBuilder.build())
    }
}
