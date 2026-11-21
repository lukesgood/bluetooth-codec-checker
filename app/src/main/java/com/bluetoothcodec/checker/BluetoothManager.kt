package com.bluetoothcodec.checker

import android.Manifest
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.core.app.ActivityCompat
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Reads Bluetooth audio state for the UI.
 *
 * Every value returned here is either measured or null. Nothing is estimated,
 * randomised or defaulted: when the platform does not expose a value to a
 * regular app, callers get null and are expected to show "unknown".
 *
 * Create one instance per process (the ViewModel owns it) and call [close]
 * when it is no longer needed.
 */
class BluetoothCodecManager(context: Context) {

    private val context: Context = context.applicationContext
    private val bluetoothManager =
        this.context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter
    private val audioManager =
        this.context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    @Volatile private var a2dpProfile: BluetoothA2dp? = null
    @Volatile private var profileRequested = false

    private val profileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile == BluetoothProfile.A2DP) a2dpProfile = proxy as BluetoothA2dp
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.A2DP) a2dpProfile = null
        }
    }

    // ---------------------------------------------------------------------
    // Permissions and adapter state
    // ---------------------------------------------------------------------

    fun hasBluetoothPermission(): Boolean {
        val bluetoothGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            isGranted(Manifest.permission.BLUETOOTH_CONNECT) &&
                isGranted(Manifest.permission.BLUETOOTH_SCAN)
        } else {
            isGranted(Manifest.permission.BLUETOOTH) &&
                isGranted(Manifest.permission.BLUETOOTH_ADMIN)
        }
        return bluetoothGranted && isGranted(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    fun isBluetoothEnabled(): Boolean {
        return try {
            bluetoothAdapter?.isEnabled == true
        } catch (e: SecurityException) {
            false
        }
    }

    private fun isGranted(permission: String): Boolean =
        ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    // ---------------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------------

    private fun ensureProfile() {
        if (a2dpProfile != null || profileRequested || !hasBluetoothPermission()) return
        try {
            profileRequested =
                bluetoothAdapter?.getProfileProxy(context, profileListener, BluetoothProfile.A2DP) == true
        } catch (e: SecurityException) {
            // Permission was revoked between the check and the call.
        }
    }

    /** Stops scanning and releases the A2DP proxy. */
    fun close() {
        stopBluetoothScan()
        val profile = a2dpProfile
        a2dpProfile = null
        profileRequested = false
        if (profile != null) {
            try {
                bluetoothAdapter?.closeProfileProxy(BluetoothProfile.A2DP, profile)
            } catch (e: Exception) {
                // Nothing useful to do if the stack is already gone.
            }
        }
    }

    // ---------------------------------------------------------------------
    // Phone-side information (static, computed once)
    // ---------------------------------------------------------------------

    private val cachedChipsetInfo: ChipsetInfo by lazy { buildChipsetInfo() }

    fun getChipsetInfo(): ChipsetInfo = cachedChipsetInfo

    private fun buildChipsetInfo(): ChipsetInfo {
        val supportedCodecs = mutableListOf(BluetoothCodecs.SBC, BluetoothCodecs.AAC)

        if (checkAptXSupport()) {
            supportedCodecs.add(BluetoothCodecs.APTX)
            supportedCodecs.add(BluetoothCodecs.APTX_HD)
            supportedCodecs.add(BluetoothCodecs.APTX_ADAPTIVE)
        }
        if (checkLdacSupport()) {
            supportedCodecs.add(BluetoothCodecs.LDAC)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            supportedCodecs.add(BluetoothCodecs.LC3)
        }

        return ChipsetInfo(
            name = getChipsetName(),
            manufacturer = Build.MANUFACTURER ?: "Unknown",
            model = Build.MODEL ?: "Unknown",
            osVersion = "Android ${Build.VERSION.RELEASE}",
            androidVersion = "API ${Build.VERSION.SDK_INT}",
            supportedCodecs = supportedCodecs
        )
    }

    private fun socManufacturer(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Build.SOC_MANUFACTURER ?: ""
        else getSystemProperty("ro.soc.manufacturer")

    private fun socModel(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Build.SOC_MODEL ?: ""
        else getSystemProperty("ro.soc.model")

    /** Heuristic: the platform has no public API for the phone's encoder list. */
    private fun checkAptXSupport(): Boolean {
        return isPropertySet("ro.vendor.bluetooth.aptx") ||
            isPropertySet("ro.bluetooth.aptx") ||
            isPropertySet("persist.vendor.bluetooth.aptx") ||
            Build.MANUFACTURER.contains("qualcomm", ignoreCase = true) ||
            Build.MANUFACTURER.contains("samsung", ignoreCase = true) ||
            Build.MANUFACTURER.contains("xiaomi", ignoreCase = true) ||
            Build.MANUFACTURER.contains("oneplus", ignoreCase = true) ||
            Build.HARDWARE.contains("qcom", ignoreCase = true) ||
            socManufacturer().contains("qualcomm", ignoreCase = true) ||
            Build.BOARD.contains("msm", ignoreCase = true) ||
            Build.BOARD.contains("sdm", ignoreCase = true) ||
            Build.BOARD.contains("sm", ignoreCase = true)
    }

    /** Heuristic: the platform has no public API for the phone's encoder list. */
    private fun checkLdacSupport(): Boolean {
        val ldacProps = arrayOf(
            "ro.vendor.bluetooth.ldac",
            "vendor.bluetooth.ldac.abr",
            "ro.audio.codec.ldac",
            "persist.vendor.bluetooth.ldac",
            "ro.bluetooth.ldac"
        )
        if (ldacProps.any { getSystemProperty(it) in setOf("true", "1", "supported") }) return true

        val manufacturer = Build.MANUFACTURER.lowercase()
        val model = Build.MODEL.lowercase()
        if (manufacturer.contains("xiaomi") || model.contains("redmi") ||
            model.contains("poco") || model.contains("mi ")
        ) return true
        if (manufacturer.contains("samsung") && model.contains("galaxy")) return true
        if (manufacturer.contains("sony")) return true

        return try {
            File("/vendor/lib/libldacBT_enc.so").exists() ||
                File("/vendor/lib64/libldacBT_enc.so").exists()
        } catch (e: SecurityException) {
            false
        }
    }

    private fun getChipsetName(): String {
        val socManufacturer = socManufacturer()
        val socModel = socModel()
        val hardware = Build.HARDWARE ?: ""
        val board = Build.BOARD ?: ""
        val platform = getSystemProperty("ro.board.platform")
        val chipset = getSystemProperty("ro.chipname")
        val abi = Build.SUPPORTED_ABIS.firstOrNull() ?: ""

        val parts = mutableListOf<String>()
        when {
            socManufacturer.isNotEmpty() && socModel.isNotEmpty() -> parts.add("$socManufacturer $socModel")
            chipset.isNotEmpty() -> parts.add(chipset)
            hardware.isNotEmpty() -> parts.add("${Build.MANUFACTURER} $hardware")
            else -> parts.add("${Build.MANUFACTURER} ${Build.MODEL}")
        }
        when {
            platform.isNotEmpty() && platform != hardware -> parts.add("($platform)")
            board.isNotEmpty() && board != hardware -> parts.add("($board)")
            hardware.isNotEmpty() -> parts.add("($hardware)")
        }
        if (abi.isNotEmpty()) parts.add("[$abi]")
        return parts.joinToString(" ")
    }

    private fun isPropertySet(property: String): Boolean {
        val value = getSystemProperty(property)
        return value.isNotEmpty() && value != "0" && value != "false"
    }

    /** Spawns `getprop`; only call from [buildChipsetInfo], which runs once. */
    private fun getSystemProperty(property: String): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("getprop", property))
            try {
                process.inputStream.bufferedReader().use { it.readText() }.trim()
            } finally {
                process.destroy()
            }
        } catch (e: Exception) {
            ""
        }
    }

    // ---------------------------------------------------------------------
    // Connected audio devices
    // ---------------------------------------------------------------------

    /** A2DP devices that are connected right now. Does blocking IPC: call off the main thread. */
    fun getConnectedDevices(): List<BluetoothDevice> {
        if (!hasBluetoothPermission() || bluetoothAdapter == null) return emptyList()
        ensureProfile()
        val profile = a2dpProfile ?: return emptyList()
        return try {
            profile.connectedDevices.map { createDevice(profile, it) }
        } catch (e: SecurityException) {
            emptyList()
        }
    }

    private fun createDevice(
        profile: BluetoothA2dp,
        device: android.bluetooth.BluetoothDevice
    ): BluetoothDevice {
        val name = try {
            device.name
        } catch (e: SecurityException) {
            null
        }
        return BluetoothDevice(
            name = name ?: "Unknown Device",
            address = device.address,
            isConnected = true,
            activeCodec = getCurrentCodec(profile, device),
            supportedCodecs = getDeviceSupportedCodecs(name),
            batteryLevel = getBatteryLevel(device),
            // Only available when the device also advertises over BLE with the same address.
            signalStrength = nearby[device.address]?.rssi
        )
    }

    /** Looked up from the bundled device database by name; this is not negotiated data. */
    private fun getDeviceSupportedCodecs(deviceName: String?): List<String> {
        val name = deviceName?.lowercase().orEmpty()
        if (name.isEmpty()) return listOf(BluetoothCodecs.SBC)
        return DeviceCodecDatabase.getSupportedCodecs(name)
            ?: DeviceCodecDatabase.getBrandCodecs(name)
    }

    /**
     * The codec in use, or null when it cannot be determined.
     *
     * Regular apps have no supported way to read this: `BluetoothA2dp.getCodecStatus`
     * is a hidden/system API that needs BLUETOOTH_PRIVILEGED on current Android, and
     * the audio HAL keys are vendor-specific. Both are tried on a best-effort basis.
     * There is deliberately no fallback to SBC and no guess from the device name.
     */
    private fun getCurrentCodec(
        profile: BluetoothA2dp,
        device: android.bluetooth.BluetoothDevice
    ): String? {
        return codecFromA2dpStatus(profile, device) ?: codecFromAudioParameters()
    }

    private fun codecFromA2dpStatus(
        profile: BluetoothA2dp,
        device: android.bluetooth.BluetoothDevice
    ): String? {
        return try {
            val status = try {
                BluetoothA2dp::class.java
                    .getMethod("getCodecStatus", android.bluetooth.BluetoothDevice::class.java)
                    .invoke(profile, device)
            } catch (e: NoSuchMethodException) {
                // Android 8.0 signature
                BluetoothA2dp::class.java.getMethod("getCodecStatus").invoke(profile)
            } ?: return null
            val config = status.javaClass.getMethod("getCodecConfig").invoke(status) ?: return null
            val type = config.javaClass.getMethod("getCodecType").invoke(config) as? Int ?: return null
            codecNameForType(type)
        } catch (e: Exception) {
            // Blocked by hidden-API restrictions or missing BLUETOOTH_PRIVILEGED.
            null
        }
    }

    /** Maps BluetoothCodecConfig.SOURCE_CODEC_TYPE_* to a display name; null if unrecognised. */
    private fun codecNameForType(type: Int): String? = when (type) {
        0 -> BluetoothCodecs.SBC
        1 -> BluetoothCodecs.AAC
        2 -> BluetoothCodecs.APTX
        3 -> BluetoothCodecs.APTX_HD
        4 -> BluetoothCodecs.LDAC
        // AOSP assigns 5 to LC3 from Android 13. Earlier Qualcomm builds used 5 for
        // aptX Adaptive. TODO: verify against real devices.
        5 -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) BluetoothCodecs.LC3
        else BluetoothCodecs.APTX_ADAPTIVE
        else -> null
    }

    @Suppress("DEPRECATION")
    private fun codecFromAudioParameters(): String? {
        if (!audioManager.isBluetoothA2dpOn) return null
        for (key in AUDIO_PARAMETER_KEYS) {
            val raw = try {
                audioManager.getParameters(key)
            } catch (e: Exception) {
                null
            } ?: continue
            // HALs answer "key=value"; an unsupported key comes back empty or as "key=".
            val value = raw.substringAfter('=', raw).trim().trimEnd(';').lowercase()
            codecNameForParameter(value)?.let { return it }
        }
        return null
    }

    /** Only explicit codec names are accepted; bare numbers are too ambiguous across vendors. */
    private fun codecNameForParameter(value: String): String? =
        when (value.replace('-', '_').replace(' ', '_')) {
            "sbc" -> BluetoothCodecs.SBC
            "aac" -> BluetoothCodecs.AAC
            "aptx" -> BluetoothCodecs.APTX
            "aptx_hd", "aptxhd" -> BluetoothCodecs.APTX_HD
            "aptx_adaptive", "aptxadaptive" -> BluetoothCodecs.APTX_ADAPTIVE
            "ldac" -> BluetoothCodecs.LDAC
            "lc3" -> BluetoothCodecs.LC3
            else -> null
        }

    /** Hidden `BluetoothDevice.getBatteryLevel()`; null when unavailable or not reported. */
    private fun getBatteryLevel(device: android.bluetooth.BluetoothDevice): Int? {
        return try {
            val level = android.bluetooth.BluetoothDevice::class.java
                .getMethod("getBatteryLevel")
                .invoke(device) as? Int
            level?.takeIf { it in 0..100 }
        } catch (e: Exception) {
            null
        }
    }

    // ---------------------------------------------------------------------
    // Nearby BLE devices
    // ---------------------------------------------------------------------

    private data class NearbyEntry(val name: String?, val rssi: Int, val lastSeenMs: Long)

    private val nearby = ConcurrentHashMap<String, NearbyEntry>()
    @Volatile private var isScanning = false
    @Volatile private var scanFailed = false

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device ?: return
            val address = device.address ?: return
            val name = nearby[address]?.name
                ?: result.scanRecord?.deviceName
                ?: try {
                    device.name
                } catch (e: SecurityException) {
                    null
                }
            nearby[address] = NearbyEntry(name, result.rssi, SystemClock.elapsedRealtime())
        }

        override fun onScanFailed(errorCode: Int) {
            Log.w(TAG, "BLE scan failed: $errorCode")
            isScanning = false
            // Do not retry in a loop: Android throttles apps that restart scans too often.
            // The flag is cleared by the next stopBluetoothScan().
            scanFailed = true
        }
    }

    /**
     * Starts one continuous BLE scan. Safe to call repeatedly: it is a no-op while a
     * scan is running or after a failure. Pair with [stopBluetoothScan] when the UI
     * goes to the background.
     */
    @Synchronized
    fun startBluetoothScan() {
        if (isScanning || scanFailed || !hasBluetoothPermission()) return
        val scanner = bluetoothAdapter?.bluetoothLeScanner ?: return
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        try {
            scanner.startScan(null, settings, scanCallback)
            isScanning = true
        } catch (e: SecurityException) {
            Log.w(TAG, "BLE scan not permitted", e)
        } catch (e: IllegalStateException) {
            // Bluetooth was turned off between the check and the call.
        }
    }

    @Synchronized
    fun stopBluetoothScan() {
        scanFailed = false
        if (!isScanning) return
        isScanning = false
        try {
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        } catch (e: SecurityException) {
            // Permission was revoked; the system has already stopped the scan.
        } catch (e: IllegalStateException) {
            // Adapter is off; the scan is gone either way.
        }
    }

    private fun pruneStaleNearby() {
        val cutoff = SystemClock.elapsedRealtime() - NEARBY_TTL_MS
        nearby.entries.removeAll { it.value.lastSeenMs < cutoff }
    }

    fun getNearbyDeviceCount(): Int {
        pruneStaleNearby()
        return nearby.size
    }

    /** (name, address, RSSI in dBm) for devices heard in the last [NEARBY_TTL_MS], strongest first. */
    fun getNearbyDevicesWithRssi(): List<Triple<String, String, Int>> {
        pruneStaleNearby()
        return nearby.entries
            .map { (address, entry) -> Triple(entry.name ?: "Unknown device", address, entry.rssi) }
            .sortedByDescending { it.third }
    }

    private companion object {
        const val TAG = "BluetoothCodecManager"
        const val NEARBY_TTL_MS = 30_000L

        val AUDIO_PARAMETER_KEYS = listOf(
            "bt_a2dp_active_codec",
            "bt_active_codec_type",
            "bluetooth_active_codec",
            "a2dp_current_codec",
            "bt_a2dp_codec_type",
            "bluetooth_a2dp_codec",
            "a2dp_codec_type",
            "bt_codec_type"
        )
    }
}
