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
    
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshData()
        // Auto-refresh every 5 seconds
        while (true) {
            kotlinx.coroutines.delay(5000)
            viewModel.refreshData()
        }
    }
    
    // Manual refresh function
    val onRefresh: () -> Unit = {
        isRefreshing = true
        viewModel.refreshData()
        // Reset refresh state immediately
        isRefreshing = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (!hasPermission) {
            PermissionCard(onRequestPermissions)
        } else {
            // Refresh button at top
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bluetooth Codec Checker",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(
                    onClick = onRefresh,
                    enabled = !isRefreshing
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("🔄", fontSize = 20.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Chipset Info
                chipsetInfo?.let { info ->
                    item {
                        ChipsetCard(info, devices)
                    }
                }

                // Connected Devices
                if (devices.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Connected Audio Devices",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            
                            if (isSearching) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                    
                    items(devices) { device ->
                        DeviceCard(device)
                    }
                } else if (!isSearching) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No connected audio devices",
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Tap 🔄 to refresh or connect a Bluetooth headset",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
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
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Supported Codecs:",
                fontWeight = FontWeight.Medium
            )
            
            // Hi-Res support indicator
            val hiResCodecs = chipsetInfo.supportedCodecs.filter { codec ->
                BluetoothCodecs.CODEC_INFO[codec]?.let { info ->
                    isHiResCodec(info.sampleRate)
                } ?: false
            }
            
            if (hiResCodecs.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "🎵 Hi-Res Audio Support: ",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = hiResCodecs.joinToString(", "),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            CodecChart(chipsetInfo.supportedCodecs, devices.firstOrNull()?.activeCodec)
        }
    }
}

