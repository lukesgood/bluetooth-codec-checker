// Add this to MainActivity.kt to test all codec detection methods

fun testAllCodecMethods(device: android.bluetooth.BluetoothDevice) {
    val bluetoothManager = BluetoothManager(this)
    
    println("=== CODEC DETECTION TEST RESULTS ===")
    
    // Test each method individually
    val results = mutableMapOf<String, String?>()
    
    try {
        results["Active Stream"] = bluetoothManager.getActiveStreamCodec()
        results["Developer Options"] = bluetoothManager.getDeveloperOptionsCodec()
        results["OS Level"] = bluetoothManager.getOSLevelCodec(device)
        results["Audio Routing"] = bluetoothManager.getCodecFromAudioRouting()
        results["System Props"] = bluetoothManager.getCurrentCodecFromSystemProps()
        results["aptX Detection"] = bluetoothManager.detectAptXCodec(device)
        results["Media Metrics"] = bluetoothManager.detectCodecFromMediaMetrics(device)
        results["Bluetooth HCI"] = bluetoothManager.detectCodecFromBluetoothHci(device)
        results["Vendor Props"] = bluetoothManager.detectCodecFromVendorProperties(device)
        results["Audio Policy"] = bluetoothManager.detectCodecFromAudioPolicy(device)
        results["Bluetooth Stack"] = bluetoothManager.detectCodecFromBluetoothStack(device)
        results["Kernel Logs"] = bluetoothManager.detectCodecFromKernelLogs(device)
    } catch (e: Exception) {
        println("Error testing methods: ${e.message}")
    }
    
    // Print results
    results.forEach { (method, result) ->
        val status = if (result != null) "✓ $result" else "✗ null"
        println("$method: $status")
    }
    
    // Final result from main method
    val finalResult = bluetoothManager.getCurrentCodec(device)
    println("\nFinal Result: $finalResult")
    println("=====================================")
}
