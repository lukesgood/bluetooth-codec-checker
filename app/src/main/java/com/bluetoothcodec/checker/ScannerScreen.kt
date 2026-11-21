package com.bluetoothcodec.checker

import android.bluetooth.BluetoothClass
import android.os.SystemClock
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

private enum class TransportFilter(val label: String) {
    ALL("All"), BLE("BLE"), CLASSIC("Classic")
}

private enum class SortOrder(val label: String) {
    SIGNAL("Signal ↓"), NAME("Name A–Z")
}

/**
 * Lists every device the radio can currently hear, with filters, and opens a detail
 * sheet that follows one device's signal so it can be located by walking around.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    devices: List<NearbyDevice>,
    connectedAddresses: Set<String>,
    classicDiscovering: Boolean,
    onStartClassicDiscovery: () -> Unit,
    modifier: Modifier = Modifier
) {
    var transportFilter by rememberSaveable { mutableStateOf(TransportFilter.ALL) }
    var namedOnly by rememberSaveable { mutableStateOf(false) }
    var sortOrder by rememberSaveable { mutableStateOf(SortOrder.SIGNAL) }
    var selectedAddress by rememberSaveable { mutableStateOf<String?>(null) }
    // Keeps the sheet populated if the selected device stops being heard.
    var lastSelected by remember { mutableStateOf<NearbyDevice?>(null) }

    val visible = remember(devices, transportFilter, namedOnly, sortOrder) {
        devices
            .filter {
                when (transportFilter) {
                    TransportFilter.ALL -> true
                    TransportFilter.BLE -> it.transport == Transport.BLE
                    TransportFilter.CLASSIC -> it.transport == Transport.CLASSIC
                }
            }
            .filter { !namedOnly || it.name != null }
            .let { list ->
                when (sortOrder) {
                    // Sort on 5 dB steps of the smoothed value so rows do not reshuffle
                    // every second on ordinary RSSI jitter.
                    SortOrder.SIGNAL -> list.sortedWith(
                        compareByDescending<NearbyDevice> { it.smoothedRssi / 5 }.thenBy { it.address }
                    )
                    SortOrder.NAME -> list.sortedWith(
                        compareBy<NearbyDevice> { it.name == null }
                            .thenBy { it.displayName.lowercase() }
                            .thenBy { it.address }
                    )
                }
            }
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "summary") {
            ScannerSummary(
                total = devices.size,
                shown = visible.size,
                classicDiscovering = classicDiscovering,
                onStartClassicDiscovery = onStartClassicDiscovery
            )
        }
        item(key = "filters") {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TransportFilter.values().forEach { option ->
                    FilterChip(
                        selected = transportFilter == option,
                        onClick = { transportFilter = option },
                        label = { Text(option.label) }
                    )
                }
                FilterChip(
                    selected = namedOnly,
                    onClick = { namedOnly = !namedOnly },
                    label = { Text("Named") }
                )
                AssistChip(
                    onClick = {
                        sortOrder =
                            if (sortOrder == SortOrder.SIGNAL) SortOrder.NAME else SortOrder.SIGNAL
                    },
                    label = { Text(sortOrder.label) }
                )
            }
        }
        if (visible.isEmpty()) {
            item(key = "empty") {
                Text(
                    text = if (devices.isEmpty()) "Listening for nearby devices…"
                    else "No devices match these filters.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }
        }
        items(visible, key = { it.address }) { device ->
            NearbyDeviceRow(
                device = device,
                connected = device.address in connectedAddresses,
                onClick = {
                    lastSelected = device
                    selectedAddress = device.address
                }
            )
        }
        item(key = "footnote") {
            Text(
                text = "Many phones, watches and trackers change their Bluetooth address every " +
                    "few minutes, so one device can appear here more than once over time.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }

    val address = selectedAddress
    if (address != null) {
        val live = devices.firstOrNull { it.address == address }
        if (live != null) lastSelected = live
        val shown = live ?: lastSelected?.takeIf { it.address == address }
        if (shown == null) {
            // Restored after process death with nothing to show.
            selectedAddress = null
        } else {
            ModalBottomSheet(onDismissRequest = { selectedAddress = null }) {
                DeviceDetail(
                    device = shown,
                    stillHeard = live != null,
                    connected = address in connectedAddresses
                )
            }
        }
    }
}

@Composable
private fun ScannerSummary(
    total: Int,
    shown: Int,
    classicDiscovering: Boolean,
    onStartClassicDiscovery: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (total == 1) "1 device in range" else "$total devices in range",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (shown == total) "BLE scan running" else "BLE scan running · showing $shown",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = onStartClassicDiscovery, enabled = !classicDiscovering) {
                    if (classicDiscovering) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .width(16.dp)
                                .height(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Searching classic devices…")
                    } else {
                        Text("Search classic devices")
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Finds speakers and headsets in pairing mode. Takes about 12 seconds " +
                    "and may briefly interrupt Bluetooth audio.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NearbyDeviceRow(device: NearbyDevice, connected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (device.name == null) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = deviceSubtitle(device, connected),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = device.address,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
                SignalBars(bars = signalBars(device.smoothedRssi))
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${device.rssi} dBm",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

private fun deviceSubtitle(device: NearbyDevice, connected: Boolean): String {
    val parts = mutableListOf<String>()
    parts += if (device.transport == Transport.BLE) "BLE" else "Classic"
    device.manufacturer?.let { parts += it }
    majorClassLabel(device.majorClass)?.let { parts += it }
    when {
        connected -> parts += "Connected"
        device.bonded -> parts += "Paired"
    }
    return parts.joinToString(" · ")
}

private fun majorClassLabel(majorClass: Int?): String? = when (majorClass) {
    null -> null
    BluetoothClass.Device.Major.AUDIO_VIDEO -> "Audio/Video"
    BluetoothClass.Device.Major.COMPUTER -> "Computer"
    BluetoothClass.Device.Major.PHONE -> "Phone"
    BluetoothClass.Device.Major.PERIPHERAL -> "Peripheral"
    BluetoothClass.Device.Major.WEARABLE -> "Wearable"
    BluetoothClass.Device.Major.HEALTH -> "Health"
    BluetoothClass.Device.Major.IMAGING -> "Imaging"
    BluetoothClass.Device.Major.NETWORKING -> "Networking"
    BluetoothClass.Device.Major.TOY -> "Toy"
    else -> null
}

@Composable
private fun SignalBars(bars: Int, modifier: Modifier = Modifier) {
    val active = MaterialTheme.colorScheme.primary
    val inactive = MaterialTheme.colorScheme.outlineVariant
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        repeat(4) { index ->
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height((6 + index * 4).dp)
                    .background(
                        color = if (index < bars) active else inactive,
                        shape = RoundedCornerShape(1.dp)
                    )
            )
        }
    }
}

@Composable
internal fun DeviceDetail(device: NearbyDevice, stillHeard: Boolean, connected: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
    ) {
        Text(
            text = device.displayName,
            style = MaterialTheme.typography.headlineSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = deviceSubtitle(device, connected),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (stillHeard) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${device.rssi}",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "dBm",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                SignalBars(
                    bars = signalBars(device.smoothedRssi),
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
            Text(
                text = proximityLabel(device.smoothedRssi) + trendSuffix(signalTrend(device.rssiHistory)),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Text(
                text = "Signal lost",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = "Last heard ${secondsAgo(device.lastSeenMs)} s ago at ${device.rssi} dBm.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        RssiSparkline(history = device.rssiHistory)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "To find it, move slowly and watch the number: closer to zero means a " +
                "stronger signal. Walls, bodies and the way the device is facing change the " +
                "reading as much as distance does, so treat it as a rough guide.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))
        DetailLine("Address", device.address)
        device.companyId?.let { DetailLine("Company ID", "0x%04X".format(it)) }
        device.connectable?.let { DetailLine("Connectable", if (it) "Yes" else "No") }
        DetailLine("Samples", device.rssiHistory.size.toString())
    }
}

private fun trendSuffix(trend: SignalTrend?): String = when (trend) {
    SignalTrend.RISING -> " · signal rising"
    SignalTrend.FALLING -> " · signal falling"
    SignalTrend.STEADY -> " · steady"
    null -> ""
}

private fun secondsAgo(lastSeenMs: Long): Long =
    ((SystemClock.elapsedRealtime() - lastSeenMs) / 1000).coerceAtLeast(0)

@Composable
private fun DetailLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
    }
}

/** RSSI samples, oldest on the left, drawn on a fixed -100..-30 dBm scale. */
@Composable
private fun RssiSparkline(history: List<Int>) {
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
    ) {
        fun yFor(rssi: Int): Float {
            val fraction = ((rssi + 100) / 70f).coerceIn(0f, 1f)
            return size.height * (1f - fraction)
        }
        // Guide lines at -50 and -80 dBm.
        listOf(-50, -80).forEach { level ->
            drawLine(
                color = gridColor,
                start = Offset(0f, yFor(level)),
                end = Offset(size.width, yFor(level)),
                strokeWidth = 1.dp.toPx()
            )
        }
        if (history.size >= 2) {
            val step = size.width / (NearbyDevice.MAX_HISTORY - 1)
            val path = Path()
            history.forEachIndexed { index, rssi ->
                val x = index * step
                if (index == 0) path.moveTo(x, yFor(rssi)) else path.lineTo(x, yFor(rssi))
            }
            drawPath(path, color = lineColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
        } else if (history.size == 1) {
            drawCircle(color = lineColor, radius = 3.dp.toPx(), center = Offset(0f, yFor(history[0])))
        }
    }
}
