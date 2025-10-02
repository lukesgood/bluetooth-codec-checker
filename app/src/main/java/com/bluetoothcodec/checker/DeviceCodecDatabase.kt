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
        "airpods max" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        "airpods" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC),
        
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
        "buds z2" to listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC, BluetoothCodecs.APTX)
    )
    
    fun getSupportedCodecs(deviceName: String): List<String>? {
        val normalizedName = deviceName.lowercase()
            .replace("-", " ")
            .replace("_", " ")
            .trim()
        
        // Direct match
        deviceDatabase[normalizedName]?.let { return it }
        
        // Partial match
        for ((key, codecs) in deviceDatabase) {
            if (normalizedName.contains(key) || key.contains(normalizedName)) {
                return codecs
            }
        }
        
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
            else -> listOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC)
        }
    }
}
