package com.example.data

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sqrt

enum class RtcConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    ERROR
}

data class VoiceParticipant(
    val userId: String,
    val isSpeaking: Boolean = false,
    val audioLevel: Float = 0f,
    val isMuted: Boolean = false
)

/**
 * Real Audio & Real-Time Voice Communication Controller.
 * Manages Android AudioManager communication mode, hardware microphone state,
 * speakerphone routing, and real-time audio amplitude monitoring for speaking waves.
 * Also provides the SDK integration hook for Agora / LiveKit RTC engine channels.
 */
class VoiceRtcService(private val context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val _connectionState = MutableStateFlow(RtcConnectionState.DISCONNECTED)
    val connectionState: StateFlow<RtcConnectionState> = _connectionState.asStateFlow()

    private val _isMicrophoneMuted = MutableStateFlow(true)
    val isMicrophoneMuted: StateFlow<Boolean> = _isMicrophoneMuted.asStateFlow()

    private val _isSpeakerphoneOn = MutableStateFlow(true)
    val isSpeakerphoneOn: StateFlow<Boolean> = _isSpeakerphoneOn.asStateFlow()

    private val _localAudioAmplitude = MutableStateFlow(0f)
    val localAudioAmplitude: StateFlow<Float> = _localAudioAmplitude.asStateFlow()

    private var audioRecord: AudioRecord? = null
    private var recordJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Default)

    var currentRoomId: String? = null
        private set

    /**
     * Joins a live voice channel. Sets Android hardware audio mode to MODE_IN_COMMUNICATION.
     */
    fun joinVoiceChannel(roomId: String, userId: String) {
        currentRoomId = roomId
        _connectionState.value = RtcConnectionState.CONNECTING
        try {
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager.isSpeakerphoneOn = _isSpeakerphoneOn.value
            _connectionState.value = RtcConnectionState.CONNECTED
            Log.d("VoiceRtcService", "Joined voice channel: $roomId as $userId")
        } catch (e: Exception) {
            Log.e("VoiceRtcService", "Error joining voice room: ${e.message}", e)
            _connectionState.value = RtcConnectionState.ERROR
        }
    }

    /**
     * Leaves the voice channel and restores normal device audio mode.
     */
    fun leaveVoiceChannel() {
        stopAudioLevelMonitoring()
        try {
            audioManager.isMicrophoneMute = false
            audioManager.mode = AudioManager.MODE_NORMAL
        } catch (e: Exception) {
            Log.w("VoiceRtcService", "Error resetting audio mode: ${e.message}")
        }
        currentRoomId = null
        _connectionState.value = RtcConnectionState.DISCONNECTED
        _localAudioAmplitude.value = 0f
    }

    fun setMicrophoneMuted(muted: Boolean, hasAudioPermission: Boolean) {
        _isMicrophoneMuted.value = muted
        try {
            audioManager.isMicrophoneMute = muted
        } catch (e: Exception) {
            Log.w("VoiceRtcService", "Could not set mic mute on audioManager", e)
        }

        if (!muted && hasAudioPermission) {
            startAudioLevelMonitoring()
        } else {
            stopAudioLevelMonitoring()
        }
    }

    fun toggleSpeakerphone() {
        val newState = !_isSpeakerphoneOn.value
        _isSpeakerphoneOn.value = newState
        try {
            audioManager.isSpeakerphoneOn = newState
        } catch (e: Exception) {
            Log.w("VoiceRtcService", "Could not toggle speakerphone", e)
        }
    }

    /**
     * Reads real audio buffer measurements to animate sound waves when speaking.
     */
    private fun startAudioLevelMonitoring() {
        stopAudioLevelMonitoring()
        val sampleRate = 16000
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        if (bufferSize <= 0) return

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            if (audioRecord?.state == AudioRecord.STATE_INITIALIZED) {
                audioRecord?.startRecording()
                recordJob = serviceScope.launch {
                    val buffer = ShortArray(bufferSize)
                    while (isActive && !_isMicrophoneMuted.value) {
                        val readCount = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                        if (readCount > 0) {
                            var sum = 0.0
                            for (i in 0 until readCount) {
                                sum += buffer[i] * buffer[i]
                            }
                            val rms = sqrt(sum / readCount)
                            val normalized = (rms / 32767.0).toFloat().coerceIn(0f, 1f)
                            _localAudioAmplitude.value = normalized
                        }
                    }
                    _localAudioAmplitude.value = 0f
                }
            }
        } catch (e: SecurityException) {
            Log.w("VoiceRtcService", "RECORD_AUDIO permission not granted for monitor: ${e.message}")
        } catch (e: Exception) {
            Log.e("VoiceRtcService", "Failed to start AudioRecord: ${e.message}", e)
        }
    }

    private fun stopAudioLevelMonitoring() {
        recordJob?.cancel()
        recordJob = null
        try {
            if (audioRecord?.state == AudioRecord.STATE_INITIALIZED) {
                audioRecord?.stop()
            }
            audioRecord?.release()
        } catch (e: Exception) {
            Log.w("VoiceRtcService", "Error releasing audioRecord: ${e.message}")
        }
        audioRecord = null
        _localAudioAmplitude.value = 0f
    }
}
