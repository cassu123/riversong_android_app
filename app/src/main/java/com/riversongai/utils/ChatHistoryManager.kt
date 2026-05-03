package com.riversongai.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.riversongai.data.model.ChatMessage

object ChatHistoryManager {
    private const val PREFS_NAME = "river_song_chat_prefs"
    private const val KEY_HISTORY = "chat_history"
    private val gson = Gson()

    fun save(context: Context, messages: List<ChatMessage>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = gson.toJson(messages.takeLast(50))
        prefs.edit().putString(KEY_HISTORY, json).apply()
    }

    fun load(context: Context): List<ChatMessage> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<ChatMessage>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
