package com.riversongai.utils

import android.Manifest
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Base64
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class AudioRecorder(private val context: Context) {

    companion object {
        private const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    private var audioRecord: AudioRecord? = null
    private var isRecording = false

    val isActive: Boolean get() = isRecording

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun start() {
        val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            bufferSize
        )
        audioRecord?.startRecording()
        isRecording = true
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    suspend fun stopAndEncode(): String = withContext(Dispatchers.IO) {
        isRecording = false
        val record = audioRecord ?: return@withContext ""
        val pcm = ByteArrayOutputStream()
        val buffer = ByteArray(4096)
        var read: Int
        while (record.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            read = record.read(buffer, 0, buffer.size)
            if (read > 0) pcm.write(buffer, 0, read)
        }
        record.stop()
        record.release()
        audioRecord = null

        val wav = WavEncoder.encode(pcm.toByteArray(), SAMPLE_RATE)
        Base64.encodeToString(wav, Base64.NO_WRAP)
    }

    fun cancel() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }
}
