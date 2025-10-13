package com.bluetoothcodec.checker

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            // Permissions granted, refresh data
        } else {
            // Show settings dialog
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainScreen { requestPermissions() }
            }
        }
    }
    
    private fun requestPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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
        permissionLauncher.launch(permissions)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onRequestPermissions: () -> Unit = {}) {
    val context = LocalContext.current
    val viewModel: MainViewModel = viewModel { MainViewModel(context) }
    val hasPermission by viewModel.hasPermission
    val devices by viewModel.devices
    val chipsetInfo by viewModel.chipsetInfo
    val isSearching by viewModel.isSearching
    
    LaunchedEffect(Unit) {
        viewModel.refreshData()
        // Auto-refresh every 5 seconds
        while (true) {
            kotlinx.coroutines.delay(5000)
            viewModel.refreshData()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (!hasPermission) {
            PermissionCard(onRequestPermissions)
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Chipset Info
                chipsetInfo?.let { info ->
                    item {
                        ChipsetCard(info, devices)
                    }
                }

                item {
                    BluetoothCongestionCard(devices)
                }
                
                // Version info at bottom
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "v1.6",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionCard(onRequestPermissions: () -> Unit) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Bluetooth Permission Required",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "This app needs Bluetooth and Location permissions to detect connected audio devices and their codec information.",
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onRequestPermissions) {
                    Text("Grant Permissions")
                }
                
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text("Settings")
                }
            }
        }
    }
}

@Composable
fun ChipsetCard(chipsetInfo: ChipsetInfo, devices: List<BluetoothDevice>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Device Information",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Device Model
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Model:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${chipsetInfo.manufacturer} ${chipsetInfo.model}",
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // OS Version
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "OS:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${chipsetInfo.osVersion} (${chipsetInfo.androidVersion})",
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // Chipset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Chipset:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = chipsetInfo.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Supported Codecs:",
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            CodecChart(chipsetInfo.supportedCodecs, devices)
        }
    }
}

