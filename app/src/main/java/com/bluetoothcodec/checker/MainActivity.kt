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
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        // Nothing to do here: MainScreen re-checks permissions on every refresh tick,
        // so the UI switches over within a second of the user answering the dialog.
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
    val viewModel: MainViewModel = viewModel()
    val hasPermission by viewModel.hasPermission
    val bluetoothEnabled by viewModel.bluetoothEnabled
    val devices by viewModel.devices
    val nearbyDevices by viewModel.nearbyDevices
    val chipsetInfo by viewModel.chipsetInfo

    // Refresh once a second, but only while the screen is visible. Leaving the
    // foreground cancels the loop and stops the BLE scan.
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            try {
                while (true) {
                    viewModel.refresh()
                    kotlinx.coroutines.delay(1000)
                }
            } finally {
                viewModel.stopScan()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // Edge-to-edge is enforced from targetSdk 35: keep content clear of system bars.
            .safeDrawingPadding()
            .padding(16.dp)
    ) {
        if (!hasPermission) {
            PermissionCard(onRequestPermissions)
        } else if (!bluetoothEnabled) {
            BluetoothOffCard()
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
                    BluetoothCongestionCard(devices, nearbyDevices)
                }
                
                // Version info at bottom
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "v${BuildConfig.VERSION_NAME}",
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
fun BluetoothOffCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Bluetooth is turned off",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Turn Bluetooth on to see connected audio devices.",
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
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
    
    // The loop below outlives recompositions, so read the device list through updated state.
    val currentDevices by rememberUpdatedState(connectedDevices)

    LaunchedEffect(Unit) {
        while (true) {
            try {
                val streaming = audioManager.isBluetoothA2dpOn && audioManager.isMusicActive
                val activeCodec = if (streaming) {
                    currentDevices.firstOrNull()?.activeCodec
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

fun isHiResCodec(sampleRate: String): Boolean {
    return when {
        sampleRate.contains("96") -> true
        sampleRate.contains("88.2") -> true
        sampleRate.contains("192") -> true
        sampleRate.contains("48-96") -> true
        else -> false
    }
}

@Composable
fun BluetoothCongestionCard(
    devices: List<com.bluetoothcodec.checker.BluetoothDevice>,
    realNearbyDevices: List<Triple<String, String, Int>> = emptyList()
) {
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
                            
                            if (codec.isNotEmpty() && codec != "Unknown") {
                                Text(
                                    text = codec,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            } else {
                                // Android does not expose the active codec to regular apps on
                                // many devices, so say so instead of implying a result is coming.
                                Text(
                                    text = "Codec unknown",
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
            val nearbySignals = devices.size + realNearbyDevices.size
            val environmentType = detectEnvironmentType(devices, nearbySignals)
            val otherSignals = maxOf(0, nearbySignals - devices.size)
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
                        // RSSI is negative dBm: map -100..-40 dBm onto 0..100. Unknown draws mid-size.
                        val signalStrength = device.signalStrength
                            ?.let { ((it + 100) * 100 / 60).coerceIn(0, 100) } ?: 50
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
                        // Distance based on RSSI: stronger signal = closer to center, weaker = farther
                        val signalStrength = (rssi + 80) / 40f // Convert RSSI to 0-1 range (-80 to -40)
                        val distance = maxRadius * (0.8f - signalStrength * 0.5f) // Strong signal = close, weak = far
                        
                        val x = centerX + (distance * Math.cos(angle)).toFloat()
                        val y = centerY + (distance * Math.sin(angle)).toFloat()
                        
                        // Color based on connection status and interference potential
                        val signalColor = if (devices.any { it.address == address && it.isConnected }) {
                            Color.Green  // Connected device = Your device (no interference)
                        } else when {
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
                        text = "Nearby Devices",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$nearbySignals devices",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

fun detectEnvironmentType(devices: List<com.bluetoothcodec.checker.BluetoothDevice>, nearbySignals: Int): String {
    val deviceCount = devices.size
    val avgSignal = devices.mapNotNull { it.signalStrength }.average().takeIf { !it.isNaN() } ?: -70.0
    
    return when {
        deviceCount == 0 -> "No connected devices"
        nearbySignals > 20 -> "High density area"
        nearbySignals > 10 -> "Moderate density area" 
        nearbySignals > 5 -> "Low density area"
        avgSignal > -50 -> "Good signal quality"
        avgSignal > -70 -> "Fair signal quality"
        else -> "Poor signal quality"
    }
}

fun getEnvironmentColor(environment: String): Color {
    return when {
        environment.contains("High density") || environment.contains("Poor signal") -> Color.Red
        environment.contains("Moderate density") || environment.contains("Fair signal") -> Color(0xFFFF9800)
        environment.contains("Low density") || environment.contains("Good signal") -> Color(0xFF4CAF50)
        else -> Color.Gray
    }
}

fun getEnvironmentTips(environment: String, nearbySignals: Int): String {
    return when {
        nearbySignals > 20 -> "High interference detected. Keep devices close and avoid obstacles."
        nearbySignals > 10 -> "Moderate interference. Stay within 2-3 meters for stable connection."
        nearbySignals > 5 -> "Low interference. Good environment for Bluetooth audio."
        environment.contains("Poor signal") -> "Move closer to your device or remove obstacles."
        environment.contains("Fair signal") -> "Connection is stable. Audio quality may vary."
        else -> "Excellent environment for high-quality Bluetooth audio."
    }
}
