# Bluetooth Codec Checker (BCC)

A professional Android app for **near real-time** Bluetooth audio codec detection and signal analysis.

## 🚀 Key Features

### ⚡ Near Real-Time Analysis
- **1-second UI updates** for instant feedback
- **5-second scan cycles** with continuous monitoring
- **Instant RSSI detection** with immediate logging
- **20 data points** signal history for accurate analysis

### 🎯 Enhanced Codec Detection
- **Multi-method detection**: BluetoothCodecConfig API + AudioManager + Log analysis
- **Real-time codec monitoring** during audio playback
- **Device-specific optimization** (LG Tone T90S aptX Adaptive)
- **Hidden API workaround** for maximum compatibility

### 📡 High-Performance Scanning
- **SCAN_MODE_LOW_LATENCY** for maximum performance
- **Aggressive matching** with immediate result reporting
- **Auto-recovery** with 0.5s failure recovery time
- **Continuous operation** with automatic restart

## 📊 Supported Codecs

| Codec | Quality | Latency | Detection Method | Real-Time |
|-------|---------|---------|------------------|-----------|
| **LDAC** | Premium | 150-200ms | AudioManager + Log | ✅ |
| **aptX Adaptive** | High | 50-80ms | Log Analysis + Device ID | ✅ |
| **aptX HD** | High | 130ms | AudioManager + System | ✅ |
| **aptX** | Good | 40ms | AudioManager + System | ✅ |
| **AAC** | Good | 40-150ms | AudioManager + System | ✅ |
| **LC3** | Basic | 20-30ms | AudioManager (Android 13+) | ✅ |
| **SBC** | Basic | 40-200ms | Always Available | ✅ |

## 📱 Installation

### Latest Release (v2.0.3)
```bash
# Download and install
wget https://github.com/lukesgood/bluetooth-codec-checker/releases/download/v2.0.3/bluetooth-codec-checker-v2.0.3.apk
adb install bluetooth-codec-checker-v2.0.3.apk
```

### Requirements
- **Android 8.0+** (API 26+)
- **Bluetooth permissions** (CONNECT, SCAN)
- **Location permission** (for device discovery)

## 🎯 Performance Metrics

### Near Real-Time Performance
- **UI Updates**: 1 second (80% faster)
- **Scan Cycles**: 5 seconds (83% faster)
- **Failure Recovery**: 0.5 seconds (75% faster)
- **Restart Delay**: 0.1 seconds (90% faster)

### Signal Analysis
- **Data Points**: 20 per device (100% increase)
- **Time Window**: 2 minutes (optimized)
- **RSSI Range**: -48dBm to -72dBm detected
- **Update Frequency**: Near real-time

## 🔍 Usage

### Basic Operation
1. **Install app** and grant permissions
2. **Connect Bluetooth audio device**
3. **Start music playback** for codec detection
4. **View real-time analysis** in app interface

### Advanced Features
- **Real-time codec switching** detection
- **Signal strength monitoring** with distance estimation
- **Interference analysis** with multiple device tracking
- **Professional audio metrics** display

## 🧪 Testing Results

### Codec Detection Accuracy
- ✅ **LG Tone T90S**: aptX Adaptive correctly detected
- ✅ **Sony WH-1000XM**: LDAC detection confirmed
- ✅ **Apple AirPods**: AAC optimization verified
- ✅ **Generic devices**: SBC fallback working

### Signal Analysis Performance
- ✅ **RSSI tracking**: -48dBm to -72dBm range
- ✅ **Continuous monitoring**: No scan gaps
- ✅ **Fast recovery**: 0.5s failure recovery
- ✅ **Multiple devices**: Simultaneous tracking



## 📄 License

This project is for educational and research purposes. Please respect device manufacturer APIs and user privacy when using codec detection and signal analysis features.

---

**Bluetooth Codec Checker - Near Real-Time Professional Audio Analysis**
