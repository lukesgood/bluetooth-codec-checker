# Bluetooth Codec Checker (BCC)

An Android app that shows the Bluetooth audio devices connected to your phone, the codec
they are using when Android exposes it, and every Bluetooth device the radio can hear
nearby.

## What it does

### Audio tab
- Lists connected A2DP devices with battery level and signal strength when available.
- Shows the active codec when the platform reports it, and **"Codec unknown"** when it does
  not. The app never falls back to a guess.
- Charts the codecs the phone is expected to support by latency and quality.
- Summarises how busy the 2.4 GHz surroundings are from the devices currently in range.

### Scanner tab
- Continuous BLE scan listing every device in range with name, address, manufacturer
  (from the advertised Bluetooth SIG company ID) and RSSI.
- **Search classic devices** runs a ~12 second classic (BR/EDR) inquiry on demand to find
  speakers and headsets in pairing mode. It can briefly interrupt Bluetooth audio, so it is
  never started automatically.
- Filters for BLE / Classic / named devices, sorted by signal or name.
- Tap a device for a live signal view (RSSI, trend, history graph) to locate it by walking
  around.

## Limits worth knowing

- **Active codec.** Android has no public API for the A2DP codec in use.
  `BluetoothA2dp.getCodecStatus` is a system API, so on most phones a regular app cannot
  read it. Developer options → *Bluetooth audio codec* remains the authoritative place to
  check.
- **Phone-supported codecs** are inferred from system properties and the device model.
- **Headphone-supported codecs** come from a bundled name database, not from the device.
- **Addresses rotate.** Phones, watches and trackers change their BLE address every few
  minutes, so one device can appear more than once and cannot be tracked over time.
- **RSSI is not distance.** Walls, bodies and antenna orientation move it as much as
  distance does. The app shows coarse labels, not metres.
- Scanning runs only while the app is on screen.

## Codec reference

| Codec | Typical bitrate | Typical latency |
|-------|-----------------|-----------------|
| SBC | 328 kbps | 40-200 ms |
| AAC | 256 kbps | 40-150 ms |
| aptX | 352 kbps | ~40 ms |
| aptX HD | 576 kbps | ~130 ms |
| aptX Adaptive | 279-420 kbps | 50-80 ms |
| LDAC | 330-990 kbps | 150-200 ms |
| LC3 | 160 kbps | 20-30 ms |

These are nominal figures for each codec, not measurements from your connection.

## Requirements

- Android 8.0+ (API 26+), built against API 36
- Bluetooth permissions (Connect, Scan) and Location permission
- JDK 17+ to build

## Build

```bash
git clone https://github.com/lukesgood/bluetooth-codec-checker.git
cd bluetooth-codec-checker
./gradlew assembleDebug
```

### Release signing
The keystore and its passwords are not stored in this repository. To build a signed
release, copy `keystore.properties.example` to `keystore.properties` (git-ignored) and
fill it in, or set `BCC_STORE_FILE`, `BCC_STORE_PASSWORD`, `BCC_KEY_ALIAS` and
`BCC_KEY_PASSWORD`. Without them `./gradlew assembleRelease` produces an unsigned APK.

## License

This project is for educational and research purposes. Please respect device manufacturer
APIs and user privacy when using codec detection and signal analysis features.