@Composable
fun DeviceCard(device: BluetoothDevice) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Device name and status with location indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = device.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Location estimation based on signal strength
                    device.signalStrength?.let { rssi ->
                        val (distance, location) = estimateLocationFromRSSI(rssi)
                        Text(
                            text = "📍 $location (~${distance}m)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Battery
                    device.batteryLevel?.let { battery ->
                        Text(
                            text = "${getBatteryIcon(battery)} $battery%",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Signal strength with distance
                    device.signalStrength?.let { rssi ->
                        Text(
                            text = "${getSignalIcon(rssi)} ${rssi}dBm",
                            fontSize = 12.sp,
                            color = getSignalColor(rssi)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = device.address,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Device model/class information
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = getDeviceTypeInfo(device.name),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
            
            // Active codec with enhanced visibility
            device.activeCodec?.let { codec ->
                Spacer(modifier = Modifier.height(8.dp))
                
                // Enhanced active codec display
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Active Codec",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = codec,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        // Real-time codec parameters
                        BluetoothCodecs.CODEC_INFO[codec]?.let { codecInfo ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Sampling Rate
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = codecInfo.sampleRate,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Sample Rate",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = getSampleRateQuality(codecInfo.sampleRate),
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }
                                
                                // Bitrate
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = codecInfo.bitrate,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = "Bitrate",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                // Latency
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = codecInfo.latency,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                    Text(
                                        text = "Latency",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CodecChart(supportedCodecs: List<String>, activeCodec: String? = null) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val isTablet = screenWidth > 600.dp
    
    val chartHeight = if (isTablet) 400.dp else 280.dp
    val chartAreaWidth = if (isTablet) 300f else 160f
    val chartAreaHeight = if (isTablet) 240f else 120f
    
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
            Text(
                text = "Latency (ms)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Box(modifier = Modifier.fillMaxSize()) {
                // Y-axis values with corrected minimum latency
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 35.dp, bottom = 35.dp)
                ) {
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
                    
                    // Auto-position codecs with collision detection based on quality ranking
                    val codecPositions = mutableMapOf<String, Pair<Float, Float>>()
                    val occupiedAreas = mutableListOf<Pair<Float, Float>>()
                    
                    BluetoothCodecs.ALL_CODECS.forEach { codecName ->
                        BluetoothCodecs.CODEC_INFO[codecName]?.let { codecInfo ->
                            val latencyValue = codecInfo.latency.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 40
                            
                            // Quality ranking (0.0 to 1.0, higher = better quality)
                            val qualityRanking = when(codecName) {
                                "SBC" -> 0.1f        // Basic quality
                                "LC3" -> 0.2f        // Efficient but basic quality
                                "AAC" -> 0.4f        // Good efficiency and quality
                                "aptX" -> 0.5f       // Balanced performance
                                "aptX LL" -> 0.5f    // Same quality as aptX
                                "aptX Adaptive" -> 0.7f  // Adaptive quality
                                "aptX HD" -> 0.8f    // High quality
                                "LDAC" -> 1.0f       // Highest quality
                                else -> 0.5f
                            }
                            
                            // Normalize latency for Y positioning
                            val normalizedLatency = ((latencyValue - 20f) / (200f - 20f)).coerceIn(0f, 1f)
                            
                            var targetX = qualityRanking * chartAreaWidth
                            var targetY = normalizedLatency * chartAreaHeight
                            
                            // Check for collisions
                            val minDistance = if (isTablet) 45f else 30f
                            var attempts = 0
                            while (attempts < 15) {
                                val hasCollision = occupiedAreas.any { (existingX, existingY) ->
                                    val distance = kotlin.math.sqrt((targetX - existingX) * (targetX - existingX) + 
                                                                  (targetY - existingY) * (targetY - existingY))
                                    distance < minDistance
                                }
                                
                                if (!hasCollision) break
                                
                                // Spiral repositioning
                                val angle = attempts * 0.8f
                                val radius = (attempts + 1) * (if (isTablet) 8f else 6f)
                                targetX = (qualityRanking * chartAreaWidth) + radius * kotlin.math.cos(angle).toFloat()
                                targetY = (normalizedLatency * chartAreaHeight) + radius * kotlin.math.sin(angle).toFloat()
                                targetX = targetX.coerceIn(0f, chartAreaWidth - 40f)
                                targetY = targetY.coerceIn(0f, chartAreaHeight - 40f)
                                attempts++
                            }
                            
                            occupiedAreas.add(Pair(targetX, targetY))
                            codecPositions[codecName] = Pair(targetX, targetY)
                        }
                    }
                    
                    codecPositions.forEach { (codecName, position) ->
                        BluetoothCodecs.CODEC_INFO[codecName]?.let { codecInfo ->
                            val latencyValue = codecInfo.latency.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 40
                            val normalizedLatency = ((latencyValue - 20f) / (200f - 20f)).coerceIn(0f, 1f)
                            val qualityRanking = when(codecName) {
                                "SBC" -> 0.1f; "LC3" -> 0.2f; "AAC" -> 0.4f; "aptX" -> 0.5f
                                "aptX LL" -> 0.5f; "aptX Adaptive" -> 0.7f; "aptX HD" -> 0.8f; "LDAC" -> 1.0f
                                else -> 0.5f
                            }
                            
                            // Size based on quality (higher quality = larger icon)
                            val baseSize = if (isTablet) 20f else 12f
                            val qualityBasedSize = (baseSize + qualityRanking * (if (isTablet) 24f else 16f)).dp
                            val latencyBasedSize = maxOf(baseSize, (baseSize + 16f) - (normalizedLatency * 12f)).dp
                            val finalIconSize = maxOf(qualityBasedSize, latencyBasedSize)
                            val isHiRes = isHiResCodec(codecInfo.sampleRate)
                            
                            Box(
                                modifier = Modifier.offset(
                                    x = position.first.dp,
                                    y = position.second.dp
                                )
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when {
                                        codecName == activeCodec -> MaterialTheme.colorScheme.primary
                                        supportedCodecs.contains(codecName) -> MaterialTheme.colorScheme.secondary
                                        else -> Color.Gray.copy(alpha = 0.4f)
                                    },
                                    modifier = Modifier
                                        .widthIn(min = finalIconSize)
                                        .heightIn(min = finalIconSize)
                                        .padding(1.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = codecName,
                                            fontSize = maxOf(if (isTablet) 9f else 7f, finalIconSize.value / 4f).sp,
                                            color = if (supportedCodecs.contains(codecName) || codecName == activeCodec) {
                                                Color.White
                                            } else {
                                                Color.Gray
                                            },
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                        
                                        // Hi-Res indicator for larger icons
                                        if (isHiRes && finalIconSize > (if (isTablet) 24.dp else 18.dp)) {
                                            Text(
                                                text = "Hi-Res",
                                                fontSize = if (isTablet) 7.sp else 5.sp,
                                                color = Color(0xFFFFD700),
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

fun estimateLocationFromRSSI(rssi: Int): Pair<String, String> {
    val distance = when {
        rssi >= -40 -> "0.5-1"
        rssi >= -50 -> "1-2"
        rssi >= -60 -> "2-5"
        rssi >= -70 -> "5-10"
        rssi >= -80 -> "10-20"
        else -> "20+"
    }
    
    val location = when {
        rssi >= -40 -> "Very Close (같은 방)"
        rssi >= -50 -> "Close (인근)"
        rssi >= -60 -> "Near (같은 층)"
        rssi >= -70 -> "Moderate (다른 방)"
        rssi >= -80 -> "Far (다른 층)"
        else -> "Very Far (멀리)"
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
