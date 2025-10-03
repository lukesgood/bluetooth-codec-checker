# Bluetooth Codec Checker v1.4 Release Notes

## What's Fixed in v1.4

### 🔧 **Accuracy Improvements**
- **Fixed codec specifications** with real-world latency ranges and quality ratings
- **Removed fake signal strength simulation** - now only shows real RSSI values or "Connected"
- **Corrected codec latency values**:
  - SBC: 40-200ms (was incorrectly ~40ms)
  - AAC: 40-150ms (was incorrectly ~80ms) 
  - LDAC: 150-200ms (was incorrectly ~150ms)
  - aptX LL: 32-40ms (was incorrectly ~20ms)

### 📊 **Real Data Only**
- **No more fake RSSI generation** - signal strength now shows actual values or null
- **Honest device codec support** - uses actual device database instead of assumptions
- **Realistic quality ratings** - codec quality scores now reflect real-world performance

### 📱 **Updated Device Database**
- **Accurate codec support** for major brands (Sony, Apple, Samsung, etc.)
- **Proper fallback handling** to SBC when codec detection fails
- **Brand-based codec estimation** when specific device data unavailable

### 🛠 **Technical Changes**
- Removed simulated connection quality estimation
- Eliminated time-based fake RSSI variation
- Updated README with accurate codec specifications
- More honest version history and capability claims

## Download

- **APK**: `bluetooth-codec-checker-v1.4.apk` (7.2 MB)
- **Version Code**: 5
- **Min Android**: 8.0 (API 26)
- **Target Android**: 14 (API 34)

## Installation

```bash
adb install bluetooth-codec-checker-v1.4.apk
```

## What's Next

Future versions will focus on:
- Improved real codec detection methods
- Better Android 14+ compatibility
- Enhanced device database accuracy
- More reliable RSSI measurement techniques

---

**Note**: This version prioritizes accuracy over flashy features. If you don't see signal strength values, it means the app couldn't get real data rather than showing fake numbers.
