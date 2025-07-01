# VCallkit Plugin

A comprehensive Flutter plugin for Android VoIP call management using the native Telecom framework. This plugin enables you to display native incoming call UI, handle call actions, and manage call states with WhatsApp-like functionality.

## Features

- 📱 **Native Incoming Call UI** - Display full-screen incoming calls on lock screen and heads-up notifications when unlocked
- 🎥 **Video & Voice Calls** - Support for both audio and video call types
- 🔧 **Complete Call Control** - Answer, reject, end, mute, hold, and DTMF controls
- 📡 **Real-time Events** - Stream-based event system for call state changes
- 🚫 **Non-Dismissible Ongoing Calls** - Ongoing call notifications cannot be swiped away, ensuring call persistence
- 🔐 **Permission Management** - Built-in permission handling for Android VoIP requirements
- 🎨 **Highly Customizable** - Flexible call data structure with custom metadata support
- 📊 **Type Safety** - Comprehensive Dart models for all call data and events
- ⚡ **Performance Optimized** - Efficient native Android implementation using Kotlin

## Platform Support

| Platform | Support                   |
| -------- | ------------------------- |
| Android  | ✅ Android 6.0+ (API 23+) |
| iOS      | ❌ Coming Soon            |

## Installation

Add this to your package's `pubspec.yaml` file:

```yaml
dependencies:
  v_callkit_plugin: ^0.0.1
```

Then run:

```bash
flutter pub get
```

## Android Setup

### 1. Update Android Manifest

Add the following permissions and service declaration to your `android/app/src/main/AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.MANAGE_OWN_CALLS" />
<uses-permission android:name="android.permission.CALL_PHONE" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
<uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT" />
```

### 2. Minimum SDK Version

Ensure your `android/app/build.gradle` has a minimum SDK version of 23:

```gradle
android {
    compileSdkVersion 34

    defaultConfig {
        minSdkVersion 23
        targetSdkVersion 34
    }
}
```

### 3. Request Permissions

The `MANAGE_OWN_CALLS` permission requires user approval through system settings. The plugin provides methods to check and request permissions.

## Quick Start

### 1. Initialize the Plugin

```dart
import 'package:v_callkit_plugin/v_callkit_plugin.dart';

class CallService {
  final VCallkitPlugin _callkit = VCallkitPlugin();

  void initialize() {
    _callkit.initialize();
    _setupCallListeners();
  }

  void _setupCallListeners() {
    // Listen to call events
    _callkit.onCallAnswered.listen((event) {
      print('Call answered: ${event.callId}');
      // Navigate to call screen or start WebRTC connection
    });

    _callkit.onCallRejected.listen((event) {
      print('Call rejected: ${event.callId}');
      // Handle call rejection
    });

    _callkit.onCallEnded.listen((event) {
      print('Call ended: ${event.callId}');
      // Clean up call resources
    });
  }
}
```

### 2. Show Incoming Call

```dart
Future<void> showIncomingCall() async {
  // Check permissions first
  if (!await _callkit.hasPermissions()) {
    await _callkit.requestPermissions();
    return;
  }

  // Show incoming voice call
  await _callkit.showIncomingVoiceCall(
    callerName: 'John Doe',
    callerNumber: '+1234567890',
    callerAvatar: 'https://example.com/avatar.jpg',
    extra: {
      'roomId': 'room-123',
      'callType': 'voice',
      'priority': 'high',
    },
  );
}
```

### 3. Show Incoming Video Call

```dart
Future<void> showIncomingVideoCall() async {
  await _callkit.showIncomingVideoCall(
    callerName: 'Jane Smith',
    callerNumber: '+1987654321',
    callerAvatar: 'https://example.com/jane.jpg',
    callId: 'custom-call-id', // Optional custom ID
    extra: {
      'roomId': 'video-room-456',
      'quality': 'hd',
    },
  );
}
```

## Advanced Usage

### Custom Call Data

For maximum flexibility, use the `CallData` model:

```dart
import 'package:v_callkit_plugin/models/call_data.dart';

final callData = CallData(
  id: 'unique-call-id',
  callerName: 'Conference Room 1',
  callerNumber: '+1555000123',
  callerAvatar: 'https://company.com/meeting-avatar.jpg',
  isVideoCall: true,
  extra: {
    'meetingId': 'meeting-123',
    'participants': ['user1', 'user2', 'user3'],
    'duration': 3600, // 1 hour
    'encrypted': true,
    'server': 'us-west-1',
  },
);

await _callkit.showIncomingCall(callData);
```

### Event Handling

Listen to specific call events:

