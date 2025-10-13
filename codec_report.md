# Bluetooth Codec Detection Methods Analysis

## Detection Order (Priority)

Based on `getCurrentCodec()` method in BluetoothManager.kt:

### 1. **Active Stream Analysis** (`getActiveStreamCodec()`)
- **Purpose**: Detects codec during active audio playback
- **Accuracy**: **HIGHEST** - Real-time detection
- **Method**: Analyzes AudioManager parameters during streaming
- **Result**: Returns actual codec being used
- **Limitation**: Only works during audio playback

### 2. **Developer Options** (`getDeveloperOptionsCodec()`)
- **Purpose**: Reads user-configured codec from Android Developer Options
- **Accuracy**: **HIGH** - User's intended codec
- **Method**: Accesses system settings for Bluetooth audio codec
- **Result**: Returns user's preferred codec setting
- **Limitation**: Requires Developer Options to be enabled

### 3. **OS-Level Detection** (`getOSLevelCodec()`)
- **Purpose**: Runtime codec detection through Android APIs
- **Accuracy**: **MEDIUM-HIGH** - System-level detection
- **Method**: Uses AudioManager and Bluetooth A2DP profile reflection
- **Result**: Returns currently negotiated codec
- **Limitation**: May show fallback codec instead of optimal

### 4. **Audio Routing** (`getCodecFromAudioRouting()`)
- **Purpose**: Detects codec from audio routing parameters
- **Accuracy**: **MEDIUM** - OS-level parameters
- **Method**: Analyzes AudioManager routing information
- **Result**: Returns codec from audio system
- **Limitation**: May not reflect actual Bluetooth codec

### 5. **System Properties** (`getCurrentCodecFromSystemProps()`)
- **Purpose**: Reads codec from Android system properties
- **Accuracy**: **MEDIUM-LOW** - System defaults
- **Method**: Accesses ro.bluetooth.* system properties
- **Result**: Returns system-configured codec
- **Limitation**: Shows defaults, not actual usage

### 6. **Device Estimation** (Device name matching)
- **Purpose**: Estimates codec based on device brand/model
- **Accuracy**: **LOW** - Educated guess
- **Method**: String matching against device names
- **Result**: Returns likely supported codec
- **Limitation**: Assumption-based, may be incorrect

## Additional Detection Methods (Not in main flow)

### 7. **aptX Detection** (`detectAptXCodec()`)
- **Purpose**: Specific aptX codec detection
- **Method**: Checks system properties for aptX support
- **Usage**: Called within other methods

### 8. **Media Metrics** (`detectCodecFromMediaMetrics()`)
- **Purpose**: Uses Android Media Metrics service
- **Accuracy**: **MEDIUM** - System metrics
- **Status**: Fallback method

### 9. **Bluetooth HCI** (`detectCodecFromBluetoothHci()`)
- **Purpose**: Low-level Bluetooth stack detection
- **Accuracy**: **HIGH** - Hardware level
- **Status**: Advanced method, may require root

### 10. **Vendor Properties** (`detectCodecFromVendorProperties()`)
- **Purpose**: Vendor-specific property detection
- **Accuracy**: **MEDIUM** - Vendor implementation
- **Status**: Device-specific

### 11. **Audio Policy** (`detectCodecFromAudioPolicy()`)
- **Purpose**: Android audio policy detection
- **Accuracy**: **MEDIUM** - Policy level
- **Status**: System-level detection

### 12. **Bluetooth Stack** (`detectCodecFromBluetoothStack()`)
- **Purpose**: Direct Bluetooth stack query
- **Accuracy**: **HIGH** - Stack level
- **Status**: Advanced detection

### 13. **Kernel Logs** (`detectCodecFromKernelLogs()`)
- **Purpose**: Parses kernel/logcat for codec information
- **Accuracy**: **HIGH** - Actual usage logs
- **Status**: Requires log access permissions

## Accuracy Ranking

1. **Active Stream Analysis** - Most accurate during playback
2. **Bluetooth HCI/Stack** - Hardware/stack level detection
3. **Kernel Logs** - Real usage evidence
4. **Developer Options** - User's intended setting
5. **OS-Level Detection** - System negotiated codec
6. **Audio Policy/Routing** - System-level detection
7. **System Properties** - Default configurations
8. **Device Estimation** - Educated guessing

## Recommendations

- **Most Reliable**: Active Stream Analysis during audio playback
- **Best Fallback**: OS-Level Detection + Developer Options
- **Avoid**: Device name estimation (too many assumptions)
- **For Development**: Enable multiple methods and compare results
