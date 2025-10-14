package com.bluetoothcodec.checker

import android.content.Context
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class LiveMetrics(
    val sampleRate: String = "Unknown",
    val bufferSize: String = "Unknown", 
    val latency: String = "Unknown",
    val volume: String = "Unknown",
    val audioMode: String = "Unknown",
    val bluetoothActive: String = "Unknown",
    val nearbyDevices: String = "Unknown",
    val lastUpdate: Long = System.currentTimeMillis()
)

class MonitoringDashboard(private val context: Context) {
    
    private val _liveMetrics = MutableStateFlow(LiveMetrics())
    val liveMetrics: StateFlow<LiveMetrics> = _liveMetrics
    
    private val handler = Handler(Looper.getMainLooper())
    private var isMonitoring = false
    private var monitoringRunnable: Runnable? = null
    
    fun startMonitoring() {
        if (isMonitoring) return
        isMonitoring = true
        
        monitoringRunnable = object : Runnable {
            override fun run() {
                if (isMonitoring) {
                    updateTimestamp()
                    handler.postDelayed(this, 1000) // Update timestamp every second
                }
            }
        }
        handler.post(monitoringRunnable!!)
    }
    
    fun stopMonitoring() {
        isMonitoring = false
        monitoringRunnable?.let { handler.removeCallbacks(it) }
    }
    
    fun updateWithRealData(
        devices: List<BluetoothDevice>,
        nearbySignals: Int,
        audioMetrics: AudioQualityMetrics
    ) {
        _liveMetrics.value = LiveMetrics(
            sampleRate = "${audioMetrics.actualSampleRate} Hz",
            bufferSize = "${audioMetrics.actualBufferSize} frames",
            latency = "${audioMetrics.actualLatency} ms",
            volume = "${audioMetrics.volume}/${audioMetrics.maxVolume}",
            audioMode = audioMetrics.audioMode,
            bluetoothActive = if (audioMetrics.isBluetoothActive) "Yes" else "No",
            nearbyDevices = "$nearbySignals detected",
            lastUpdate = System.currentTimeMillis()
        )
    }
    
    private fun updateTimestamp() {
        _liveMetrics.value = _liveMetrics.value.copy(
            lastUpdate = System.currentTimeMillis()
        )
    }
}