```dart
// Listen to all events for a specific call
_callkit.listenToCall('call-id-123').listen((event) {
  print('Event for call-123: ${event.action}');
});

// Listen to call state changes
_callkit.onCallStateChanged.listen((event) {
  switch (event.state) {
    case CallState.ringing:
      print('Call is ringing');
      break;
    case CallState.active:
      print('Call is active');
      // Start WebRTC or audio/video streams
      break;
    case CallState.holding:
      print('Call on hold');
      break;
    case CallState.disconnected:
      print('Call disconnected');
      break;
  }
});

// Listen to mute events
_callkit.onCallMute.listen((event) {
  print('Mute state: ${event.isMuted}');
  // Update UI accordingly
});

// Listen to hold events
_callkit.onCallHold.listen((event) {
  print('Hold state: ${event.isOnHold}');
  // Pause/resume media streams
});
```

### Call Control

```dart
class CallController {
  final VCallkitPlugin _callkit = VCallkitPlugin();

  // End current call
  Future<void> endCall([String? callId]) async {
    await _callkit.endCall(callId);
  }

  // Answer incoming call programmatically
  Future<void> answerCall([String? callId]) async {
    await _callkit.answerCall(callId);
  }

  // Reject incoming call programmatically
  Future<void> rejectCall([String? callId]) async {
    await _callkit.rejectCall(callId);
  }

  // Mute/unmute call
  Future<void> toggleMute(bool isMuted, [String? callId]) async {
    await _callkit.muteCall(isMuted, callId);
  }

  // Hold/unhold call
  Future<void> toggleHold(bool isOnHold, [String? callId]) async {
    await _callkit.holdCall(isOnHold, callId);
  }

  // Check if there's an active call
  Future<bool> hasActiveCall() async {
    return await _callkit.isCallActive();
  }

  // Get active call information
  Future<CallData?> getActiveCall() async {
    return await _callkit.getActiveCallData();
  }
}
```

## Integration with Push Notifications

The plugin works perfectly with push notifications. Here's how to integrate with Firebase Cloud Messaging (FCM):

```dart
import 'package:firebase_messaging/firebase_messaging.dart';

class NotificationService {
  final VCallkitPlugin _callkit = VCallkitPlugin();

  void initialize() {
    _callkit.initialize();

    // Handle foreground messages
    FirebaseMessaging.onMessage.listen(_handleForegroundMessage);

    // Handle background messages
    FirebaseMessaging.onBackgroundMessage(_handleBackgroundMessage);

    // Handle app opened from notification
    FirebaseMessaging.onMessageOpenedApp.listen(_handleNotificationTap);
  }

  void _handleForegroundMessage(RemoteMessage message) {
    final data = message.data;
    if (data['type'] == 'incoming_call') {
      _showIncomingCallFromNotification(data);
    }
  }

  static Future<void> _handleBackgroundMessage(RemoteMessage message) async {
    final data = message.data;
    if (data['type'] == 'incoming_call') {
      final callkit = VCallkitPlugin();
      await callkit.showIncomingVoiceCall(
        callerName: data['caller_name'],
        callerNumber: data['caller_number'],
        callerAvatar: data['caller_avatar'],
        extra: {
          'roomId': data['room_id'],
          'callId': data['call_id'],
        },
      );
    }
  }

  void _showIncomingCallFromNotification(Map<String, dynamic> data) async {
    await _callkit.showIncomingCall(CallData.fromMap(data));
  }
}
```

## Data Models

### CallData

```dart
class CallData {
  final String id;              // Unique call identifier
  final String callerName;      // Display name
  final String callerNumber;    // Phone number
  final String? callerAvatar;   // Avatar URL/path
  final bool isVideoCall;       // Video call flag
  final Map<String, dynamic> extra; // Custom metadata
}
```

### CallEvent Types

- `CallAnsweredEvent` - Call was answered
- `CallRejectedEvent` - Call was rejected
- `CallEndedEvent` - Call ended
- `CallHoldEvent` - Call put on hold/resumed
- `CallMuteEvent` - Call muted/unmuted
- `CallStateChangedEvent` - Call state changed
- `CallDtmfEvent` - DTMF tone pressed

### CallState Enum

```dart
enum CallState {
  initializing,
  newCall,
  ringing,
  dialing,
  active,
  holding,
  disconnected,
  unknown,
}
```

## Notification Behavior

### Non-Dismissible Ongoing Call Notifications

