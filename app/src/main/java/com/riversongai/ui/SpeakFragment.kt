package com.riversongai.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.riversongai.R
import com.riversongai.databinding.FragmentSpeakBinding
import com.riversongai.ui.viewmodel.ChatViewModel
import com.riversongai.ui.widget.PresenceOrbView
import com.riversongai.utils.SessionManager
import okhttp3.*
import okio.ByteString.Companion.toByteString
import org.json.JSONObject
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.util.concurrent.TimeUnit

class SpeakFragment : Fragment(R.layout.fragment_speak) {

    private var _binding: FragmentSpeakBinding? = null
    private val binding get() = _binding!!
    private val sessionManager: SessionManager by inject()
    
    private var webSocket: WebSocket? = null
    private val okHttpClient = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSpeakBinding.bind(view)

        binding.buttonMic.setOnClickListener {
            if (binding.textViewStatus.text == "LISTENING") {
                stopRecording()
            } else {
                startRecording()
            }
        }

        binding.switchAmbient.setOnCheckedChangeListener { _, isChecked ->
            webSocket?.send(JSONObject().put("type", "ambient_mode").put("enabled", isChecked).toString())
        }

        connectWebSocket()
    }

    private fun connectWebSocket() {
        val token = sessionManager.getAuthToken() ?: return
        val request = Request.Builder()
            .url("${com.riversongai.utils.Constants.BASE_URL.replace("http", "ws")}ws/conversation?token=$token")
            .build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                activity?.runOnUiThread { binding.textViewStatus.text = "CONNECTED" }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val msg = JSONObject(text)
                val type = msg.optString("type")
                activity?.runOnUiThread {
                    when (type) {
                        "listening" -> {
                            binding.textViewStatus.text = "LISTENING"
                            binding.viewOrbPulse.setState(PresenceOrbView.OrbState.LISTENING)
                        }
                        "transcribing" -> {
                            binding.textViewStatus.text = "TRANSCRIBING"
                            binding.viewOrbPulse.setState(PresenceOrbView.OrbState.THINKING)
                        }
                        "transcript" -> binding.textViewTranscript.text = msg.optString("text")
                        "thinking" -> {
                            binding.textViewStatus.text = "THINKING"
                            binding.viewOrbPulse.setState(PresenceOrbView.OrbState.THINKING)
                        }
                        "response_chunk" -> binding.textViewResponse.append(msg.optString("text"))
                        "response_complete" -> {
                            binding.textViewResponse.text = msg.optString("text")
                            binding.textViewStatus.text = "SPEAKING"
                            binding.viewOrbPulse.setState(PresenceOrbView.OrbState.SPEAKING)
                        }
                        "audio" -> playAudio(msg.optString("data"))
                        "idle" -> {
                            binding.textViewStatus.text = "IDLE"
                            binding.viewOrbPulse.setState(PresenceOrbView.OrbState.IDLE)
                        }
                        "error" -> {
                            Toast.makeText(context, msg.optString("message"), Toast.LENGTH_LONG).show()
                            binding.viewOrbPulse.setState(PresenceOrbView.OrbState.ATTENTION)
                        }
                    }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                activity?.runOnUiThread {
                    binding.textViewStatus.text = "DISCONNECTED"
                    binding.viewOrbPulse.setState(PresenceOrbView.OrbState.IDLE)
                }
            }
        })
    }

    private fun startRecording() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 100)
            return
        }
        webSocket?.send(JSONObject().put("type", "start").toString())
    }

    private fun stopRecording() {
        // In a real app, we'd send the actual audio buffer here as base64
        // webSocket?.send(JSONObject().put("type", "audio_data").put("data", b64).toString())
    }

    private fun playAudio(base64Wav: String) {
        // Decode and play via AudioTrack or MediaPlayer
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webSocket?.close(1000, "Fragment destroyed")
        _binding = null
    }
}
