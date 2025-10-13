import re

# Read the BluetoothManager.kt file
with open('app/src/main/java/com/bluetoothcodec/checker/BluetoothManager.kt', 'r') as f:
    content = f.read()

# Find all codec detection methods
methods = []

# Method patterns to search for
patterns = [
    (r'getActiveStreamCodec\(\)', 'Active Stream Analysis'),
    (r'getDeveloperOptionsCodec\(\)', 'Developer Options'),
    (r'getOSLevelCodec\(', 'OS-Level Detection'),
    (r'getCodecFromAudioRouting\(\)', 'Audio Routing'),
    (r'getCurrentCodecFromSystemProps\(\)', 'System Properties'),
    (r'detectAptXCodec\(', 'aptX Detection'),
    (r'detectCodecFromMediaMetrics\(', 'Media Metrics'),
    (r'detectCodecFromBluetoothHci\(', 'Bluetooth HCI'),
    (r'detectCodecFromVendorProperties\(', 'Vendor Properties'),
    (r'detectCodecFromAudioPolicy\(', 'Audio Policy'),
    (r'detectCodecFromBluetoothStack\(', 'Bluetooth Stack'),
    (r'detectCodecFromKernelLogs\(', 'Kernel Logs'),
]

print("=== BLUETOOTH CODEC DETECTION METHODS ANALYSIS ===\n")

for i, (pattern, name) in enumerate(patterns, 1):
    if re.search(pattern, content):
        print(f"Method {i}: {name}")
        # Find the method implementation
        method_match = re.search(rf'(private fun {pattern.replace("(", r"\(").replace(")", r"\)")}.*?(?=private fun|\Z))', content, re.DOTALL)
        if method_match:
            method_code = method_match.group(1)
            # Count lines
            lines = len(method_code.split('\n'))
            print(f"  - Implementation: {lines} lines")
            
            # Check for try-catch blocks
            if 'try {' in method_code:
                print("  - Error handling: Yes (try-catch)")
            else:
                print("  - Error handling: No")
            
            # Check for logging
            if 'android.util.Log' in method_code:
                print("  - Logging: Yes")
            else:
                print("  - Logging: No")
            
            # Check return types
            if 'return null' in method_code:
                print("  - Fallback: Returns null on failure")
            elif 'return "' in method_code:
                print("  - Fallback: Returns string on failure")
            
        print()

print("=== DETECTION ORDER (Priority) ===")
order_text = """
Based on getCurrentCodec() method:
1. Active Stream Analysis (during playback)
2. Developer Options (user configured)
3. OS-Level Detection (runtime)
4. Audio Routing (OS level)
5. System Properties (fallback)
6. Device Estimation (based on device name)
"""
print(order_text)
