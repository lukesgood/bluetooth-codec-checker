# Bluetooth Codec Checker (BCC) v2.0.1

A professional Android app for detecting and analyzing Bluetooth audio codecs, device information, and connection quality with practical interference analysis.

## Features

### 🎵 Codec Detection
- **Real-time codec detection** for connected Bluetooth audio devices
- **Comprehensive codec support**: SBC, AAC, aptX, aptX HD, aptX LL, aptX Adaptive, LDAC, LC3
- **Hi-Res audio identification** with quality indicators
- **OS-Level detection method** for maximum accuracy
- **Professional codec analysis** with technical specifications

### 📱 Device Information
- **Chipset detection** with manufacturer and model information
- **OS version** and Android version display
- **Supported codec list** based on device capabilities
- **Enhanced device name recognition** with manufacturer database
- **Professional audio engineer mode** with technical metrics

### 🔗 Connection Analysis
- **Signal strength monitoring** with distance estimation
- **Battery level tracking** for connected devices
- **Real-time interference analysis** with risk assessment
- **Device type identification** with detailed model information
- **Professional radar chart** with interference visualization

### 📊 Professional Interface
- **Audio Engineer Mode** with technical measurements
- **Interactive radar chart** with interference risk zones
- **Material Design 3** UI with professional styling
- **Real system data only** - no pseudo or estimated values
- **Color-coded interference levels** (High/Medium/Low Risk)

### 🔧 Technical Features
- **Enhanced Bluetooth scanning** with BLE and Classic discovery
- **Manufacturer OUI database** for device identification
- **Real audio system metrics** from Android AudioManager
- **Professional performance analysis**
- **No root, ADB, or developer options required**

## Supported Codecs

| Codec | Quality | Latency | Sample Rate | Bitrate |
|-------|---------|---------|-------------|---------|
| **LDAC** | Premium | 150-200ms | 48-96 kHz | 330-990 kbps |
| **aptX HD** | High | 130ms | 48 kHz | 576 kbps |
| **aptX Adaptive** | High | 50-80ms | 48-96 kHz | 279-420 kbps |
| **aptX** | Good | 40ms | 48 kHz | 352 kbps |
| **aptX LL** | Good | 32-40ms | 48 kHz | 352 kbps |
| **AAC** | Good | 40-150ms | 44.1 kHz | 256 kbps |
| **LC3** | Basic | 20-30ms | 48 kHz | 160 kbps |
| **SBC** | Basic | 40-200ms | 44.1 kHz | 328 kbps |

## Installation

### Latest Release (v2.0.1)
```bash
# Download and install
wget https://github.com/lukesgood/bluetooth-codec-checker/releases/download/v2.0.1/bluetooth-codec-checker-v2.0.1.apk
adb install bluetooth-codec-checker-v2.0.1.apk
```

### Build from Source
```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease
```

## Permissions Required

- **BLUETOOTH_CONNECT** (Android 12+)
- **BLUETOOTH_SCAN** (Android 12+)
- **ACCESS_FINE_LOCATION** (for device discovery)
- **BLUETOOTH** (Android 11 and below)
- **BLUETOOTH_ADMIN** (Android 11 and below)

## Professional Features

### Audio Engineer Mode
- **Sample Rate**: Real measurements from AudioManager (44.1kHz, 48kHz, 96kHz)
- **Buffer Size**: Actual system buffer configuration (frames)
- **Audio Latency**: Calculated from real buffer size and sample rate
- **Volume Levels**: Current system volume settings
- **Audio Mode**: Real Android audio state (Normal, In Call, Communication)
- **System Status**: Live Bluetooth connection status

### Interference Analysis
- **Dual scanning** (BLE + Classic Bluetooth)
- **RSSI-based positioning** on professional radar chart
- **Color-coded risk levels** with realistic thresholds
- **Manufacturer identification** from MAC address OUI database
- **Real-time device discovery** with enhanced naming

### Performance Classifications
- **Latency Classes**: Ultra-Low, Low, Medium, High, Very High
- **Sample Rate Classes**: Hi-Res, High, Standard, Low
- **Buffer Classes**: Low-Latency, Balanced, Stable, High-Latency

