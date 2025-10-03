package com.bluetoothcodec.checker

object DeviceCodecDatabase {
    
    private val deviceDatabase = mapOf(
        // Sony
        "wh-1000xm5" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.LDAC),
        "wh-1000xm4" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.LDAC),
        "wh-1000xm3" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.LDAC),
        "wf-1000xm4" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.LDAC),
        "wf-1000xm3" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.LDAC),
        "wh-ch720n" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.LDAC),
        "wh-xb910n" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.LDAC),
        
        // Sennheiser
        "hd 350bt" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX),
        "hd350bt" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX),
        "hd 450bt" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX),
        "hd 550bt" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX, BluetoothCodecs.APTX_HD),
        "momentum 3" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX, BluetoothCodecs.APTX_HD),
        "momentum 4" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX, BluetoothCodecs.APTX_HD, BluetoothCodecs.APTX_ADAPTIVE),
        "cx plus" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX),
        
        // Bose
        "quietcomfort 45" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        "quietcomfort 35" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        "quietcomfort earbuds" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        "soundlink" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        "700" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        
        // Apple
        "airpods pro" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        "airpods pro 2" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        "airpods pro (2nd generation)" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        "airpods pro (3rd generation)" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        "airpods pro 3" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        "airpods pro usb-c" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        "airpods pro lightning" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        "airpods max" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        "airpods (3rd generation)" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        "airpods (2nd generation)" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        "airpods (1st generation)" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        "airpods 4" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        "airpods 3" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        "airpods 2" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        "airpods" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        
        // Beats (Apple-owned)
        "beats studio3" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        "beats studio 3" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        "beats solo3" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        "beats solo 3" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        "beats solo4" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        "beats solo 4" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        "beats solo pro" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        "beats studio buds" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        "beats studio buds +" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        "beats studio buds plus" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        "beats fit pro" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        "beats powerbeats pro" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        "beats powerbeats3" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        "beats x" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        "beatsx" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        
        // Samsung
        "galaxy buds pro" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX),
        "galaxy buds2 pro" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX),
        "galaxy buds live" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX),
        
        // Audio-Technica
        "ath-m50xbt" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX),
        "ath-anc900bt" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX, BluetoothCodecs.APTX_HD),
        "ath-ws1100is" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX),
        
        // Jabra
        "elite 85h" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX),
        "elite 75t" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        "elite active 75t" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        
        // Anker Soundcore
        "life q30" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX),
        "liberty 3 pro" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.LDAC),
        "space q45" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.LDAC),
        
        // JBL
        "live 650btnc" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        "club pro+" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        "tune 760nc" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        
        // Beats
        "studio3" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        "solo pro" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        "powerbeats pro" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        
        // Skullcandy
        "crusher anc" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX),
        "venue" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX),
        
        // Philips
        "fidelio l3" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX, BluetoothCodecs.APTX_HD),
        "ph805" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX),
        
        // Marshall
        "major iv" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX),
        "monitor ii anc" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX),
        
        // Xiaomi
        "mi true wireless" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX),
        "redmi buds 3 pro" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX),
        
        // OnePlus
        "buds pro" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX),
        "buds z2" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX),
        
        // LG
        "tone free" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX, BluetoothCodecs.APTX_ADAPTIVE),
        "tone free t90s" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX, BluetoothCodecs.APTX_ADAPTIVE),
        "tone free t90" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX, BluetoothCodecs.APTX_ADAPTIVE),
        "tone free fp9" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX, BluetoothCodecs.APTX_ADAPTIVE),
        "tone free fp8" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX, BluetoothCodecs.APTX_ADAPTIVE),
        "tone+" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX),
        "tone ultra" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX),
        "hbs-" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX),
        "lg tone" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX, BluetoothCodecs.APTX_ADAPTIVE),
        
        // Additional LG variations
        "lg" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX, BluetoothCodecs.APTX_ADAPTIVE),
        "tone" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX, BluetoothCodecs.APTX_ADAPTIVE),
        "t90s" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX, BluetoothCodecs.APTX_ADAPTIVE),
        "t90" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX, BluetoothCodecs.APTX_ADAPTIVE)
    )
    
    fun getSupportedCodecs(deviceName: String): List<String>? {
        val normalizedName = deviceName.lowercase()
            .replace("-", " ")
            .replace("_", " ")
            .replace("(", "")
            .replace(")", "")
            .trim()
        
        android.util.Log.d("DeviceCodecDB", "Checking device: '$deviceName' -> normalized: '$normalizedName'")
        
        // Direct match first
        deviceDatabase[normalizedName]?.let { 
            android.util.Log.d("DeviceCodecDB", "Direct match found: $it")
            return it 
        }
        
        // Partial match - check if device name contains any key
        for ((key, codecs) in deviceDatabase) {
            if (normalizedName.contains(key) || key.contains(normalizedName)) {
                android.util.Log.d("DeviceCodecDB", "Partial match '$key' found: $codecs")
                return codecs
            }
        }
        
        // Special cases for LG devices - check for any LG/Tone patterns
        when {
            normalizedName.contains("tone free") -> {
                android.util.Log.d("DeviceCodecDB", "Tone Free pattern match")
                return listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX, BluetoothCodecs.APTX_ADAPTIVE)
            }
            normalizedName.contains("lg") && normalizedName.contains("tone") -> {
                android.util.Log.d("DeviceCodecDB", "LG Tone pattern match")
                return listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX, BluetoothCodecs.APTX_ADAPTIVE)
            }
            normalizedName.contains("t90") -> {
                android.util.Log.d("DeviceCodecDB", "T90 pattern match")
                return listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX, BluetoothCodecs.APTX_ADAPTIVE)
            }
            normalizedName.contains("lg") -> {
                android.util.Log.d("DeviceCodecDB", "LG brand match")
                return listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX, BluetoothCodecs.APTX_ADAPTIVE)
            }
            normalizedName.contains("tone") -> {
                android.util.Log.d("DeviceCodecDB", "Tone pattern match")
                return listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX, BluetoothCodecs.APTX_ADAPTIVE)
            }
        }
        
        android.util.Log.d("DeviceCodecDB", "No match found for: '$normalizedName'")
        return null
    }
    
    fun getBrandCodecs(deviceName: String): List<String> {
        val name = deviceName.lowercase()
        return when {
            name.contains("sony") -> listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.LDAC)
            name.contains("sennheiser") -> listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX)
            name.contains("bose") -> listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC)
            name.contains("apple") || name.contains("airpods") -> listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC)
            name.contains("beats") -> listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC)
            name.contains("samsung") -> listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX)
            name.contains("jabra") -> listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC)
            name.contains("jbl") -> listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC)
            name.contains("anker") || name.contains("soundcore") -> listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX)
            name.contains("lg") -> listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX, BluetoothCodecs.APTX_ADAPTIVE)
            else -> listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC)
        }
    }
}