According to the [Android Developer documentation for CallStyle notifications](https://developer.android.com/develop/ui/views/notifications/call-style), this plugin implements the official CallStyle notification template with proper non-dismissible behavior:

- **Android 14+ (API 34+)**: Uses `CallStyle.forOngoingCall()` with `FLAG_ONGOING_EVENT` for true non-dismissible notifications
- **Android 12-13 (API 31-33)**: Uses `CallStyle.forOngoingCall()` with foreground service association for high priority
- **Android 8-11 (API 26-30)**: Uses legacy notifications with `setColorized(true)` and foreground service for high ranking
- **System Compliance**: Follows Android's official CallStyle notification template requirements
- **Automatic Foreground Service**: Ensures notifications cannot be dismissed by users

#### How it Works

1. **Incoming Call Notifications**: Dismissible via answer/decline actions using standard call notification behavior
2. **Ongoing Call Notifications**: Automatically become non-dismissible using Android's CallStyle template when call is answered
3. **CallStyle Implementation**: Uses `Notification.CallStyle.forOngoingCall()` for Android 12+ with proper hang-up action
4. **Foreground Service Association**: All ongoing calls run as foreground services to ensure notification persistence
5. **Call Duration Timer**: Shows real-time call duration that updates every second with chronometer
6. **Automatic Cleanup**: Notifications are automatically removed when foreground service stops

#### Notification States

```dart
// Before answering - dismissible
await _callkit.showIncomingCall(callData);

// After answering - non-dismissible until call ends
_callkit.onCallAnswered.listen((event) {
  // Ongoing notification is now non-dismissible using CallStyle
  // User can only remove it by ending the call via hang-up action
});

// Call ended - notification automatically removed
_callkit.onCallEnded.listen((event) {
  // Foreground service stops and notification is automatically dismissed
});
```

#### Technical Implementation by Android Version

| Android Version       | Notification Type            | Non-Dismissible Method                         |
| --------------------- | ---------------------------- | ---------------------------------------------- |
| **14+ (API 34+)**     | `CallStyle.forOngoingCall()` | `FLAG_ONGOING_EVENT` + `setOngoing(true)`      |
| **12-13 (API 31-33)** | `CallStyle.forOngoingCall()` | Foreground service + `FLAG_FOREGROUND_SERVICE` |
| **8-11 (API 26-30)**  | Legacy with actions          | Foreground service + `setColorized(true)`      |
| **7 and below**       | Basic persistent             | `FLAG_NO_CLEAR` + `FLAG_ONGOING_EVENT`         |

#### Key Features

- **CallStyle Template**: Uses Android's official call notification template for consistency
- **Automatic Actions**: System-provided hang-up button with appropriate icons
- **High Priority Ranking**: Notifications appear at the top of the notification shade
- **Cross-Device Forwarding**: Compatible with Android's call forwarding to other devices
- **Duration Display**: Real-time call timer with proper chronometer formatting

## Best Practices

### 1. Permission Handling

Always check permissions before showing calls:

```dart
Future<bool> ensurePermissions() async {
  if (await _callkit.hasPermissions()) {
    return true;
  }

  // Show explanation to user
  showPermissionDialog();

  // Request permissions (this opens system settings)
  return await _callkit.requestPermissions();
}
```

### 2. Resource Management

```dart
class CallManager {
  VCallkitPlugin? _callkit;
  StreamSubscription? _eventSubscription;

  void initialize() {
    _callkit = VCallkitPlugin();
    _callkit!.initialize();

    _eventSubscription = _callkit!.onCallEvent.listen(_handleCallEvent);
  }

  void dispose() {
    _eventSubscription?.cancel();
    _callkit?.dispose();
  }
}
```

### 3. Error Handling

```dart
Future<void> showCallSafely(CallData callData) async {
  try {
    final success = await _callkit.showIncomingCall(callData);
    if (!success) {
      // Handle failure (permissions, etc.)
      _showErrorDialog('Unable to show incoming call');
    }
  } on PlatformException catch (e) {
    print('Platform error: ${e.message}');
    _handlePlatformError(e);
  } catch (e) {
    print('Unexpected error: $e');
    _showErrorDialog('An unexpected error occurred');
  }
}
```

## Troubleshooting

### Common Issues

1. **Permissions not granted**

   - The `MANAGE_OWN_CALLS` permission must be granted in system settings
   - Use `hasPermissions()` and `requestPermissions()` methods

2. **Call UI not showing**

   - Ensure minimum SDK version is 23+
   - Check that all required permissions are in AndroidManifest.xml
   - Verify device is not in Do Not Disturb mode

3. **Events not received**
   - Make sure `initialize()` is called before using the plugin
   - Check that event listeners are set up before showing calls

### Testing

Run the example app to test all functionality:

```bash
cd example
flutter run
```

The example app provides a comprehensive test interface for all plugin features.

## Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

- 📫 [Report Issues](https://github.com/your-repo/v_callkit_plugin/issues)
- 💬 [Discussions](https://github.com/your-repo/v_callkit_plugin/discussions)
- 📖 [Documentation](https://github.com/your-repo/v_callkit_plugin/wiki)

---

Made with ❤️ for the Flutter community
