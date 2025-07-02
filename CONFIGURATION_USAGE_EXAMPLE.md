# VCallkit Configuration Usage Examples

This guide shows how to use the new strongly-typed configuration models instead of raw Maps.

## Basic Usage

### Simple Incoming Call (No Configuration)

```dart
final callData = CallData(
  id: 'unique_call_id',
  callerName: 'John Doe',
  callerNumber: '+1234567890',
  callerAvatar: 'https://example.com/avatar.jpg',
  isVideoCall: false,
);

// Show incoming call with default configuration
await vCallkitPlugin.showIncomingCall(callData);
```

### Incoming Call with Custom Configuration

```dart
final callData = CallData(
  id: 'unique_call_id',
  callerName: 'John Doe',
  callerNumber: '+1234567890',
  callerAvatar: 'https://example.com/avatar.jpg',
  isVideoCall: true,
);

final configuration = VCallkitCallConfiguration(
  // Visual Theme
  backgroundColor: '#1a1a1a',
  primaryColor: '#007AFF',
  accentColor: '#34C759',
  textColor: '#FFFFFF',

  // Call Settings
  showCallerNumber: true,
  showCallDuration: true,
  callTimeoutSeconds: 45,

  // Audio Settings
  enableVibration: true,
  enableRingtone: true,

  // Text Translations
  incomingCallTitle: 'Incoming Video Call',
  answerButtonText: 'Accept',
  declineButtonText: 'Decline',
);

// Show incoming call with custom configuration
await vCallkitPlugin.showIncomingCallWithConfiguration(
  callData: callData,
  configuration: configuration,
);
```

## Convenience Methods

### Voice Call with Configuration

```dart
await vCallkitPlugin.showIncomingVoiceCall(
  callerName: 'Alice Smith',
  callerNumber: '+1987654321',
  callerAvatar: 'https://example.com/alice.jpg',
  callConfiguration: VCallkitCallConfiguration.darkTheme,
);
```

### Video Call with Configuration

```dart
await vCallkitPlugin.showIncomingVideoCall(
  callerName: 'Bob Wilson',
  callerNumber: '+1555123456',
  callerAvatar: 'https://example.com/bob.jpg',
  callConfiguration: VCallkitCallConfiguration.lightTheme,
);
```

### Full Configuration Example

```dart
await vCallkitPlugin.showIncomingCallWithFullConfig(
  callerName: 'Sarah Johnson',
  callerNumber: '+1444567890',
  callerAvatar: 'https://example.com/sarah.jpg',
  isVideoCall: true,
  extra: {
    'room_id': 'room_123',
    'priority': 'high',
  },
  callConfiguration: VCallkitCallConfiguration(
    backgroundColor: '#2C2C2E',
    primaryColor: '#FF9500',
    showCallDuration: true,
    callTimeoutSeconds: 60,
    incomingCallTitle: 'Business Call',
    answerButtonText: 'Join Meeting',
  ),
);
```

## Pre-built Configurations

### Dark Theme

```dart
await vCallkitPlugin.showIncomingCallWithConfiguration(
  callData: callData,
  configuration: VCallkitCallConfiguration.darkTheme,
);
```

### Light Theme

```dart
await vCallkitPlugin.showIncomingCallWithConfiguration(
  callData: callData,
  configuration: VCallkitCallConfiguration.lightTheme,
);
```

### Default Configuration

```dart
await vCallkitPlugin.showIncomingCallWithConfiguration(
  callData: callData,
  configuration: VCallkitCallConfiguration.defaultConfig,
);
```

## Firebase Messaging Integration

### Background Message Handler

```dart
@pragma('vm:entry-point')
Future<void> firebaseMessagingBackgroundHandler(RemoteMessage message) async {
  final vCallkitPlugin = VCallkitPlugin();
  vCallkitPlugin.initialize();

  // Create call data from Firebase message
  final callData = CallData(
    id: message.data['call_id'] ?? 'fcm_call_${DateTime.now().millisecondsSinceEpoch}',
    callerName: message.data['caller_name'] ?? 'Unknown Caller',
    callerNumber: message.data['caller_number'] ?? 'Unknown Number',
    callerAvatar: message.data['caller_avatar'],
    isVideoCall: message.data['is_video'] == 'true',
    extra: {
      'source': 'firebase',
      'message_id': message.messageId,
    },
  );

  // Custom configuration based on message data
  final configuration = VCallkitCallConfiguration(
    backgroundColor: message.data['theme'] == 'dark' ? '#1a1a1a' : '#FFFFFF',
    primaryColor: message.data['accent_color'] ?? '#007AFF',
    callTimeoutSeconds: int.tryParse(message.data['timeout'] ?? '30') ?? 30,
    showCallerNumber: message.data['show_number'] != 'false',
  );

  // Show incoming call
  await vCallkitPlugin.showIncomingCallWithConfiguration(
    callData: callData,
    configuration: configuration,
  );
}
```

