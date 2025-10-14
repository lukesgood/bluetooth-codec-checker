package com.bluetoothcodec.checker

import android.content.Context
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.abs

data class LiveMetrics(
    val connectionStability: Int = 0,
    val audioQualityScore: Int = 0,
    val environmentRating: String = "Unknown",
    val performanceAlerts: List<String> = emptyList(),
    val trendIndicator: String = "Stable",
    val lastUpdate: Long = System.currentTimeMillis()
)

class MonitoringDashboard(private val context: Context) {
    
    private val _liveMetrics = MutableStateFlow(LiveMetrics())
    val liveMetrics: StateFlow<LiveMetrics> = _liveMetrics
    
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val handler = Handler(Looper.getMainLooper())
    private var isMonitoring = false
    private var monitoringRunnable: Runnable? = null
    
    // Historical data for trend analysis
    private val stabilityHistory = mutableListOf<Int>()
    private val qualityHistory = mutableListOf<Int>()
    private val alertHistory = mutableListOf<String>()
    
    fun startMonitoring() {
        if (isMonitoring) return
        isMonitoring = true
        
        monitoringRunnable = object : Runnable {
            override fun run() {
                if (isMonitoring) {
                    updateLiveMetrics()
                    handler.postDelayed(this, 2000) // Update every 2 seconds
                }
            }
        }
        handler.post(monitoringRunnable!!)
    }
    
    fun stopMonitoring() {
        isMonitoring = false
        monitoringRunnable?.let { handler.removeCallbacks(it) }
    }
    
    fun updateWithData(
        devices: List<BluetoothDevice>,
        nearbySignals: Int,
        audioMetrics: AudioQualityMetrics,
        currentCodec: String
    ) {
        val stability = calculateConnectionStability(devices, nearbySignals, audioMetrics)
        val quality = calculateAudioQuality(audioMetrics, currentCodec)
        val environment = assessEnvironment(nearbySignals, audioMetrics)
        val alerts = generatePerformanceAlerts(devices, nearbySignals, audioMetrics)
        val trend = analyzeTrend(stability, quality)
        
        // Store history for trend analysis
        stabilityHistory.add(stability)
        qualityHistory.add(quality)
        if (stabilityHistory.size > 10) stabilityHistory.removeAt(0)
        if (qualityHistory.size > 10) qualityHistory.removeAt(0)
        
        _liveMetrics.value = LiveMetrics(
            connectionStability = stability,
            audioQualityScore = quality,
            environmentRating = environment,
            performanceAlerts = alerts,
            trendIndicator = trend,
            lastUpdate = System.currentTimeMillis()
        )
    }
    
    private fun updateLiveMetrics() {
        // This runs every 2 seconds to update timestamp and check for changes
        _liveMetrics.value = _liveMetrics.value.copy(
            lastUpdate = System.currentTimeMillis()
        )
    }
    
    private fun calculateConnectionStability(
        devices: List<BluetoothDevice>,
        nearbySignals: Int,
        audioMetrics: AudioQualityMetrics
    ): Int {
        var stability = 100
        
        // Interference impact
        stability -= when {
            nearbySignals > 25 -> 50
            nearbySignals > 15 -> 30
            nearbySignals > 8 -> 15
            else -> 0
        }
        
        // Signal strength impact
        val avgSignal = devices.mapNotNull { it.signalStrength }.average()
        if (!avgSignal.isNaN()) {
            stability -= when {
                avgSignal < 30 -> 30
                avgSignal < 50 -> 15
                avgSignal < 70 -> 5
                else -> 0
            }
        }
        
        // Audio system impact
        stability -= when {
            audioMetrics.actualLatency > 50 -> 20
            audioMetrics.actualLatency > 30 -> 10
            audioMetrics.actualLatency > 20 -> 5
            else -> 0
        }
        
        return stability.coerceIn(0, 100)
    }
    
    private fun calculateAudioQuality(
        audioMetrics: AudioQualityMetrics,
        currentCodec: String
    ): Int {
        var quality = 50 // Base score
        
        // Sample rate contribution
        quality += when {
            audioMetrics.actualSampleRate >= 96000 -> 25
            audioMetrics.actualSampleRate >= 48000 -> 20
            audioMetrics.actualSampleRate >= 44100 -> 15
            else -> 5
        }
        
        // Latency contribution
        quality += when {
            audioMetrics.actualLatency <= 10 -> 15
            audioMetrics.actualLatency <= 20 -> 10
            audioMetrics.actualLatency <= 30 -> 5
            else -> 0
        }
        
        // Codec contribution
        quality += when (currentCodec) {
            "LDAC" -> 10
            "aptX HD", "aptX Adaptive" -> 8
            "aptX", "aptX LL" -> 6
            "AAC" -> 4
            "SBC" -> 2
            else -> 0
        }
        
        return quality.coerceIn(0, 100)
    }
    
    private fun assessEnvironment(nearbySignals: Int, audioMetrics: AudioQualityMetrics): String {
        return when {
            nearbySignals > 25 -> "Critical"
            nearbySignals > 15 -> "Challenging"
            nearbySignals > 8 -> "Moderate"
            nearbySignals > 3 -> "Good"
            else -> "Excellent"
        }
    }
    
    private fun generatePerformanceAlerts(
        devices: List<BluetoothDevice>,
        nearbySignals: Int,
        audioMetrics: AudioQualityMetrics
    ): List<String> {
        val alerts = mutableListOf<String>()
        
        if (nearbySignals > 20) {
            alerts.add("High interference detected")
        }
        
        if (audioMetrics.actualLatency > 30) {
            alerts.add("Audio delay may be noticeable")
        }
        
        val avgSignal = devices.mapNotNull { it.signalStrength }.average()
        if (!avgSignal.isNaN() && avgSignal < 40) {
            alerts.add("Weak Bluetooth signal")
        }
        
        if (audioMetrics.volume > audioMetrics.maxVolume * 0.9) {
            alerts.add("Volume near maximum")
        }
        
        if (!audioMetrics.isBluetoothActive) {
            alerts.add("Bluetooth audio not active")
        }
        
        return alerts
    }
    
    private fun analyzeTrend(currentStability: Int, currentQuality: Int): String {
        if (stabilityHistory.size < 3) return "Initializing"
        
        val stabilityTrend = stabilityHistory.takeLast(3).let { recent ->
            val avg = recent.average()
            when {
                currentStability > avg + 10 -> "Improving"
                currentStability < avg - 10 -> "Declining"
                else -> "Stable"
            }
        }
        
        val qualityTrend = qualityHistory.takeLast(3).let { recent ->
            val avg = recent.average()
            when {
                currentQuality > avg + 5 -> "Improving"
                currentQuality < avg - 5 -> "Declining"
                else -> "Stable"
            }
        }
        
        return when {
            stabilityTrend == "Improving" && qualityTrend == "Improving" -> "Improving"
            stabilityTrend == "Declining" || qualityTrend == "Declining" -> "Declining"
            else -> "Stable"
        }
    }
}
