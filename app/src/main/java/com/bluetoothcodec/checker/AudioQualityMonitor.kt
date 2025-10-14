package com.bluetoothcodec.checker

import android.content.Context
import android.media.AudioManager
import android.media.AudioTrack
import android.media.AudioFormat
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.*

data class AudioQualityMetrics(
    val actualSampleRate: Int = 0,      // Real sample rate from system
    val actualBufferSize: Int = 0,      // Real buffer size
    val actualLatency: Int = 0,         // Real audio latency
    val volume: Int = 0,                // Current volume level
    val maxVolume: Int = 0,             // Maximum volume level
    val isBluetoothActive: Boolean = false, // Real Bluetooth state
    val audioMode: String = "Unknown",  // Current audio mode
    val lastUpdate: Long = 0L           // Timestamp of last update
)

class AudioQualityMonitor(private val context: Context) {
    
    private val _audioMetrics = MutableStateFlow(AudioQualityMetrics())
    val audioMetrics: StateFlow<AudioQualityMetrics> = _audioMetrics
    
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val handler = Handler(Looper.getMainLooper())
    private var isMonitoring = false
    private var monitoringRunnable: Runnable? = null
    
    fun checkOnce() {
        updateRealAudioMetrics()
    }
    
    fun startMonitoring() {
        if (isMonitoring) return
        isMonitoring = true
        
        monitoringRunnable = object : Runnable {
            override fun run() {
                if (isMonitoring) {
                    updateRealAudioMetrics()
                    handler.postDelayed(this, 250) // Update every 250ms for real-time feel
                }
            }
        }
        handler.post(monitoringRunnable!!)
    }
    
    fun stopMonitoring() {
        isMonitoring = false
        monitoringRunnable?.let { handler.removeCallbacks(it) }
    }
    
    private fun updateRealAudioMetrics() {
        val sampleRate = getRealSampleRate()
        val bufferSize = getRealBufferSize()
        val latency = getRealLatency()
        val volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val isBluetoothActive = isBluetoothAudioActive()
        val audioMode = getCurrentAudioMode()
        
        _audioMetrics.value = AudioQualityMetrics(
            actualSampleRate = sampleRate,
            actualBufferSize = bufferSize,
            actualLatency = latency,
            volume = volume,
            maxVolume = maxVolume,
            isBluetoothActive = isBluetoothActive,
            audioMode = audioMode,
            lastUpdate = System.currentTimeMillis()
        )
    }
    
    private fun getRealSampleRate(): Int {
        return try {
            val sampleRateStr = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)
            sampleRateStr?.toIntOrNull() ?: 0
        } catch (e: Exception) {
            0
        }
    }
    
    private fun getRealBufferSize(): Int {
        return try {
            val bufferSizeStr = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER)
            bufferSizeStr?.toIntOrNull() ?: 0
        } catch (e: Exception) {
            0
        }
    }
    
    private fun getRealLatency(): Int {
        return try {
            // Calculate real latency from buffer size and sample rate
            val bufferSize = getRealBufferSize()
            val sampleRate = getRealSampleRate()
            
            if (bufferSize > 0 && sampleRate > 0) {
                ((bufferSize.toFloat() / sampleRate) * 1000).toInt()
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }
    
    private fun isBluetoothAudioActive(): Boolean {
        return try {
            audioManager.isBluetoothA2dpOn || 
            audioManager.isBluetoothScoOn ||
            audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).any { 
                it.type == android.media.AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                it.type == android.media.AudioDeviceInfo.TYPE_BLUETOOTH_SCO
            }
        } catch (e: Exception) {
            false
        }
    }
    
    private fun getCurrentAudioMode(): String {
        return try {
            when (audioManager.mode) {
                AudioManager.MODE_NORMAL -> "Normal"
                AudioManager.MODE_RINGTONE -> "Ringtone"
                AudioManager.MODE_IN_CALL -> "In Call"
                AudioManager.MODE_IN_COMMUNICATION -> "Communication"
                else -> "Unknown"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }
}