## Migration from Map-based Configuration

### Old Way (Deprecated)

```dart
// ❌ Old deprecated approach
await vCallkitPlugin.showIncomingCallWithConfig({
  'callData': {
    'id': 'call_123',
    'callerName': 'John Doe',
    'callerNumber': '+1234567890',
    'isVideoCall': true,
  },
  'config': {
    'backgroundColor': '#1a1a1a',
    'primaryColor': '#007AFF',
    'showCallerNumber': true,
    'callTimeoutSeconds': 45,
  },
});
```

### New Way (Recommended)

```dart
// ✅ New strongly-typed approach
final callData = CallData(
  id: 'call_123',
  callerName: 'John Doe',
  callerNumber: '+1234567890',
  isVideoCall: true,
);

final configuration = VCallkitCallConfiguration(
  backgroundColor: '#1a1a1a',
  primaryColor: '#007AFF',
  showCallerNumber: true,
  callTimeoutSeconds: 45,
);

await vCallkitPlugin.showIncomingCallWithConfiguration(
  callData: callData,
  configuration: configuration,
);
```

## Configuration Options Reference

### Visual Theme Properties

```dart
VCallkitCallConfiguration(
  backgroundColor: '#FFFFFF',        // Background color
  primaryColor: '#007AFF',          // Primary accent color
  accentColor: '#34C759',           // Secondary accent color
  textColor: '#000000',             // Text color
  buttonBackgroundColor: '#F2F2F7', // Button background
  buttonTextColor: '#000000',       // Button text color
  iconColor: '#000000',             // Icon color
  borderRadius: 12.0,               // Border radius for elements
  buttonSize: 64.0,                 // Button size
  fontFamily: 'SF Pro Display',     // Font family
  fontSize: 16.0,                   // Font size
  backgroundImage: 'image_url',     // Background image URL
  backgroundOpacity: 0.8,           // Background opacity
)
```

### Text Translations

```dart
VCallkitCallConfiguration(
  incomingCallTitle: 'Incoming Call',
  answerButtonText: 'Answer',
  declineButtonText: 'Decline',
  hangupButtonText: 'Hang Up',
  audioCallText: 'Audio Call',
  videoCallText: 'Video Call',
  callEndedText: 'Call Ended',
  missedCallText: 'Missed Call',
  callingText: 'Calling...',
  connectingText: 'Connecting...',
)
```

### Call Settings

```dart
VCallkitCallConfiguration(
  showCallerNumber: true,           // Show caller's phone number
  showCallDuration: true,           // Show call duration timer
  callTimeoutSeconds: 30,           // Call timeout in seconds
)
```

### Audio Settings

```dart
VCallkitCallConfiguration(
  enableVibration: true,            // Enable vibration
  enableRingtone: true,             // Enable ringtone
)
```

### Additional Settings

```dart
VCallkitCallConfiguration(
  enableCallTimeout: true,          // Enable automatic call timeout
  useFullScreenCallUI: true,        // Use full screen UI
  showCallType: true,               // Show call type (audio/video)
  enableTapToReturnToCall: true,    // Enable tap to return to call
  hangupNotificationPriority: 0,   // Hangup notification priority
  use24HourFormat: true,            // Use 24-hour time format
  durationFormat: 'mm:ss',          // Duration format
)
```

### Accessibility

```dart
VCallkitCallConfiguration(
  answerButtonContentDescription: 'Answer call button',
  declineButtonContentDescription: 'Decline call button',
  hangupButtonContentDescription: 'Hangup call button',
)
```

## Benefits of Strongly-Typed Configuration

1. **Type Safety**: Compile-time checking prevents runtime errors
2. **Auto-completion**: IDE provides better code completion
3. **Documentation**: Clear parameter names and types
4. **Maintainability**: Easier to refactor and update
5. **Default Values**: Sensible defaults for all optional parameters
6. **Validation**: Built-in type conversion and validation

## Best Practices

1. **Use pre-built themes** when possible (`darkTheme`, `lightTheme`)
2. **Create reusable configurations** for your app's brand
3. **Validate user input** when creating configurations dynamically
4. **Use null safety** effectively with optional parameters
5. **Test configurations** across different devices and screen sizes

This new approach provides better developer experience while maintaining backward compatibility with the existing Map-based API.