@Composable
fun ConnectedDeviceInfo(device: BluetoothDevice) {
    val context = LocalContext.current
    val audioManager = context.getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager
    
    var isStreaming by remember { mutableStateOf(false) }
    var currentBitrate by remember { mutableStateOf("Unknown") }
    var sampleRate by remember { mutableStateOf(0) }
    
    LaunchedEffect(Unit) {
        while (true) {
            try {
                val streaming = audioManager.isBluetoothA2dpOn && audioManager.isMusicActive
                val systemRate = audioManager.getProperty(android.media.AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)?.toIntOrNull() ?: 44100
                
                isStreaming = streaming
                
                if (streaming && device.activeCodec != null) {
                    // Try to get actual codec parameters instead of system parameters
                    val actualParams = getActualCodecParameters(device.activeCodec!!)
                    sampleRate = actualParams.first
                    currentBitrate = actualParams.second
                } else {
                    sampleRate = systemRate
                    currentBitrate = "Not Streaming"
                }
            } catch (e: Exception) {
                currentBitrate = "Unknown"
            }
            kotlinx.coroutines.delay(1000)
        }
    }
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isStreaming) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
                           else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = device.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    device.activeCodec?.let { codec ->
                        Text(
                            text = codec,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = if (isStreaming) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                        )
                        Text(
                            text = if (isStreaming) "Streaming" else "Connected",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    if (isStreaming) {
                        Text(
                            text = currentBitrate,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${sampleRate / 1000}kHz",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

fun getActualCodecParameters(codec: String): Pair<Int, String> {
    // Return codec-specific typical parameters since Android doesn't expose actual BT codec params
    return when (codec) {
        "LDAC" -> {
            // LDAC can use 44.1, 48, 88.2, 96kHz - assume high quality streaming uses 96kHz
            val sampleRate = 96000
            val bitrate = "990 kbps"
            Pair(sampleRate, bitrate)
        }
        "aptX HD" -> {
            val sampleRate = 48000
            val bitrate = "576 kbps"
            Pair(sampleRate, bitrate)
        }
        "aptX Adaptive" -> {
            // Adaptive can vary, assume high quality mode
            val sampleRate = 96000
            val bitrate = "420 kbps"
            Pair(sampleRate, bitrate)
        }
        "aptX", "aptX LL" -> {
            val sampleRate = 48000
            val bitrate = "352 kbps"
            Pair(sampleRate, bitrate)
        }
        "AAC" -> {
            val sampleRate = 48000
            val bitrate = "320 kbps"
            Pair(sampleRate, bitrate)
        }
        "LC3" -> {
            val sampleRate = 48000
            val bitrate = "160 kbps"
            Pair(sampleRate, bitrate)
        }
        "SBC" -> {
            val sampleRate = 44100
            val bitrate = "328 kbps"
            Pair(sampleRate, bitrate)
        }
        else -> {
            val sampleRate = 44100
            val bitrate = "Unknown"
            Pair(sampleRate, bitrate)
        }
    }
}

fun calculateRealTimeBitrate(codec: String, sampleRate: Int): String {
    return when (codec) {
        "LDAC" -> when {
            sampleRate >= 96000 -> "990 kbps"
            sampleRate >= 48000 -> "660 kbps"
            else -> "330 kbps"
        }
        "aptX HD" -> "576 kbps"
        "aptX Adaptive" -> when {
            sampleRate >= 96000 -> "420 kbps"
            else -> "279 kbps"
        }
        "aptX", "aptX LL" -> "352 kbps"
        "AAC" -> when {
            sampleRate >= 48000 -> "320 kbps"
            else -> "256 kbps"
        }
        "LC3" -> "160 kbps"
        "SBC" -> "328 kbps"
        else -> "Unknown"
    }
}

@Composable
fun CodecChart(supportedCodecs: List<String>, connectedDevices: List<com.bluetoothcodec.checker.BluetoothDevice> = emptyList()) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val isTablet = screenWidth > 600.dp
    
    val chartHeight = if (isTablet) 400.dp else 280.dp
    
    // Real-time streaming info
    val context = LocalContext.current
    val audioManager = context.getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager
    
    var isStreaming by remember { mutableStateOf(false) }
    var streamingSampleRate by remember { mutableStateOf(0) }
    var activeStreamingCodec by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        while (true) {
            try {
                val streaming = audioManager.isBluetoothA2dpOn && audioManager.isMusicActive
                val activeCodec = if (streaming) {
                    connectedDevices.firstOrNull()?.activeCodec
                } else null
                
                isStreaming = streaming
                activeStreamingCodec = activeCodec
                
                if (streaming && activeCodec != null) {
                    // Get actual codec sample rate instead of system sample rate
                    val actualParams = getActualCodecParameters(activeCodec)
                    streamingSampleRate = actualParams.first
                } else {
                    streamingSampleRate = 44100
                }
            } catch (e: Exception) {
                isStreaming = false
            }
            kotlinx.coroutines.delay(1000)
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(chartHeight),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Latency (ms)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Real-time streaming info
                if (isStreaming && activeStreamingCodec != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = Color(0xFF4CAF50),
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                        )
                        Text(
                            text = "$activeStreamingCodec • ${streamingSampleRate / 1000}kHz",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Box(modifier = Modifier.fillMaxSize()) {
                // Y-axis values
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .height(if (isTablet) 280.dp else 180.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("20", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("40", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("80", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("130", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("200", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                // Chart area
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 35.dp, bottom = 35.dp)
                ) {
                    val availableWidth = maxWidth.value
                    val availableHeight = maxHeight.value
                    
                    // Grid
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val gridColor = Color.Gray.copy(alpha = 0.3f)
                        for (i in 1..4) {
                            val x = size.width * i / 4
                            val y = size.height * i / 4
                            drawLine(gridColor, androidx.compose.ui.geometry.Offset(x, 0f), androidx.compose.ui.geometry.Offset(x, size.height))
                            drawLine(gridColor, androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(size.width, y))
                        }
                    }
                    
                    // Position codecs based on quality vs latency with collision detection
                    val codecPositions = mutableMapOf<String, Pair<Float, Float>>()
                    val occupiedAreas = mutableListOf<Pair<Float, Float>>()
                    val cardSize = if (isTablet) 65f else 55f
                    
                    BluetoothCodecs.ALL_CODECS.forEach { codecName ->
                        BluetoothCodecs.CODEC_INFO[codecName]?.let { codecInfo ->
                            // Extract actual latency values from the data
                            val latencyValue = when(codecName) {
                                "LC3" -> 20f
                                "aptX LL" -> 32f
                                "SBC" -> 40f
                                "aptX" -> 40f
                                "aptX Adaptive" -> 65f // middle of 50-80ms range
                                "AAC" -> 80f
                                "aptX HD" -> 130f
                                "LDAC" -> 200f
                                else -> 40f
                            }
                            
                            // Quality ranking (0.0 to 1.0, higher = better quality)
                            val qualityRanking = when(codecName) {
                                "SBC" -> 0.1f
                                "LC3" -> 0.2f
                                "AAC" -> 0.4f
                                "aptX" -> 0.5f
                                "aptX LL" -> 0.5f
                                "aptX Adaptive" -> 0.7f
                                "aptX HD" -> 0.8f
                                "LDAC" -> 1.0f
                                else -> 0.5f
                            }
                            
                            // Normalize latency for Y positioning (lower latency = higher on chart)
                            val normalizedLatency = ((latencyValue - 20f) / (200f - 20f)).coerceIn(0f, 1f)
                            
                            var targetX = qualityRanking * (availableWidth - cardSize)
                            var targetY = normalizedLatency * (availableHeight - cardSize)
                            
                            // Collision detection and repositioning
                            val minDistance = cardSize + 5f
                            var attempts = 0
                            while (attempts < 20) {
                                val hasCollision = occupiedAreas.any { (existingX, existingY) ->
                                    val distance = kotlin.math.sqrt((targetX - existingX) * (targetX - existingX) + 
                                                                  (targetY - existingY) * (targetY - existingY))
                                    distance < minDistance
                                }
                                
                                if (!hasCollision) break
                                
                                // Spiral repositioning around original position
                                val angle = attempts * 0.6f
                                val radius = (attempts + 1) * 8f
                                val baseX = qualityRanking * (availableWidth - cardSize)
                                val baseY = normalizedLatency * (availableHeight - cardSize)
                                targetX = baseX + radius * kotlin.math.cos(angle).toFloat()
                                targetY = baseY + radius * kotlin.math.sin(angle).toFloat()
                                
                                // Ensure within bounds
                                targetX = targetX.coerceIn(0f, availableWidth - cardSize)
                                targetY = targetY.coerceIn(0f, availableHeight - cardSize)
                                attempts++
                            }
                            
                            occupiedAreas.add(Pair(targetX, targetY))
                            codecPositions[codecName] = Pair(targetX, targetY)
                        }
                    }
                    
                    // Render codecs at calculated positions
                    codecPositions.forEach { (codecName, position) ->
                        BluetoothCodecs.CODEC_INFO[codecName]?.let { _ ->
                            // Determine codec status
                            val activeDevice = connectedDevices.firstOrNull { it.activeCodec == codecName && it.isConnected }
                            val isActivelyStreaming = activeDevice != null
                            val isSupported = supportedCodecs.contains(codecName)
                            
                            Box(
                                modifier = Modifier.offset(
                                    x = position.first.dp,
                                    y = position.second.dp
                                )
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when {
                                        isActivelyStreaming -> MaterialTheme.colorScheme.primary
                                        isSupported -> MaterialTheme.colorScheme.secondary
                                        else -> Color.Gray.copy(alpha = 0.4f)
                                    },
                                    modifier = Modifier
                                        .size(if (isTablet) 65.dp else 55.dp)
                                        .padding(2.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(6.dp)
                                    ) {
                                        Text(
                                            text = codecName,
                                            fontSize = if (isTablet) 10.sp else 8.sp,
                                            color = if (isActivelyStreaming || isSupported) Color.White else Color.Gray,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            lineHeight = if (isTablet) 11.sp else 9.sp,
                                            maxLines = 2
                                        )
                                        
                                        // Hi-Res indicator
                                        val isHiRes = isHiResCodec(BluetoothCodecs.CODEC_INFO[codecName]?.sampleRate ?: "")
                                        if (isHiRes) {
                                            Text(
                                                text = "Hi-Res",
                                                fontSize = if (isTablet) 7.sp else 6.sp,
                                                color = Color(0xFFFFD700),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        
                                        // Active streaming indicator with sample rate
                                        if (isActivelyStreaming && isStreaming && codecName == activeStreamingCodec) {
                                            Text(
                                                text = "●",
                                                fontSize = if (isTablet) 12.sp else 10.sp,
                                                color = Color.Green,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${streamingSampleRate / 1000}k",
                                                fontSize = if (isTablet) 6.sp else 5.sp,
                                                color = Color.Green,
                                                fontWeight = FontWeight.Bold
                                            )
                                        } else if (isActivelyStreaming) {
                                            Text(
                                                text = "●",
                                                fontSize = if (isTablet) 12.sp else 10.sp,
                                                color = Color.Green,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // X-axis labels - Quality levels
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 35.dp, end = 20.dp, bottom = 20.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Basic", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Good", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("High", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Premium", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                Text(
                    text = "Audio Quality",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 4.dp)
                )
            }
        }
    }
}

@Composable
fun CodecDetailsCard(codecInfo: CodecInfo) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Bitrate:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text(codecInfo.bitrate, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Latency:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text(codecInfo.latency, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = codecInfo.description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

fun isHiResCodec(sampleRate: String): Boolean {
    return when {
        sampleRate.contains("96") -> true
        sampleRate.contains("88.2") -> true
        sampleRate.contains("192") -> true
        sampleRate.contains("48-96") -> true
        else -> false
    }
}

fun getSampleRateQuality(sampleRate: String): String {
    return when {
        sampleRate.contains("192") -> "Studio Master"
        sampleRate.contains("96") -> "Hi-Res Audio"
        sampleRate.contains("88.2") -> "Hi-Res Audio"
        sampleRate.contains("48") -> "DVD Quality"
        sampleRate.contains("44.1") -> "CD Quality"
        sampleRate.contains("32") -> "FM Radio"
        sampleRate.contains("22") -> "AM Radio"
        sampleRate.contains("16") -> "Phone Quality"
        sampleRate.contains("8") -> "Voice Call"
        else -> "Standard"
    }
}

fun isCodecActivelyStreaming(codecName: String, devices: List<com.bluetoothcodec.checker.BluetoothDevice>): Boolean {
    // Only one codec can be actively streaming at a time across all devices
    // Find the device that is currently streaming (first connected device with active codec)
    val streamingDevice = devices.firstOrNull { device -> 
        device.isConnected && device.activeCodec != null
    }
    
    return streamingDevice?.activeCodec == codecName
}

fun getSignalDirection(deviceAddress: String): String {
    // This would ideally come from the BluetoothManager
    // For now, return a placeholder
    return "Stable"
}

fun estimateLocationFromRSSI(rssi: Int): Pair<String, String> {
    val distance = when {
        rssi >= -30 -> "0.1-0.3"  // Very close - touching/holding
        rssi >= -40 -> "0.3-0.5"  // Extremely close - same desk
        rssi >= -50 -> "0.5-1"    // Very close - same room close
        rssi >= -60 -> "1-2"      // Close - same room
        rssi >= -70 -> "2-4"      // Near - adjacent room
        rssi >= -80 -> "4-8"      // Moderate - different room
        else -> "8+"              // Far - different floor
    }
    
    val location = when {
        rssi >= -30 -> "Right Next To You"
        rssi >= -40 -> "On Your Desk"
        rssi >= -50 -> "Very Close (같은 책상)"
        rssi >= -60 -> "Same Room (같은 방)"
        rssi >= -70 -> "Nearby Room (인근 방)"
        rssi >= -80 -> "Different Room (다른 방)"
        else -> "Far Away (멀리)"
    }
    
    return Pair(distance, location)
}

fun getSignalColor(rssi: Int): androidx.compose.ui.graphics.Color {
    return when {
        rssi >= -50 -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Green
        rssi >= -60 -> androidx.compose.ui.graphics.Color(0xFF8BC34A) // Light Green
        rssi >= -70 -> androidx.compose.ui.graphics.Color(0xFFFF9800) // Orange
        else -> androidx.compose.ui.graphics.Color(0xFFF44336) // Red
    }
}

fun getDeviceTypeInfo(deviceName: String?): String {
    val name = deviceName?.lowercase() ?: return "Unknown Device Type"
    
    return when {
        // Jabra devices
        name.contains("jabra") -> {
            when {
                name.contains("elite 4") -> "True Wireless Earbuds • Sports"
                name.contains("elite 3") -> "True Wireless Earbuds • Basic"
                name.contains("elite 85") -> "Wireless Headphones • Premium"
                name.contains("elite 75") -> "True Wireless Earbuds • Premium"
                name.contains("elite 65") -> "True Wireless Earbuds • Business"
                name.contains("evolve") -> "Business Headset • UC Certified"
                else -> "Jabra Audio Device"
            }
        }
        
        // Sony devices
        name.contains("sony") || name.contains("wh-") || name.contains("wf-") -> {
            when {
                name.contains("wh-1000xm") -> "Wireless Headphones • Noise Canceling"
                name.contains("wf-1000xm") -> "True Wireless Earbuds • Noise Canceling"
                name.contains("wh-ch") -> "Wireless Headphones • Entry Level"
                name.contains("wi-") -> "Wireless Neckband Earphones"
                else -> "Sony Audio Device"
            }
        }
        
        // Apple devices
        name.contains("airpods") -> {
            when {
                name.contains("pro") && (name.contains("3") || name.contains("usb-c")) -> "AirPods Pro (3rd Gen) • USB-C • Spatial Audio"
                name.contains("pro") && name.contains("2") -> "AirPods Pro (2nd Gen) • Spatial Audio"
                name.contains("pro") -> "AirPods Pro • Noise Canceling"
                name.contains("max") -> "AirPods Max • Over-Ear Premium"
                name.contains("4") -> "AirPods (4th Gen) • Latest"
                name.contains("3") -> "AirPods (3rd Gen) • Spatial Audio"
                name.contains("2") -> "AirPods (2nd Gen) • Classic"
                else -> "AirPods • True Wireless"
            }
        }
        
        // Beats devices (Apple-owned)
        name.contains("beats") -> {
            when {
                name.contains("studio3") || name.contains("studio 3") -> "Beats Studio3 • Noise Canceling"
                name.contains("studio buds +") || name.contains("studio buds plus") -> "Beats Studio Buds+ • True Wireless Pro"
                name.contains("studio buds") -> "Beats Studio Buds • True Wireless"
                name.contains("solo4") || name.contains("solo 4") -> "Beats Solo4 • Latest On-Ear"
                name.contains("solo3") || name.contains("solo 3") -> "Beats Solo3 • On-Ear Wireless"
                name.contains("solo pro") -> "Beats Solo Pro • Noise Canceling"
                name.contains("fit pro") -> "Beats Fit Pro • Sports True Wireless"
                name.contains("powerbeats pro") -> "Beats Powerbeats Pro • Sports True Wireless"
                name.contains("powerbeats3") -> "Beats Powerbeats3 • Sports Wireless"
                name.contains("beatsx") || name.contains("beats x") -> "BeatsX • Neckband Wireless"
                else -> "Beats Audio Device • Apple"
            }
        }
        
        // Bose devices
        name.contains("bose") -> {
            when {
                name.contains("quietcomfort") -> "Wireless Headphones • Noise Canceling"
                name.contains("soundlink") -> "Portable Speaker"
                name.contains("sport") -> "Sports Earphones"
                else -> "Bose Audio Device"
            }
        }
        
        // Sennheiser devices
        name.contains("sennheiser") -> {
            when {
                name.contains("momentum") -> "Wireless Headphones • Audiophile"
                name.contains("hd") -> "Wired Headphones • Professional"
                else -> "Sennheiser Audio Device"
            }
        }
        
        // Samsung devices
        name.contains("galaxy buds") -> "True Wireless Earbuds • Samsung"
        
        // Generic classifications
        name.contains("headphone") || name.contains("headset") -> "Wireless Headphones"
        name.contains("earbuds") || name.contains("earphone") -> "Wireless Earbuds"
        name.contains("speaker") -> "Bluetooth Speaker"
        
        else -> "Bluetooth Audio Device"
    }
}

fun getBatteryIcon(battery: Int): String {
    return when {
        battery >= 75 -> "████"
        battery >= 50 -> "███░"
        battery >= 25 -> "██░░"
        else -> "█░░░"
    }
}

fun getSignalIcon(rssi: Int): String {
    return when {
        rssi >= -50 -> "●●●●"
        rssi >= -60 -> "●●●○"
        rssi >= -70 -> "●●○○"
        else -> "●○○○"
    }
}

fun getManufacturerName(deviceName: String?): String {
    val name = deviceName?.lowercase() ?: return "Unknown"
    
    return when {
        name.contains("airpods") || name.contains("beats") -> "Apple"
        name.contains("jabra") -> "Jabra"
        name.contains("sony") || name.contains("wh-") || name.contains("wf-") -> "Sony"
        name.contains("bose") -> "Bose"
        name.contains("sennheiser") -> "Sennheiser"
        name.contains("galaxy buds") || name.contains("samsung") -> "Samsung"
        name.contains("jbl") -> "JBL"
        name.contains("skullcandy") -> "Skullcandy"
        name.contains("audio-technica") -> "Audio-Technica"
        name.contains("plantronics") || name.contains("poly") -> "Plantronics"
        name.contains("anker") || name.contains("soundcore") -> "Anker"
        name.contains("marshall") -> "Marshall"
        name.contains("bang & olufsen") || name.contains("b&o") -> "Bang & Olufsen"
        name.contains("focal") -> "Focal"
        name.contains("beyerdynamic") -> "Beyerdynamic"
        name.contains("akg") -> "AKG"
        name.contains("shure") -> "Shure"
        name.contains("grado") -> "Grado"
        name.contains("audeze") -> "Audeze"
        name.contains("hifiman") -> "HiFiMAN"
        name.contains("oppo") -> "Oppo"
        name.contains("oneplus") -> "OnePlus"
        name.contains("xiaomi") || name.contains("redmi") -> "Xiaomi"
        name.contains("huawei") -> "Huawei"
        name.contains("lg") -> "LG"
        name.contains("motorola") -> "Motorola"
        name.contains("nothing") -> "Nothing"
        else -> "Unknown"
    }
}

@Composable
fun BluetoothCongestionCard(devices: List<com.bluetoothcodec.checker.BluetoothDevice>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Bluetooth Signal Analysis",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Connected devices info
            if (devices.isNotEmpty()) {
                Text(
                    text = "Connected Devices (${devices.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                devices.forEach { device ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color.Green, androidx.compose.foundation.shape.CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = device.name ?: "Unknown Device",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Only show codec if it's not unknown
                            val codec = device.activeCodec ?: ""
                            android.util.Log.d("MainActivity", "Device: ${device.name}, activeCodec: '${device.activeCodec}'")
                            
                            if (codec.isNotEmpty() && codec != "Unknown") {
                                Text(
                                    text = codec,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            } else {
                                // Show "Detecting..." when codec is unknown
                                Text(
                                    text = "Detecting...",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                            
                            // Signal strength indicator
                            device.signalStrength?.let { rssi ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    repeat(4) { index ->
                                        val threshold = -90 + (index * 15) // -90, -75, -60, -45 dBm thresholds
                                        Box(
                                            modifier = Modifier
                                                .width(3.dp)
                                                .height((6 + index * 2).dp)
                                                .background(
                                                    if (rssi > threshold) Color.Green else Color.Gray.copy(alpha = 0.3f),
                                                    androidx.compose.foundation.shape.RoundedCornerShape(1.dp)
                                                )
                                        )
                                        if (index < 3) Spacer(modifier = Modifier.width(1.dp))
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${rssi}dBm",
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            } ?: run {
                                // Show estimated signal strength when RSSI is unavailable
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    repeat(4) { index ->
                                        // Show 3/4 bars for connected device (good signal)
                                        Box(
                                            modifier = Modifier
                                                .width(3.dp)
                                                .height((6 + index * 2).dp)
                                                .background(
                                                    if (index < 3) Color.Green else Color.Gray.copy(alpha = 0.3f),
                                                    androidx.compose.foundation.shape.RoundedCornerShape(1.dp)
                                                )
                                        )
                                        if (index < 3) Spacer(modifier = Modifier.width(1.dp))
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "-60dBm",
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            
                            // Battery indicator
                            device.batteryLevel?.let { battery ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .width(16.dp)
                                            .height(8.dp)
                                            .background(
                                                Color.Gray.copy(alpha = 0.3f),
                                                androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                                            )
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(battery / 100f)
                                                .background(
                                                    when {
                                                        battery > 50 -> Color.Green
                                                        battery > 20 -> Color(0xFFFF9800)
                                                        else -> Color.Red
                                                    },
                                                    androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                                                )
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${battery}%",
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Environmental signal detection
            val nearbySignals = estimateNearbySignals(devices)
            val environmentType = detectEnvironmentType(devices, nearbySignals)
            val otherSignals = maxOf(0, nearbySignals - devices.size)
            val realNearbyDevices = getRealNearbyDevices()
            val angleStep = 360f / maxOf(nearbySignals, 8)
            
            // Radiation chart for signal interference
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerX = size.width / 2
                    val centerY = size.height / 2
                    val maxRadius = minOf(centerX, centerY) - 20f
                    
                    // Draw concentric circles for interference risk zones
                    val zones = listOf(
                        Pair(maxRadius * 0.3f, Color.Red.copy(alpha = 0.3f)),      // High interference (close devices)
                        Pair(maxRadius * 0.6f, Color(0xFFFF9800).copy(alpha = 0.3f)), // Medium interference
                        Pair(maxRadius * 0.9f, Color(0xFFFFEB3B).copy(alpha = 0.3f)), // Low interference (far devices)
                        Pair(maxRadius, Color.Green.copy(alpha = 0.2f))             // Safe zone
                    )
                    
                    zones.forEach { (radius, color) ->
                        drawCircle(
                            color = color,
                            radius = radius,
                            center = androidx.compose.ui.geometry.Offset(centerX, centerY)
                        )
                    }
                    
                    // Draw signal sources as dots around the chart
                    
                    // First, draw connected Bluetooth devices with their signal strength circles
                    devices.forEachIndexed { index, device ->
                        val angle = Math.toRadians((index * 60.0)) // Spread connected devices
                        val distance = maxRadius * 0.7f // Fixed distance for connected devices
                        
                        val x = centerX + (distance * Math.cos(angle)).toFloat()
                        val y = centerY + (distance * Math.sin(angle)).toFloat()
                        
                        // Draw signal strength circle for each device
                        val signalStrength = (device.signalStrength ?: 50).coerceIn(0, 100) // Ensure valid range
                        val signalRadius = (signalStrength / 100f) * 40f + 20f // 20-60px radius based on signal
                        val signalAlpha = ((signalStrength / 100f) * 0.3f + 0.1f).coerceIn(0.1f, 0.4f) // Ensure valid alpha range
                        
                        // Signal strength circle
                        drawCircle(
                            color = Color.Green.copy(alpha = signalAlpha),
                            radius = signalRadius,
                            center = androidx.compose.ui.geometry.Offset(x, y)
                        )
                        
                        // Device dot - green with border
                        drawCircle(
                            color = Color.Green,
                            radius = 10f,
                            center = androidx.compose.ui.geometry.Offset(x, y)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 6f,
                            center = androidx.compose.ui.geometry.Offset(x, y)
                        )
                    }
                    
                    // Draw real nearby devices (not connected) with different signal strengths
                    realNearbyDevices.forEachIndexed { index, (name, address, rssi) ->
                        val angle = Math.toRadians(((index + devices.size) * angleStep).toDouble())
                        // Distance based on RSSI strength (-40 = close, -80 = far)
                        val signalStrength = (rssi + 80) / 40f // Convert RSSI to 0-1 range
                        val distance = maxRadius * (0.3f + signalStrength * 0.5f)
                        
                        val x = centerX + (distance * Math.cos(angle)).toFloat()
                        val y = centerY + (distance * Math.sin(angle)).toFloat()
                        
                        // Color based on interference potential (proximity)
                        val signalColor = when {
                            rssi > -50 -> Color.Red         // Close device = High interference risk
                            rssi > -65 -> Color(0xFFFF9800) // Medium distance = Medium interference
                            else -> Color(0xFFFFEB3B)       // Far device = Low interference risk
                        }
                        
                        drawCircle(
                            color = signalColor,
                            radius = 5f,
                            center = androidx.compose.ui.geometry.Offset(x, y)
                        )
                    }
                    
                    // Draw center point (your device)
                    drawCircle(
                        color = Color.Blue,
                        radius = 8f,
                        center = androidx.compose.ui.geometry.Offset(centerX, centerY)
                    )
                }
                
                // Device name labels overlay
                devices.forEachIndexed { index, device ->
                    val angle = Math.toRadians((index * 60.0))
                    val distance = (200.dp.value * 0.35f) // Adjust for Box size
                    
                    val offsetX = (distance * Math.cos(angle)).dp
                    val offsetY = (distance * Math.sin(angle)).dp
                    
                    Text(
                        text = device.name ?: "Unknown",
                        fontSize = 8.sp,
                        color = Color.Black,
                        modifier = Modifier
                            .offset(x = offsetX, y = offsetY + 15.dp)
                            .background(
                                Color.White.copy(alpha = 0.8f),
                                androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                            )
                            .padding(horizontal = 2.dp)
                    )
                }
                
                // Legend overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.Blue, androidx.compose.foundation.shape.CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Your Phone", fontSize = 8.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.Green, androidx.compose.foundation.shape.CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Connected Device", fontSize = 8.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color.Red, androidx.compose.foundation.shape.CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("High Risk", fontSize = 8.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFFFF9800), androidx.compose.foundation.shape.CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Medium Risk", fontSize = 8.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFFFFEB3B), androidx.compose.foundation.shape.CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Low Risk", fontSize = 8.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Environment analysis
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Environment",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = environmentType,
                        fontSize = 11.sp,
                        color = getEnvironmentColor(environmentType)
                    )
                }
                
                Column {
                    Text(
                        text = "Interference Level",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    val interferenceLevel = getInterferenceLevel(nearbySignals)
                    Text(
                        text = interferenceLevel,
                        fontSize = 11.sp,
                        color = getInterferenceLevelColor(interferenceLevel)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Top interfering signals
            Text(
                text = "Major Interference Sources",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                realNearbyDevices.take(4).forEach { (name, address, rssi) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = name,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "${rssi}dBm",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .background(
                                        when {
                                            rssi > -50 -> Color.Red         // Close = High interference risk
                                            rssi > -65 -> Color(0xFFFF9800) // Medium = Medium interference
                                            else -> Color(0xFFFFEB3B)       // Far = Low interference risk
                                        },
                                        androidx.compose.foundation.shape.CircleShape
                                    )
                            )
                        }
                    }
                }
                
                if (realNearbyDevices.isEmpty()) {
                    Text(
                        text = "No nearby devices detected",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Environmental tips
            Text(
                text = getEnvironmentTips(environmentType, nearbySignals),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}

fun getChannelCongestion(channel: Int, devices: List<com.bluetoothcodec.checker.BluetoothDevice>, nearbySignals: Int = 0): Float {
    // Enhanced congestion calculation including nearby interfering signals
    val deviceCount = devices.size
    val totalSignals = deviceCount + nearbySignals
    val avgSignal = devices.mapNotNull { it.signalStrength }.average().takeIf { !it.isNaN() } ?: -70.0
    
    // Base congestion from signal density
    val baseCongestion = when (channel) {
        1, 6, 11 -> (totalSignals * 0.15f + ((-avgSignal.toFloat() - 30) / 100f)).coerceIn(0f, 1f) // Common WiFi channels
        2, 3, 4, 5, 7, 8, 9, 10, 12 -> (totalSignals * 0.12f + ((-avgSignal.toFloat() - 40) / 120f)).coerceIn(0f, 1f)
        13, 14 -> (totalSignals * 0.08f + ((-avgSignal.toFloat() - 50) / 140f)).coerceIn(0f, 1f) // Less used
        else -> 0.1f
    }
    
    // Environmental multiplier based on nearby signals
    val environmentMultiplier = when {
        nearbySignals > 20 -> 1.4f // High density (metro, mall)
        nearbySignals > 10 -> 1.2f // Medium density (office, cafe)
        nearbySignals > 5 -> 1.1f  // Low density (home, park)
        else -> 1.0f               // Minimal interference
    }
    
    return (baseCongestion * environmentMultiplier).coerceIn(0f, 1f)
}

fun getWiFiInterference(devices: List<com.bluetoothcodec.checker.BluetoothDevice>, nearbySignals: Int = 0): String {
    val deviceCount = devices.size
    val totalSignals = deviceCount + nearbySignals
    val avgSignal = devices.mapNotNull { it.signalStrength }.average().takeIf { !it.isNaN() } ?: -70.0
    
    return when {
        totalSignals > 15 && avgSignal > -50.0 -> "Severe"
        totalSignals > 10 && avgSignal > -55.0 -> "High"
        totalSignals > 5 && avgSignal > -65.0 -> "Medium"
        totalSignals > 2 && avgSignal > -70.0 -> "Low"
        else -> "Minimal"
    }
}

@Composable
fun estimateNearbySignals(devices: List<com.bluetoothcodec.checker.BluetoothDevice>): Int {
    // Get real nearby device count from Bluetooth scanning
    val context = LocalContext.current
    val bluetoothManager = remember { BluetoothCodecManager(context) }
    
    // Start scanning for nearby devices
    LaunchedEffect(Unit) {
        bluetoothManager.startBluetoothScan()
    }
    
    // Return actual connected devices + scanned nearby devices
    return devices.size + bluetoothManager.getNearbyDeviceCount()
}

fun detectEnvironmentType(devices: List<com.bluetoothcodec.checker.BluetoothDevice>, nearbySignals: Int): String {
    val deviceCount = devices.size
    val avgSignal = devices.mapNotNull { it.signalStrength }.average().takeIf { !it.isNaN() } ?: -70.0
    
    return when {
        deviceCount == 0 -> "No Bluetooth devices detected"
        deviceCount == 1 && avgSignal > -50 -> "Single device - Good signal"
        deviceCount == 1 -> "Single device - Weak signal"
        deviceCount > 1 && avgSignal > -50 -> "Multiple devices - Good environment"
        deviceCount > 1 -> "Multiple devices - Interference possible"
        else -> "Unknown environment"
    }
}

fun getConnectionStabilityRisk(devices: List<com.bluetoothcodec.checker.BluetoothDevice>, nearbySignals: Int, environment: String): String {
    val totalSignals = devices.size + nearbySignals
    val avgSignal = devices.mapNotNull { it.signalStrength }.average().takeIf { !it.isNaN() } ?: -70.0
    
    return when {
        totalSignals > 20 || avgSignal > -40 -> "High Risk"
        totalSignals > 10 || avgSignal > -50 -> "Medium Risk"
        totalSignals > 5 || avgSignal > -60 -> "Low Risk"
        else -> "Stable"
    }
}

fun getEnvironmentColor(environment: String): Color {
    return when {
        environment.contains("Metro") || environment.contains("Mall") -> Color.Red
        environment.contains("Office") || environment.contains("Cafe") -> Color(0xFFFF9800)
        environment.contains("Residential") -> Color(0xFF4CAF50)
        else -> Color.Gray
    }
}

fun getStabilityColor(stability: String): Color {
    return when (stability) {
        "High Risk" -> Color.Red
        "Medium Risk" -> Color(0xFFFF9800)
        "Low Risk" -> Color(0xFFFFEB3B)
        "Stable" -> Color.Green
        else -> Color.Gray
    }
}

@Composable
fun getRealNearbyDevices(): List<Triple<String, String, Int>> {
    val context = LocalContext.current
    val bluetoothManager = remember { BluetoothCodecManager(context) }
    var nearbyDevices by remember { mutableStateOf(emptyList<Triple<String, String, Int>>()) }
    
    LaunchedEffect(Unit) {
        bluetoothManager.startBluetoothScan()
        // Update nearby devices list periodically
        while (true) {
            kotlinx.coroutines.delay(3000)
            nearbyDevices = bluetoothManager.getNearbyDevicesWithRssi()
        }
    }
    
    return nearbyDevices
}

fun getInterferenceLevel(nearbySignals: Int): String {
    return when {
        nearbySignals > 15 -> "Critical"
        nearbySignals > 10 -> "High"
        nearbySignals > 5 -> "Medium"
        nearbySignals > 2 -> "Low"
        else -> "Minimal"
    }
}

fun getInterferenceLevelColor(level: String): Color {
    return when (level) {
        "Critical" -> Color.Red
        "High" -> Color(0xFFFF5722)
        "Medium" -> Color(0xFFFF9800)
        "Low" -> Color(0xFFFFEB3B)
        "Minimal" -> Color.Green
        else -> Color.Gray
    }
}

fun getEnvironmentTips(environment: String, nearbySignals: Int): String {
    return when {
        environment.contains("Metro") -> "High interference area. Consider using wired headphones during peak hours."
        environment.contains("Mall") -> "Many competing signals. Move closer to device or use higher quality codec."
        environment.contains("Office") -> "WiFi networks may interfere. Try switching to 5GHz WiFi if available."
        nearbySignals > 10 -> "Dense signal environment detected. Audio dropouts may occur."
        nearbySignals > 5 -> "Moderate interference. Keep device within 1-2 meters for best performance."
        else -> "Good signal environment for stable Bluetooth connection."
    }
}

fun getSignalQuality(rssi: Int): String {
    return when {
        rssi >= -50 -> "Excellent"
        rssi >= -60 -> "Good"
        rssi >= -70 -> "Fair"
        else -> "Poor"
    }
}

fun guessCodecFromStream(sampleRate: Int, bufferSize: Int): Pair<String, Int> {
    return when {
        // Hi-Res indicators (96kHz+) - likely LDAC
        sampleRate >= 96000 -> Pair("LDAC", 85)
        
        // High quality 48kHz with small buffers - likely AAC or aptX HD
        sampleRate >= 48000 && bufferSize <= 256 -> Pair("AAC", 75)
        
        // Standard 48kHz - likely AAC
        sampleRate >= 48000 -> Pair("AAC", 70)
        
        // CD quality with large buffers - likely SBC
        sampleRate == 44100 && bufferSize > 512 -> Pair("SBC", 80)
        
        // CD quality with small buffers - could be aptX
        sampleRate == 44100 && bufferSize <= 256 -> Pair("aptX", 65)
        
        // Standard CD quality - likely SBC
        sampleRate == 44100 -> Pair("SBC", 75)
        
        // Lower quality - definitely SBC
        else -> Pair("SBC", 90)
    }
}
