package com.bluetoothcodec.checker

import android.content.Context
import android.media.AudioManager
import android.os.BatteryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class SmartRecommendations(
    val optimalCodec: String = "Unknown",
    val codecReason: String = "Analyzing...",
    val volumeRecommendation: Int = 0,
    val volumeReason: String = "Calculating...",
    val stabilityScore: Int = 0,
    val stabilityReason: String = "Evaluating...",
    val batteryEstimate: String = "Unknown",
    val batteryReason: String = "Estimating...",
    val environmentTip: String = "Analyzing environment..."
)

class PredictiveIntelligence(private val context: Context) {
    
    private val _recommendations = MutableStateFlow(SmartRecommendations())
    val recommendations: StateFlow<SmartRecommendations> = _recommendations
    
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    
    fun analyzeAndRecommend(
        currentCodec: String,
        devices: List<BluetoothDevice>,
        nearbySignals: Int,
        audioMetrics: AudioQualityMetrics
    ) {
        val optimalCodec = predictOptimalCodec(devices, nearbySignals, audioMetrics)
        val volumeRec = predictOptimalVolume(audioMetrics, nearbySignals)
        val stability = predictConnectionStability(devices, nearbySignals, audioMetrics)
        val battery = predictBatteryLife(currentCodec, audioMetrics)
        val envTip = generateEnvironmentTip(nearbySignals, audioMetrics, devices)
        
        _recommendations.value = SmartRecommendations(
            optimalCodec = optimalCodec.first,
            codecReason = optimalCodec.second,
            volumeRecommendation = volumeRec.first,
            volumeReason = volumeRec.second,
            stabilityScore = stability.first,
            stabilityReason = stability.second,
            batteryEstimate = battery.first,
            batteryReason = battery.second,
            environmentTip = envTip
        )
    }
    
    private fun predictOptimalCodec(
        devices: List<BluetoothDevice>,
        nearbySignals: Int,
        audioMetrics: AudioQualityMetrics
    ): Pair<String, String> {
        val currentDevice = devices.firstOrNull()
        
        return when {
            // High interference environment
            nearbySignals > 15 -> Pair("AAC", "High interference detected - AAC more stable")
            
            // Low latency needed (gaming/calls)
            audioMetrics.actualLatency < 10 -> Pair("aptX LL", "Low latency optimal for real-time audio")
            
            // High quality audio setup
            audioMetrics.actualSampleRate >= 48000 && nearbySignals < 8 -> 
                Pair("LDAC", "Hi-res capable with clean environment")
            
            // Battery conservation
            getBatteryLevel() < 20 -> Pair("SBC", "Battery saving mode recommended")
            
            // Balanced performance
            else -> Pair("aptX", "Balanced quality and stability")
        }
    }
    
    private fun predictOptimalVolume(
        audioMetrics: AudioQualityMetrics,
        nearbySignals: Int
    ): Pair<Int, String> {
        val currentVolume = audioMetrics.volume
        val maxVolume = audioMetrics.maxVolume
        
        val optimal = when {
            nearbySignals > 20 -> (maxVolume * 0.8f).toInt()
            nearbySignals > 10 -> (maxVolume * 0.7f).toInt()
            audioMetrics.isBluetoothActive -> (maxVolume * 0.6f).toInt()
            else -> (maxVolume * 0.5f).toInt()
        }
        
        val reason = when {
            optimal > currentVolume -> "Increase volume for better signal clarity"
            optimal < currentVolume -> "Reduce volume to minimize interference"
            else -> "Current volume is optimal"
        }
        
        return Pair(optimal, reason)
    }
    
    private fun predictConnectionStability(
        devices: List<BluetoothDevice>,
        nearbySignals: Int,
        audioMetrics: AudioQualityMetrics
    ): Pair<Int, String> {
        var score = 100
        var reasons = mutableListOf<String>()
        
        // Interference impact
        when {
            nearbySignals > 20 -> {
                score -= 40
                reasons.add("High interference")
            }
            nearbySignals > 10 -> {
                score -= 20
                reasons.add("Moderate interference")
            }
        }
        
        // Audio system health
        if (audioMetrics.actualLatency > 20) {
            score -= 15
            reasons.add("High latency")
        }
        
        if (audioMetrics.actualBufferSize > 512) {
            score -= 10
            reasons.add("Large buffer")
        }
        
        // Device signal strength
        val avgSignal = devices.mapNotNull { it.signalStrength }.average()
        if (avgSignal < 50) {
            score -= 25
            reasons.add("Weak signal")
        }
        
        score = score.coerceIn(0, 100)
        val reason = if (reasons.isEmpty()) "Excellent conditions" else reasons.joinToString(", ")
        
        return Pair(score, reason)
    }
    
    private fun predictBatteryLife(
        currentCodec: String,
        audioMetrics: AudioQualityMetrics
    ): Pair<String, String> {
        val batteryLevel = getBatteryLevel()
        
        val codecDrain = when (currentCodec) {
            "LDAC" -> 1.5f
            "aptX HD", "aptX Adaptive" -> 1.3f
            "aptX", "aptX LL" -> 1.1f
            "AAC" -> 1.0f
            "SBC" -> 0.9f
            else -> 1.0f
        }
        
        val volumeDrain = audioMetrics.volume.toFloat() / audioMetrics.maxVolume * 0.3f + 0.7f
        val totalDrain = codecDrain * volumeDrain
        
        val estimatedHours = (batteryLevel / (totalDrain * 10)).toInt()
        
        val estimate = when {
            estimatedHours > 8 -> "8+ hours"
            estimatedHours > 0 -> "${estimatedHours}h"
            else -> "<1h"
        }
        
        val reason = "Based on $currentCodec codec and ${audioMetrics.volume}/${audioMetrics.maxVolume} volume"
        
        return Pair(estimate, reason)
    }
    
    private fun generateEnvironmentTip(
        nearbySignals: Int,
        audioMetrics: AudioQualityMetrics,
        devices: List<BluetoothDevice>
    ): String {
        return when {
            nearbySignals > 20 -> "Move to less crowded area or use wired connection"
            nearbySignals > 10 -> "Keep device within 1-2 meters for stability"
            audioMetrics.actualLatency > 30 -> "Close other apps to reduce audio processing delay"
            devices.any { (it.signalStrength ?: 0) < 30 } -> "Move closer to device or remove obstacles"
            audioMetrics.volume > audioMetrics.maxVolume * 0.8 -> "Lower volume to extend battery life"
            else -> "Optimal conditions - enjoy your audio!"
        }
    }
    
    private fun getBatteryLevel(): Int {
        return try {
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        } catch (e: Exception) {
            50 // Default fallback
        }
    }
}
