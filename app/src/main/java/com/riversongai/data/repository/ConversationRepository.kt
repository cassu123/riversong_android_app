package com.riversongai.data.repository

import com.riversongai.data.model.ChatRequest
import com.riversongai.data.model.ChatResponse
import com.riversongai.data.remote.RiverSongApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.ResponseBody
import java.io.BufferedReader
import java.io.InputStreamReader

class ConversationRepository(private val api: RiverSongApiService) {

    suspend fun sendMessage(request: ChatRequest): Result<ChatResponse> = runCatching {
        val r = api.sendMessage(request)
        if (r.isSuccessful) r.body()!! else error(r.code().toString())
    }

    fun streamChat(request: ChatRequest): Flow<String> = flow {
        val response = api.chatHttp(request)
        if (!response.isSuccessful) {
            emit("[ERROR] HTTP ${response.code()}")
            return@flow
        }

        val body = response.body() ?: return@flow
        val reader = BufferedReader(InputStreamReader(body.byteStream()))
        
        try {
            var line: String? = reader.readLine()
            while (line != null) {
                if (line.startsWith("data: ")) {
                    val content = line.substring(6)
                    emit(content)
                    if (content == "[DONE]") break
                }
                line = reader.readLine()
            }
        } catch (e: Exception) {
            emit("[ERROR] ${e.message}")
        } finally {
            reader.close()
        }
    }

    suspend fun getChatHistory() = runCatching {
        val r = api.getChatHistory()
        if (r.isSuccessful) r.body()!! else error(r.code().toString())
    }
}
