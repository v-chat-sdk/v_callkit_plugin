# Hangup Notification Implementation Guide

## Overview

The VCallKit plugin now includes a persistent hangup notification feature that provides a non-dismissible notification with a hangup button after answering a call. This implementation follows Android's best practices for foreground services and ensures compatibility across all Android versions (API 21+) and device manufacturers.

## Features

✅ **Persistent Non-Dismissible Notification** - Cannot be swiped away by users  
✅ **Live Call Duration Timer** - Updates every second with call progress  
✅ **Single Hangup Action** - Clean, simple interface  
✅ **Foreground Service Integration** - Survives app backgrounding and task killing  
✅ **Manufacturer Compatibility** - Works on Xiaomi, Huawei, Oppo, etc.  
✅ **Android 14+ Support** - Uses proper `phoneCall` foreground service type  
✅ **Fallback Mechanisms** - Graceful degradation if permissions are missing  
✅ **Memory Efficient** - Automatic cleanup and resource management

## Implementation

### 1. Android Native Components

#### HangupNotificationService

- **File**: `android/src/main/kotlin/.../HangupNotificationService.kt`
- **Purpose**: Dedicated foreground service for hangup notifications
- **Features**: Duration timer, notification channel management, task removal handling

#### VCallkitPlugin Updates

- **Methods Added**: `showHangupNotification`, `hideHangupNotification`, `updateHangupNotification`
- **Integration**: Seamless integration with existing call flow

#### Android Manifest Updates

- **Service Declaration**: `HangupNotificationService` with `phoneCall` foreground service type
- **Permissions**: All required permissions already included

### 2. Flutter Implementation

#### Platform Interface

```dart
abstract class VCallkitPluginPlatform {
  /// Show persistent hangup notification
  Future<bool> showHangupNotification(Map<String, dynamic> callData);

  /// Hide hangup notification
  Future<bool> hideHangupNotification();

  /// Update hangup notification with new call data
  Future<bool> updateHangupNotification(Map<String, dynamic> callData);
}
```

#### Main Plugin Class

```dart
class VCallkitPlugin {
  /// Show persistent hangup notification
  Future<bool> showHangupNotification(CallData callData);

  /// Hide hangup notification
  Future<bool> hideHangupNotification();

  /// Update hangup notification with new call data
  Future<bool> updateHangupNotification(CallData callData);
}
```

## Usage Examples

### Basic Usage

```dart
final VCallkitPlugin _plugin = VCallkitPlugin();

// Show hangup notification after answering a call
final callData = CallData(
  id: 'call_123',
  callerName: 'John Doe',
  callerNumber: '+1234567890',
  isVideoCall: false,
);

// Show the persistent hangup notification
await _plugin.showHangupNotification(callData);

// Later, when call ends
await _plugin.hideHangupNotification();
```

### Integration with Call Flow

```dart
class CallManager {
  final VCallkitPlugin _plugin = VCallkitPlugin();

  void _setupCallListeners() {
    _plugin.onCallAnswered.listen((event) async {
      // Call was answered, show hangup notification
      final callData = await _plugin.getActiveCallData();
      if (callData != null) {
        await _plugin.showHangupNotification(callData);
      }
    });

    _plugin.onCallEnded.listen((event) async {
      // Call ended, hide hangup notification
      await _plugin.hideHangupNotification();
    });
  }
}
```

### Advanced Usage with Updates

```dart
class OngoingCallScreen extends StatefulWidget {
  @override
  _OngoingCallScreenState createState() => _OngoingCallScreenState();
}

class _OngoingCallScreenState extends State<OngoingCallScreen> {
  final VCallkitPlugin _plugin = VCallkitPlugin();
  Timer? _updateTimer;

  @override
  void initState() {
    super.initState();
    _startHangupNotification();
    _startUpdateTimer();
  }

  void _startHangupNotification() async {
    final callData = CallData(
      id: widget.callId,
      callerName: widget.callerName,
      callerNumber: widget.callerNumber,
      isVideoCall: widget.isVideoCall,
    );

    await _plugin.showHangupNotification(callData);
  }

  void _startUpdateTimer() {
    _updateTimer = Timer.periodic(Duration(seconds: 30), (timer) {
      // Update notification with any changes
      _updateNotification();
    });
  }

  void _updateNotification() async {
    // Example: Update caller info if it changes
    final updatedCallData = CallData(
      id: widget.callId,
      callerName: _getCurrentCallerName(), // Your logic here
      callerNumber: widget.callerNumber,
      isVideoCall: _isCurrentlyVideoCall(), // Your logic here
    );

    await _plugin.updateHangupNotification(updatedCallData);
  }

  @override
  void dispose() {
    _updateTimer?.cancel();
    _plugin.hideHangupNotification();
    super.dispose();
  }
}
```

## Testing

### Demo Widget

The plugin includes a comprehensive demo widget (`HangupNotificationDemoWidget`) that demonstrates all functionality:

1. **Show Notification** - Creates a test hangup notification
2. **Hide Notification** - Programmatically dismisses the notification
3. **Update Notification** - Updates with new caller information
4. **Status Tracking** - Shows current notification state

### Manual Testing Steps

1. **Build and Install**:

   ```bash
   cd example
   flutter build apk --debug
   flutter install
   ```

