package com.bluetoothcodec.checker

data class BluetoothDevice(
    val name: String,
    val address: String,
    val isConnected: Boolean,
    val activeCodec: String? = null,
    val supportedCodecs: List<String> = emptyList(),
    val batteryLevel: Int? = null, // Battery percentage (0-100) or null if unavailable
    val signalStrength: Int? = null // RSSI in dBm or null if unavailable
)

data class ChipsetInfo(
    val name: String,
    val manufacturer: String = "",
    val model: String = "",
    val osVersion: String = "",
    val androidVersion: String = "",
    val supportedCodecs: List<String>
)

data class CodecInfo(
    val name: String,
    val quality: Float, // 0.0 to 1.0 (음질)
    val speed: Float,   // 0.0 to 1.0 (속도/지연시간)
    val bitrate: String = "",
    val sampleRate: String = "",
    val latency: String = "",
    val description: String = ""
)

object BluetoothCodecs {
    const val SBC = "SBC"
    const val AAC = "AAC"
    const val APTX = "aptX"
    const val APTX_HD = "aptX HD"
    const val APTX_LL = "aptX LL"
    const val APTX_ADAPTIVE = "aptX Adaptive"
    const val LDAC = "LDAC"
    const val LC3 = "LC3"
    
    val ALL_CODECS = listOf(SBC, AAC, APTX, APTX_HD, APTX_LL, APTX_ADAPTIVE, LDAC, LC3)
    
    val CODEC_INFO = mapOf(
        SBC to CodecInfo(
            SBC, quality = 0.1f, speed = 0.9f,
            bitrate = "328 kbps", sampleRate = "44.1 kHz", latency = "~40ms",
            description = "Basic codec, supported by all devices"
        ),
        AAC to CodecInfo(
            AAC, quality = 0.4f, speed = 0.7f,
            bitrate = "320 kbps", sampleRate = "44.1 kHz", latency = "~80ms",
            description = "Apple optimized, efficient compression"
        ),
        APTX to CodecInfo(
            APTX, quality = 0.6f, speed = 0.8f,
            bitrate = "352 kbps", sampleRate = "48 kHz", latency = "~40ms",
            description = "Qualcomm developed, low latency"
        ),
        APTX_HD to CodecInfo(
            APTX_HD, quality = 0.8f, speed = 0.5f,
            bitrate = "576 kbps", sampleRate = "48 kHz", latency = "~130ms",
            description = "High quality version, 24-bit support"
        ),
        APTX_LL to CodecInfo(
            APTX_LL, quality = 0.6f, speed = 0.95f,
            bitrate = "352 kbps", sampleRate = "48 kHz", latency = "~32ms",
            description = "Low latency version, gaming optimized"
        ),
        APTX_ADAPTIVE to CodecInfo(
            APTX_ADAPTIVE, quality = 0.7f, speed = 0.6f,
            bitrate = "279-420 kbps", sampleRate = "48-96 kHz", latency = "50-80ms",
            description = "Adaptive bitrate, optimized by situation"
        ),
        LDAC to CodecInfo(
            LDAC, quality = 0.95f, speed = 0.2f,
            bitrate = "330-990 kbps", sampleRate = "96 kHz", latency = "~200ms",
            description = "Sony developed, highest quality"
        ),
        LC3 to CodecInfo(
            LC3, quality = 0.5f, speed = 0.85f,
            bitrate = "160 kbps", sampleRate = "48 kHz", latency = "~20ms",
            description = "Next-gen standard, low power"
        )
    )
}
