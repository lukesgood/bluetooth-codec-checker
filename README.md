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
- **Real-time codec monitoring** during audio playbook
- **Device-specific optimization** for various audio devices
- **Hidden API workaround** for maximum compatibility

### 📡 High-Performance Scanning
- **SCAN_MODE_LOW_LATENCY** for maximum performance
- **Aggressive matching** with immediate result reporting
- **Auto-recovery** with fast failure recovery time
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

### Requirements
- **Android 8.0+** (API 26+)
- **Bluetooth permissions** (CONNECT, SCAN)
- **Location permission** (for device discovery)

### Build from Source
```bash
git clone https://github.com/lukesgood/bluetooth-codec-checker.git
cd bluetooth-codec-checker
./gradlew assembleDebug
```

## 🎯 Performance Metrics

### Near Real-Time Performance
- **UI Updates**: 1 second for instant feedback
- **Scan Cycles**: 5 seconds for continuous monitoring
- **Failure Recovery**: Fast automatic recovery
- **Restart Delay**: Minimal delay for continuous operation

### Signal Analysis
- **Data Points**: 20 per device for accurate analysis
- **Time Window**: 2 minutes optimized window
- **RSSI Range**: Wide range detection capability
- **Update Frequency**: Near real-time updates

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
- ✅ **Premium devices**: High-quality codec detection
- ✅ **Standard devices**: Reliable codec identification
- ✅ **Generic devices**: Fallback codec support
- ✅ **Multiple devices**: Simultaneous tracking

### Signal Analysis Performance
- ✅ **RSSI tracking**: Wide range signal detection
- ✅ **Continuous monitoring**: No scan interruptions
- ✅ **Fast recovery**: Quick failure recovery
- ✅ **Multiple devices**: Concurrent device tracking

## 📄 License

This project is for educational and research purposes. Please respect device manufacturer APIs and user privacy when using codec detection and signal analysis features.

---

**Bluetooth Codec Checker - Near Real-Time Professional Audio Analysis**
