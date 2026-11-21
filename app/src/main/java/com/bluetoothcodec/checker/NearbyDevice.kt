package com.bluetoothcodec.checker

enum class Transport { BLE, CLASSIC }

/**
 * A device heard by a BLE scan or a classic inquiry. Every field is taken from the
 * radio or left null; nothing here is estimated.
 *
 * [address] is whatever the device advertised. Phones, watches and trackers rotate
 * random addresses every few minutes, so one physical device can show up as several
 * entries over time and cannot be followed for long.
 */
data class NearbyDevice(
    val address: String,
    val name: String?,
    /** Latest RSSI in dBm. */
    val rssi: Int,
    /** SystemClock.elapsedRealtime() of the latest sighting. */
    val lastSeenMs: Long,
    val transport: Transport,
    /** Bluetooth SIG company identifier from manufacturer-specific advertising data. */
    val companyId: Int? = null,
    val connectable: Boolean? = null,
    /** BluetoothClass major device class; classic inquiry only. */
    val majorClass: Int? = null,
    val bonded: Boolean = false,
    /** Most recent RSSI samples, oldest first, capped at [MAX_HISTORY]. */
    val rssiHistory: List<Int> = emptyList()
) {
    val displayName: String get() = name ?: "Unnamed device"

    val manufacturer: String? get() = companyId?.let { CompanyIdentifiers.nameFor(it) }

    /** Mean of the last few samples; steadier than a single reading for sorting and display. */
    val smoothedRssi: Int
        get() = if (rssiHistory.isEmpty()) rssi else rssiHistory.takeLast(5).average().toInt()

    companion object {
        const val MAX_HISTORY = 40
    }
}

enum class SignalTrend { RISING, FALLING, STEADY }

/**
 * Compares the mean of the last three samples with the three before them. Returns null
 * until there are six samples. A rising signal usually means the device is getting
 * closer, but bodies, walls and antenna orientation move RSSI just as much.
 */
fun signalTrend(history: List<Int>): SignalTrend? {
    if (history.size < 6) return null
    val recent = history.takeLast(3).average()
    val before = history.dropLast(3).takeLast(3).average()
    val delta = recent - before
    return when {
        delta >= 4 -> SignalTrend.RISING
        delta <= -4 -> SignalTrend.FALLING
        else -> SignalTrend.STEADY
    }
}

/** Coarse label for an RSSI value. Deliberately not a distance in metres. */
fun proximityLabel(rssi: Int): String = when {
    rssi >= -55 -> "Very close"
    rssi >= -70 -> "Nearby"
    rssi >= -85 -> "Farther away"
    else -> "Weak signal"
}

/** 0..4 bars for an RSSI value. */
fun signalBars(rssi: Int): Int = when {
    rssi >= -55 -> 4
    rssi >= -67 -> 3
    rssi >= -80 -> 2
    rssi >= -92 -> 1
    else -> 0
}

/**
 * A small subset of the Bluetooth SIG assigned company identifiers. The identifier names
 * whoever registered the advertising format, which is often but not always the brand on
 * the box (chip vendors appear here too). Unknown identifiers are shown as hex.
 */
object CompanyIdentifiers {
    private val names = mapOf(
        0x0002 to "Intel",
        0x0006 to "Microsoft",
        0x000A to "Qualcomm (CSR)",
        0x000F to "Broadcom",
        0x001D to "Qualcomm",
        0x004C to "Apple",
        0x0057 to "Harman",
        0x0059 to "Nordic Semiconductor",
        0x0067 to "GN (Jabra)",
        0x0075 to "Samsung",
        0x0087 to "Garmin",
        0x009E to "Bose",
        0x00C4 to "LG Electronics",
        0x00E0 to "Google",
        0x012D to "Sony",
        0x0171 to "Amazon",
        0x027D to "Huawei",
        0x02E5 to "Espressif",
        0x038F to "Xiaomi"
    )

    fun nameFor(companyId: Int): String =
        names[companyId] ?: "Company 0x%04X".format(companyId)
}