## Technical Implementation

### Architecture
- **MVVM pattern** with Jetpack Compose UI
- **Kotlin Coroutines** for async operations
- **Material Design 3** components
- **Real system data only** - no pseudo measurements

### Detection Methods
- **OS-Level Detection** (Primary method for accuracy)
- **AudioManager** real-time configuration
- **System properties** for codec support
- **Enhanced Bluetooth APIs** for comprehensive scanning

### Data Sources
- **Android AudioManager** for real audio metrics
- **Bluetooth APIs** for device and codec information
- **System properties** for hardware capabilities
- **Manufacturer OUI database** for device identification

## Version History

### v2.0.1 (Latest)
- **Practical interference analysis** - Connected devices now show as green (no interference)
- **Simple device count** instead of complex interference levels
- **Real-time Bluetooth scanning** with actual device discovery
- **Improved user experience** for close connected devices (earbuds, headphones)

### v2.0.0 (Previous Stable)
- **Frozen stable version** with all core features
- **Removed experimental features** that caused compilation issues
- **Professional audio engineer mode** with real system data
- **Enhanced interference analysis** with realistic thresholds
- **Clean, stable codebase** ready for production use

### v1.7.4 (Previous Stable)
- **Fixed radar chart positioning** - weak signals appear farther from center
- **Corrected proximity representation** for interference analysis
- **Improved visual accuracy** of signal strength mapping
- **Realistic interference levels** instead of exaggerated warnings

### v1.7.1
- **Clarified interference color logic** throughout the app
- **Enhanced legend** with High/Medium/Low Risk indicators
- **Consistent color meaning** for interference analysis

### v1.7
- **Enhanced interference source naming** with manufacturer recognition
- **Added manufacturer OUI database** for 10+ major brands
- **Improved device type inference** from MAC address patterns
- **Enhanced Bluetooth scanning** with dual BLE/Classic discovery

## Interference Analysis

### Color-Coded Risk System
- 🔴 **Red**: Close devices (High interference risk)
- 🟠 **Orange**: Medium distance (Medium interference)
- 🟡 **Yellow**: Far devices (Low interference risk)
- 🟢 **Green**: Connected devices (No interference)

### Professional Radar Chart
- **Distance-based positioning** (closer = stronger signal)
- **Real-time updates** with enhanced device naming
- **Interference risk zones** with professional color coding
- **Manufacturer identification** for better device recognition

### Supported Manufacturers
- **Apple**: AirPods, Beats products
- **Sony**: WH/WF series with LDAC support
- **Jabra**: Elite series with aptX support
- **Bose**: QuietComfort series
- **Sennheiser**: Momentum series
- **Samsung**: Galaxy Buds series
- **JBL**: Speakers and headphones
- **LG**: Audio devices
- **Plantronics**: Professional headsets

## Build Requirements

- **Android Studio** Arctic Fox or later
- **Gradle** 8.0+
- **Kotlin** 1.9+
- **Compose** 1.5+
- **Target SDK** 34
- **Min SDK** 26 (Android 8.0)

## Technical Specifications

### Real System Data
- **Sample Rate**: Direct from `AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE`
- **Buffer Size**: Real from `AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER`
- **Audio Latency**: Calculated as `(bufferSize / sampleRate) * 1000`
- **Volume Levels**: Actual system volume via `getStreamVolume()`
- **Audio Mode**: Real mode from `audioManager.mode`
- **Bluetooth Status**: True connection state from system APIs

### No Pseudo Data
- **No estimated bitrates** or fake quality scores
- **No simulated measurements** or calculated predictions
- **No artificial intelligence** or predictive features
- **Pure system information** from Android audio stack

## Contributing

This is a **frozen, stable release**. The app provides professional-grade Bluetooth codec analysis using real Android system data. No additional features will be added to maintain stability.

## License

This project is for educational and research purposes. Please respect device manufacturer APIs and user privacy when using codec detection and interference analysis features.

---

**Bluetooth Codec Checker v2.0.0 - Professional Audio Analysis Tool**
