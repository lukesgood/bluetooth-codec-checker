package com.bluetoothcodec.checker

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MainViewModel(private val context: Context) : ViewModel() {
    
    val hasPermission = mutableStateOf(false)
    val devices = mutableStateOf<List<com.bluetoothcodec.checker.BluetoothDevice>>(emptyList())
    val chipsetInfo = mutableStateOf<ChipsetInfo?>(null)
    val isSearching = mutableStateOf(false)
    
    private val systemBluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = systemBluetoothManager.adapter
    private val bluetoothCodecManager = BluetoothCodecManager(context)
    
    init {
        checkPermissions()
        chipsetInfo.value = bluetoothCodecManager.getChipsetInfo()
    }
    
    private fun checkPermissions(): Boolean {
        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
        
        val allGranted = requiredPermissions.all { permission ->
            ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
        
        hasPermission.value = allGranted && bluetoothAdapter?.isEnabled == true
        return hasPermission.value
    }
    
    fun refreshData() {
        if (!checkPermissions()) {
            return
        }
        
        isSearching.value = true
        
        viewModelScope.launch {
            try {
                // Use BluetoothCodecManager to get real connected devices
                val connectedDevices = bluetoothCodecManager.getConnectedDevices()
                devices.value = connectedDevices
                
                // Get chipset info
                chipsetInfo.value = bluetoothCodecManager.getChipsetInfo()
                
            } catch (e: Exception) {
                // Handle errors
                devices.value = emptyList()
            } finally {
                isSearching.value = false
            }
        }
    }
    
    private fun loadChipsetInfo() {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        val androidVersion = "API ${Build.VERSION.SDK_INT}"
        val osVersion = "Android ${Build.VERSION.RELEASE}"
        
        // Determine chipset and supported codecs
        val chipsetName = getChipsetName()
        val supportedCodecs = getSupportedCodecs(chipsetName)
        
        chipsetInfo.value = ChipsetInfo(
            manufacturer = manufacturer,
            model = model,
            name = chipsetName,
            androidVersion = androidVersion,
            osVersion = osVersion,
            supportedCodecs = supportedCodecs
        )
    }
    
    private fun getChipsetName(): String {
        val hardware = Build.HARDWARE.lowercase()
        val board = Build.BOARD.lowercase()
        val product = Build.PRODUCT.lowercase()
        
        return when {
            hardware.contains("qcom") || board.contains("msm") || product.contains("msm") -> 
                "Qualcomm Snapdragon"
            hardware.contains("exynos") || board.contains("exynos") -> 
                "Samsung Exynos"
            hardware.contains("kirin") || board.contains("kirin") -> 
                "HiSilicon Kirin"
            hardware.contains("mt") || board.contains("mt") -> 
                "MediaTek"
            hardware.contains("unisoc") || board.contains("unisoc") -> 
                "UNISOC"
            else -> "Unknown Chipset"
        }
    }
    
    private fun getSupportedCodecs(chipset: String): List<String> {
        return when {
            chipset.contains("Qualcomm") -> listOf("SBC", "AAC", "aptX", "aptX HD", "aptX LL", "aptX Adaptive", "LDAC")
            chipset.contains("Samsung") -> listOf("SBC", "AAC", "LDAC")
            chipset.contains("MediaTek") -> listOf("SBC", "AAC", "LDAC")
            else -> listOf("SBC", "AAC")
        }
    }
    
    private fun isAudioDevice(device: BluetoothDevice): Boolean {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        
        val deviceClass = device.bluetoothClass?.majorDeviceClass
        return deviceClass == android.bluetooth.BluetoothClass.Device.Major.AUDIO_VIDEO
    }
    
    private fun getBatteryLevel(device: BluetoothDevice): Int? {
        return try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                // This is a simplified implementation
                // Real implementation would use BluetoothDevice.getBatteryLevel() if available
                (60..95).random() // Mock battery level for demo
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    private fun getSignalStrength(device: BluetoothDevice): Int? {
        return try {
            // This is a simplified implementation
            // Real implementation would use connection info
            (-45..-75).random() // Mock RSSI for demo
        } catch (e: Exception) {
            null
        }
    }
    
    private fun getActiveCodec(device: BluetoothDevice): String? {
        return try {
            // This is a simplified implementation
            // Real implementation would query the actual codec
            listOf("SBC", "AAC", "aptX", "LDAC").random() // Mock codec for demo
        } catch (e: Exception) {
            "SBC" // Default fallback
        }
    }
}
