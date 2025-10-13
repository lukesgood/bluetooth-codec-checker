# Bluetooth Codec Checker (BCC)

A comprehensive Android app for detecting and analyzing Bluetooth audio codecs, device information, and connection quality with advanced interference analysis.

## Features

### 🎵 Codec Detection
- **Real-time codec detection** for connected Bluetooth audio devices
- **Comprehensive codec support**: SBC, AAC, aptX, aptX HD, aptX LL, aptX Adaptive, LDAC, LC3
- **Hi-Res audio identification** with quality indicators
- **Visual codec chart** showing quality vs latency mapping
- **OS-Level detection method** for improved accuracy

### 📱 Device Information
- **Chipset detection** with manufacturer and model information
- **OS version** and Android version display
- **Supported codec list** based on device capabilities
- **Hi-Res audio support** indicators
- **Enhanced device name recognition** with manufacturer database

### 🔗 Connection Analysis
- **Signal strength monitoring** with distance estimation
- **Battery level tracking** for connected devices
- **Location estimation** based on RSSI values
- **Device type identification** with detailed model information
- **Real-time interference analysis** with risk assessment

### 📊 Visual Interface
- **Interactive radar chart** with interference risk zones
- **Real-time refresh** with manual and automatic updates
- **Material Design 3** UI with modern styling
- **Responsive layout** for phones and tablets
- **Color-coded interference levels** (High/Medium/Low Risk)

### 🔧 Technical Features
- **Enhanced Bluetooth scanning** with BLE and Classic discovery
- **Manufacturer OUI database** for device identification
- **Permission handling** with proper security checks
- **Auto-refresh** every 5 seconds
- **Comprehensive device database** with known codec capabilities

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

## Device Support

### Chipset Detection
- **Qualcomm Snapdragon** series with codec capabilities
- **MediaTek Dimensity** series
- **Samsung Exynos** series
- **Google Tensor** series

### Manufacturer Recognition
- **Apple**: AirPods with AAC optimization
- **Sony**: WH/WF series with LDAC support
- **Jabra**: Elite series with aptX support
- **Bose**: QuietComfort series
- **Beats**: Studio/Solo series
- **Sennheiser**: Momentum series
- **JBL**: Speakers and headphones
- **LG**: Audio devices
- **Samsung**: Galaxy Buds series
- **Plantronics**: Professional headsets

## Installation

### Latest Release (v1.7.2)
```bash
# Download and install
wget https://github.com/lukesgood/bluetooth-codec-checker/releases/download/v1.7.2/bluetooth-codec-checker-v1.7.2.apk
adb install bluetooth-codec-checker-v1.7.2.apk
```

### Debug Build
```bash
./gradlew assembleDebug
```
APK location: `app/build/outputs/apk/debug/app-debug.apk`

### Release Build
```bash
./gradlew assembleRelease
```
APK location: `app/build/outputs/apk/release/app-release-unsigned.apk`

## Permissions Required

- **BLUETOOTH_CONNECT** (Android 12+)
- **BLUETOOTH_SCAN** (Android 12+)
- **ACCESS_FINE_LOCATION** (for device discovery)
- **BLUETOOTH** (Android 11 and below)
- **BLUETOOTH_ADMIN** (Android 11 and below)

## Technical Implementation

### Architecture
- **MVVM pattern** with Compose UI
- **Kotlin Coroutines** for async operations
- **Material Design 3** components
- **Enhanced Bluetooth APIs** for comprehensive detection

### Detection Methods
1. **OS-Level Detection** (Primary method for accuracy)
2. **AudioManager** codec configuration
3. **System properties** for codec support
4. **Device-specific estimation** based on manufacturer database

### Interference Analysis
- **Dual scanning** (BLE + Classic Bluetooth)
- **RSSI-based positioning** on radar chart
- **Color-coded risk levels** (Red/Orange/Yellow for High/Medium/Low risk)
- **Real-time device discovery** with manufacturer identification

## Version History

### v1.7.2 (Current)
- **Fixed radar chart positioning** - weak signals now appear farther from center
- **Corrected proximity representation** for interference analysis
- **Improved visual accuracy** of signal strength mapping

### v1.7.1
- **Clarified interference color logic** throughout the app
- **Enhanced legend** with High/Medium/Low Risk indicators
- **Consistent color meaning** for interference analysis

### v1.7
- **Enhanced interference source naming** with manufacturer recognition
- **Added manufacturer OUI database** for 10+ major brands
- **Improved device type inference** from MAC address patterns
- **Enhanced Bluetooth scanning** with dual BLE/Classic discovery
- **Better fallback naming** instead of "Unknown Device"

### v1.6
- **Simplified codec detection** to use only OS-Level Detection method
- **Removed unreliable detection methods** for improved accuracy
- **Faster execution** with single detection method
- **Cleaner logging** and error handling

### v1.5
- **Eliminated fake RSSI generation** and simulated device data
- **Fixed crash caused by invalid RSSI range**
- **Added real nearby device detection** with differentiated signal strengths
- **Clean radar chart** showing only genuine Bluetooth connections

### v1.4
- **Multiple codec detection methods** using Android APIs and reflection
- **LG device support** added to codec database
- **Improved detection reliability** for supported Android versions
- **Enhanced compatibility** across different manufacturers

### v1.3
- Enhanced UI with repositioned battery and signal indicators
- Professional green/gray dot codec indicators
- Manufacturer name detection for 20+ major audio brands
- Bluetooth signal congestion analysis with radiation charts
- Environmental interference monitoring

### v1.2
- Enhanced codec detection accuracy
- Improved UI with codec chart visualization
- Added device location estimation
- Better permission handling

### v1.1
- Added chipset detection
- Improved device recognition
- Enhanced battery monitoring

### v1.0
- Initial release
- Basic codec detection
- Simple device listing

## Interference Analysis Features

### Color-Coded Risk System
- 🔴 **Red**: Close devices (High interference risk)
- 🟠 **Orange**: Medium distance (Medium interference)
- 🟡 **Yellow**: Far devices (Low interference risk)
- 🟢 **Green**: Connected devices (No interference)

### Radar Chart
- **Interactive visualization** of nearby Bluetooth devices
- **Distance-based positioning** (closer = stronger signal)
- **Real-time updates** every 5 seconds
- **Interference risk zones** with color coding

### Device Recognition
- **Manufacturer identification** from MAC address OUI
- **Device type inference** (Audio Device, Phone/Tablet, Laptop/PC)
- **Signal strength monitoring** with RSSI values
- **Meaningful device names** instead of "Unknown Device"

## Build Requirements

- **Android Studio** Arctic Fox or later
- **Gradle** 8.0+
- **Kotlin** 1.9+
- **Compose** 1.5+
- **Target SDK** 34
- **Min SDK** 26 (Android 8.0)

## Contributing

The app uses advanced Android APIs and enhanced Bluetooth scanning for comprehensive codec and interference analysis. Contributions are welcome for:
- Additional device support
- Improved codec detection methods
- Enhanced interference analysis
- UI/UX improvements
- Performance optimizations

## License

This project is for educational and research purposes. Please respect device manufacturer APIs and user privacy when using codec detection and interference analysis features.
