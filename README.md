# Bluetooth Codec Checker (BCC)

A comprehensive Android app for detecting and analyzing Bluetooth audio codecs, device information, and connection quality.

## Features

### 🎵 Codec Detection
- **Real-time codec detection** for connected Bluetooth audio devices
- **Comprehensive codec support**: SBC, AAC, aptX, aptX HD, aptX LL, aptX Adaptive, LDAC, LC3
- **Hi-Res audio identification** with quality indicators
- **Visual codec chart** showing quality vs latency mapping

### 📱 Device Information
- **Chipset detection** with manufacturer and model information
- **OS version** and Android version display
- **Supported codec list** based on device capabilities
- **Hi-Res audio support** indicators

### 🔗 Connection Analysis
- **Signal strength** monitoring with distance estimation
- **Battery level** tracking for connected devices
- **Location estimation** based on RSSI values
- **Device type identification** with detailed model information

### 📊 Visual Interface
- **Interactive codec chart** with quality vs latency positioning
- **Real-time refresh** with manual and automatic updates
- **Material Design 3** UI with modern styling
- **Responsive layout** for phones and tablets

### 🔧 Technical Features
- **Multiple detection methods**: Reflection API, logcat parsing, device estimation
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

### Brand Recognition
- **Sony**: WH/WF series with LDAC support
- **Jabra**: Elite series with aptX support
- **Apple**: AirPods with AAC optimization
- **Beats**: Studio/Solo series
- **Bose**: QuietComfort series
- **Sennheiser**: Momentum series

## Installation

### Latest Release (v1.5)
```bash
# Download and install
wget https://github.com/user/bluetooth-codec-checker/releases/download/v1.5/bluetooth-codec-checker-v1.5.apk
adb install bluetooth-codec-checker-v1.5.apk
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
- **Reflection API** for advanced codec detection

### Detection Methods
1. **Bluetooth A2DP Profile** reflection
2. **AudioManager** codec configuration
3. **System properties** for codec support
4. **Device-specific estimation** based on known capabilities

### Data Sources
- Android Bluetooth APIs
- System audio configuration
- Device manufacturer databases
- Real-time connection monitoring

## Version History

### v1.5 (Current)
- **Eliminated all fake RSSI generation** and simulated device data
- **Fixed crash caused by invalid RSSI range** (-40..-80 corrected to -80..-40)
- **Removed duplicate device info** from Device Information panel
- **Deleted Hi-Res audio codec information text**
- **Added real nearby device detection** with differentiated signal strengths
- **Show actual RSSI values** with color-coded strength indicators
- **Clean radar chart** showing only genuine Bluetooth connections

### v1.4
- **Multiple codec detection methods** using Android APIs and reflection
- **LG device support** added to codec database
- **Improved detection reliability** for supported Android versions
- **Additional detection methods**: Media Metrics, HCI logs, Vendor Properties, Audio Policy
- **Better SBC fallback handling** for unsupported codecs
- **Enhanced compatibility** across different manufacturers

### v1.3
- Enhanced UI with repositioned battery and signal indicators
- Professional green/gray dot codec indicators (replaced emoji)
- Manufacturer name detection for 20+ major audio brands
- Bluetooth signal congestion analysis with radiation charts
- Environmental interference monitoring (metro, office, mall, residential)
- Improved codec chart positioning with collision detection
- Fixed streaming indicator logic for single active codec
- Optimized APK size and performance

### v1.2
- Enhanced codec detection accuracy
- Improved UI with codec chart visualization
- Added device location estimation
- Better permission handling
- Real-time refresh functionality

### v1.1
- Added chipset detection
- Improved device recognition
- Enhanced battery monitoring

### v1.0
- Initial release
- Basic codec detection
- Simple device listing

## Build Requirements

- **Android Studio** Arctic Fox or later
- **Gradle** 8.0+
- **Kotlin** 1.9+
- **Compose** 1.5+
- **Target SDK** 34
- **Min SDK** 26 (Android 8.0)

## Contributing

The app uses advanced Android APIs and reflection to detect codec information. Contributions are welcome for:
- Additional device support
- Improved codec detection methods
- UI/UX enhancements
- Performance optimizations

## License

This project is for educational and research purposes. Please respect device manufacturer APIs and user privacy when using codec detection features.
