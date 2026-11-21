package com.bluetoothcodec.checker

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val hasPermission = mutableStateOf(false)
    val bluetoothEnabled = mutableStateOf(true)
    val devices = mutableStateOf<List<BluetoothDevice>>(emptyList())
    val nearbyDevices = mutableStateOf<List<NearbyDevice>>(emptyList())
    val classicDiscovering = mutableStateOf(false)
    val chipsetInfo = mutableStateOf<ChipsetInfo?>(null)
    val isSearching = mutableStateOf(false)

    // The only BluetoothCodecManager in the process; it holds the A2DP proxy and the BLE scan.
    private val codecManager = BluetoothCodecManager(application)

    /**
     * Reads the current Bluetooth state. Call from the main thread; the blocking
     * work runs on Dispatchers.IO and results are published back on the caller.
     */
    suspend fun refresh() {
        val granted = codecManager.hasBluetoothPermission()
        val enabled = codecManager.isBluetoothEnabled()
        hasPermission.value = granted
        bluetoothEnabled.value = enabled

        if (!granted || !enabled) {
            codecManager.stopBluetoothScan()
            devices.value = emptyList()
            nearbyDevices.value = emptyList()
            classicDiscovering.value = false
            return
        }

        isSearching.value = true
        try {
            val (chipset, connected, nearby) = withContext(Dispatchers.IO) {
                codecManager.startBluetoothScan()
                Triple(
                    codecManager.getChipsetInfo(),
                    codecManager.getConnectedDevices(),
                    codecManager.getNearbyDevices()
                )
            }
            chipsetInfo.value = chipset
            devices.value = connected
            nearbyDevices.value = nearby
            classicDiscovering.value = codecManager.isClassicDiscovering()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            devices.value = emptyList()
        } finally {
            isSearching.value = false
        }
    }

    /**
     * Starts a ~12 s classic inquiry. Only call from an explicit user action: it can make
     * Bluetooth audio stutter while it runs.
     */
    fun startClassicDiscovery() {
        classicDiscovering.value = codecManager.startClassicDiscovery()
    }

    /** Call when the UI leaves the foreground. */
    fun stopScan() {
        codecManager.stopBluetoothScan()
        classicDiscovering.value = false
    }

    override fun onCleared() {
        codecManager.close()
    }
}