2. **Test Basic Functionality**:

   - Open the demo app
   - Scroll to "Hangup Notification Demo"
   - Tap "Show Hangup Notification"
   - Check notification panel
   - Try swiping notification (should not dismiss)
   - Tap "Hangup" button in notification

3. **Test Persistence**:

   - Show hangup notification
   - Background the app
   - Check notification still visible
   - Kill the app from recent apps
   - Check notification still visible

4. **Test Update Functionality**:
   - Show hangup notification
   - Tap "Update with New Caller Info"
   - Check notification shows updated information

## Architecture Details

### Notification Channel Configuration

```kotlin
val channel = NotificationChannel(
    HANGUP_CHANNEL_ID,
    "Call Hangup",
    NotificationManager.IMPORTANCE_DEFAULT
).apply {
    description = "Persistent notification with hangup button for active calls"
    setShowBadge(true)
    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
    enableVibration(false)
    enableLights(false)
    setSound(null, null) // No sound for hangup notifications
}
```

### Foreground Service Type

- **Type**: `phoneCall` (Android 14+ requirement)
- **Fallback**: Generic foreground service for older versions
- **Permissions**: `FOREGROUND_SERVICE_PHONE_CALL`, `MANAGE_OWN_CALLS`

### Non-Dismissible Implementation

```kotlin
val notification = NotificationCompat.Builder(context, HANGUP_CHANNEL_ID)
    .setOngoing(true)
    .setAutoCancel(false)
    // ... other configurations
    .build().apply {
        // Make notification non-dismissible
        flags = flags or Notification.FLAG_NO_CLEAR or Notification.FLAG_ONGOING_EVENT
    }
```

### Duration Timer

- **Update Frequency**: Every 1 second
- **Format**: MM:SS (under 1 hour) or HH:MM:SS (1 hour+)
- **Automatic Cleanup**: Timer stops when service is destroyed

## Compatibility

### Android Versions

- ✅ **Android 5.0+ (API 21+)** - Full compatibility
- ✅ **Android 8.0+ (API 26+)** - Foreground service with notification channels
- ✅ **Android 12+ (API 31+)** - Enhanced CallStyle support
- ✅ **Android 14+ (API 34+)** - Proper foreground service types

### Device Manufacturers

- ✅ **Xiaomi/MIUI** - Works with battery optimization handling
- ✅ **Huawei/EMUI** - Survives aggressive task killing
- ✅ **Oppo/ColorOS** - Proper notification persistence
- ✅ **Samsung/OneUI** - Full compatibility
- ✅ **OnePlus/OxygenOS** - All features supported
- ✅ **Google Pixel** - Reference implementation

### Permissions Required

All permissions are already included in the plugin manifest:

- `FOREGROUND_SERVICE`
- `FOREGROUND_SERVICE_PHONE_CALL` (Android 14+)
- `POST_NOTIFICATIONS` (Android 13+)
- `WAKE_LOCK`

## Troubleshooting

### Common Issues

1. **Notification Not Showing**

   - Check if notifications are enabled for the app
   - Verify foreground service permissions
   - Ensure battery optimization is disabled (Xiaomi/Huawei)

2. **Notification Dismissible**

   - Check if the service is properly running in foreground
   - Verify notification flags are set correctly
   - Check Android version compatibility

3. **Service Killed on Task Removal**
   - Enable autostart for the app (Xiaomi)
   - Disable battery optimization
   - Check manufacturer-specific settings

### Debug Logging

Enable debug logging to troubleshoot:

```kotlin
Log.d("HangupNotificationService", "Debug message here")
```

### Verification Commands

```bash
# Check if service is running
adb shell dumpsys activity services | grep HangupNotificationService

# Check notifications
adb shell dumpsys notification

# Check foreground services
adb shell dumpsys activity services | grep "foreground"
```

## Best Practices

1. **Show After Call Answered**

   - Always show hangup notification immediately after call is answered
   - Don't delay the notification appearance

2. **Cleanup on Call End**

   - Always hide hangup notification when call ends
   - Clean up in all exit paths (normal end, error, etc.)

3. **Update Sparingly**

   - Only update notification when necessary
   - Avoid frequent updates that might cause performance issues

4. **Handle Permissions Gracefully**

   - Check permissions before showing notification
   - Provide fallback mechanisms for missing permissions

5. **Test on Real Devices**
   - Always test on physical devices, especially Xiaomi/Huawei
   - Test different Android versions and manufacturers

## API Reference

### showHangupNotification(CallData callData)

Shows a persistent hangup notification with call information.

**Parameters:**

- `callData`: Call information to display in notification

**Returns:** `Future<bool>` - true if notification was shown successfully

### hideHangupNotification()

Hides the persistent hangup notification.

**Returns:** `Future<bool>` - true if notification was hidden successfully

### updateHangupNotification(CallData callData)

Updates the existing hangup notification with new call information.

**Parameters:**

- `callData`: Updated call information

**Returns:** `Future<bool>` - true if notification was updated successfully

## Conclusion

The hangup notification implementation provides a robust, cross-platform solution for persistent call notifications. It follows Android best practices, handles manufacturer-specific quirks, and provides a clean API for Flutter developers.

The implementation is production-ready and has been tested across multiple device manufacturers and Android versions.
