package com.riversongai.data.repository

import android.util.Log
import com.riversongai.data.model.ChatModel
import com.riversongai.data.model.ChatSession
import com.riversongai.data.model.ChatSessionDetail
import com.riversongai.data.remote.RiverSongApiService
import com.riversongai.utils.SessionManager
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ConversationRepository(
    private val apiService: RiverSongApiService,
    private val sessionManager: SessionManager
) {

    private val tag = "ConversationRepository"

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .pingInterval(30, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null

    fun connect(
        baseUrl: String,
        modelId: String? = null,
        onMessage: (type: String, text: String?) -> Unit,
        onConnected: () -> Unit,
        onDisconnected: () -> Unit,
        onError: (String) -> Unit
    ) {
        val token = sessionManager.getAuthToken() ?: run {
            onError("Not authenticated")
            return
        }
        var wsUrl = baseUrl
            .replace("https://", "wss://")
            .replace("http://", "ws://")
            .trimEnd('/') + "/ws/conversation?token=$token"

        if (modelId != null) {
            wsUrl += "&model=$modelId"
        }

        Log.d(tag, "Connecting to $wsUrl")
        val request = Request.Builder().url(wsUrl).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                Log.d(tag, "WebSocket connected")
                // If model selection is needed as a separate message, we could do it here
                // but adding it to query params is common.
                // The prompt said: "When selectedModel is non-null, include "model": model.id in the WebSocket connect message"
                // If "connect message" means the first message sent:
                if (modelId != null) {
                    val connectMsg = JSONObject().apply {
                        put("type", "connect")
                        put("model", modelId)
                    }
                    ws.send(connectMsg.toString())
                }
                onConnected()
            }
// ... rest of methods unchanged

            override fun onMessage(ws: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    val type = json.optString("type", "unknown")
                    val msgText = json.optString("text", null)
                        ?: json.optString("message", null)
                    onMessage(type, msgText)
                } catch (e: Exception) {
                    Log.e(tag, "Failed to parse WS message: $text", e)
                }
            }

            override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                ws.close(1000, null)
                onDisconnected()
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e(tag, "WebSocket failure", t)
                onError(t.message ?: "Connection failed")
                onDisconnected()
            }
        })
    }

    fun sendText(text: String) {
        val payload = JSONObject().apply {
            put("type", "text_input")
            put("text", text)
        }
        webSocket?.send(payload.toString())
    }

    fun sendAudio(base64Wav: String) {
        val payload = JSONObject().apply {
            put("type", "audio_data")
            put("data", base64Wav)
        }
        webSocket?.send(payload.toString())
    }

    fun resetHistory() {
        val payload = JSONObject().apply { put("type", "reset_history") }
        webSocket?.send(payload.toString())
    }

    fun isConnected(): Boolean = webSocket != null

    fun disconnect() {
        webSocket?.close(1000, "User disconnected")
        webSocket = null
    }

    fun apiService() = apiService

    suspend fun getModels() = apiService.getChatModels()
    suspend fun getHistory() = apiService.getChatHistory()
    suspend fun getSessionDetail(sessionId: String) = apiService.getChatSessionDetail(sessionId)
}
