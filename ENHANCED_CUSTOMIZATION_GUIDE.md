# Enhanced V CallKit Plugin - Customization & Translation Guide

## 🚀 New Features Overview

The V CallKit Plugin has been significantly enhanced with comprehensive customization, translation support, and improved stream APIs. This guide covers all the new capabilities added to make your call UI fully customizable and translatable.

## 📱 Live Demo

The `CustomizationDemoScreen` in the example app showcases all features with real-time configuration changes, multiple themes, and multi-language support.

## 🎨 Core Enhancement Features

### 1. **Enhanced Plugin API**

#### New Methods Added:

- `showIncomingCallWithConfig()` - Show calls with per-call customization
- `setUIConfiguration()` - Apply global UI settings
- `forceShowHangupNotification()` - Test hangup notifications
- `getCallManagerDebugInfo()` - Get detailed debug information
- `showIncomingCallWithFullConfig()` - Comprehensive call customization

#### Enhanced Stream APIs:

- `onCallConfigurationChanged` - Listen to configuration updates
- `onCallTimerUpdated` - Real-time call duration updates
- `onCallAudioDeviceChanged` - Audio device change notifications
- `onAllEvents` - Combined stream of all events including enhanced ones

### 2. **Theme Customization System**

#### Built-in Themes:

- **Dark Green**: Professional dark theme
- **Light Blue**: Clean light theme
- **Purple**: Modern purple theme
- **Orange**: Vibrant orange theme

#### Theme Configuration:

```dart
final theme = VCallkitPlugin.createTheme(
  backgroundColor: '#1C1C1E',
  accentColor: '#34C759',
  textColor: '#FFFFFF',
  secondaryTextColor: '#8E8E93',
);
```

### 3. **Multi-Language Translation Support**

#### Supported Languages:

- **English** 🇺🇸
- **Spanish** 🇪🇸
- **French** 🇫🇷
- **Arabic** 🇸🇦

#### Translation Configuration:

```dart
final translation = VCallkitPlugin.createTranslation(
  answerButtonText: 'Answer',
  declineButtonText: 'Decline',
  hangupButtonText: 'Hangup',
  incomingVoiceCallText: 'Incoming Voice Call',
  incomingVideoCallText: 'Incoming Video Call',
  callInProgressText: 'Call in Progress',
);
```

### 4. **Advanced Behavior Configuration**

#### Behavior Settings:

```dart
final behavior = VCallkitPlugin.createBehaviorConfig(
  showCallerNumber: true,
  enableVibration: true,
  enableRingtone: true,
  showCallDuration: true,
  callTimeoutSeconds: 60,
  enableCallTimeout: true,
  useFullScreenCallUI: true,
);
```

## 🛠️ Implementation Guide

### Step 1: Initialize Plugin with Enhanced Features

```dart
class MyApp extends StatefulWidget {
  @override
  void initState() {
    super.initState();

    // Initialize the plugin
    _vCallkitPlugin.initialize();

    // Set up enhanced event listeners
    _setupEnhancedEventHandlers();
  }

  void _setupEnhancedEventHandlers() {
    // Listen to timer updates
    _vCallkitPlugin.onCallTimerUpdated.listen((event) {
      print('Call duration: ${event['duration']}');
    });

    // Listen to configuration changes
    _vCallkitPlugin.onCallConfigurationChanged.listen((config) {
      print('Config updated: $config');
    });

    // Listen to all events combined
    _vCallkitPlugin.onAllEvents.listen((event) {
      print('Event received: ${event['type']}');
    });
  }
}
```

### Step 2: Apply Global Configuration

```dart
Future<void> setupGlobalCallConfiguration() async {
  final config = {
    // Theme
    'backgroundColor': '#1C1C1E',
    'accentColor': '#34C759',
    'textColor': '#FFFFFF',
    'secondaryTextColor': '#8E8E93',

    // Translations
    'answerButtonText': 'Answer',
    'declineButtonText': 'Decline',
    'hangupButtonText': 'Hangup',
    'incomingVoiceCallText': 'Incoming Voice Call',
    'incomingVideoCallText': 'Incoming Video Call',
    'callInProgressText': 'Call in Progress',
    'tapToReturnText': 'Tap to return to call',
    'unknownCallerText': 'Unknown',

    // Behavior
    'showCallerNumber': true,
    'enableVibration': true,
    'enableRingtone': true,
    'showCallDuration': true,
    'callTimeoutSeconds': 60,
    'enableCallTimeout': true,
    'useFullScreenCallUI': true,
    'use24HourFormat': true,
    'durationFormat': 'mm:ss',
  };

  await _vCallkitPlugin.setUIConfiguration(config);
}
```

### Step 3: Show Calls with Custom Configuration

#### Method 1: Using Full Configuration Helper

```dart
Future<void> showCustomizedCall() async {
  await _vCallkitPlugin.showIncomingCallWithFullConfig(
    callerName: 'John Doe',
    callerNumber: '+1234567890',
    callerAvatar: 'https://example.com/avatar.jpg',
    isVideoCall: true,

    // Custom theme for this call
    backgroundColor: '#1D1B20',
    accentColor: '#D0BCFF',
    textColor: '#E6E1E5',

    // Custom translations for this call
    answerButtonText: 'Répondre',
    declineButtonText: 'Refuser',
    hangupButtonText: 'Raccrocher',
    incomingCallText: 'Appel Vidéo Entrant',

    // Custom behavior for this call
    enableVibration: false,
    callTimeoutSeconds: 45,
  );
}
```

#### Method 2: Using Configuration Object

```dart
Future<void> showCallWithConfig() async {
  final callData = {
    'id': 'call-123',
    'callerName': 'Jane Smith',
    'callerNumber': '+1987654321',
    'isVideoCall': false,
  };

  final config = {
    'backgroundColor': '#F2F2F7',
    'accentColor': '#007AFF',
    'textColor': '#000000',
    'answerButtonText': 'Contestar',
    'declineButtonText': 'Rechazar',
    'enableVibration': true,
    'showCallDuration': true,
  };

  await _vCallkitPlugin.showIncomingCallWithConfig({
    'callData': callData,
    'config': config,
  });
}
```

### Step 4: Test Hangup Notifications

```dart
Future<void> testHangupNotification() async {
  final testCallData = {
    'id': 'test_hangup_${DateTime.now().millisecondsSinceEpoch}',
    'callerName': 'Test Caller',
    'callerNumber': '+1555000123',
    'isVideoCall': false,
  };

  final config = {
    'backgroundColor': '#1C1B1F',
    'accentColor': '#FFB4AB',
    'textColor': '#E6E1E5',
    'hangupButtonText': 'End Call',
    'showCallDuration': true,
  };

  await _vCallkitPlugin.forceShowHangupNotification({
    'callData': testCallData,
    'config': config,
  });
}
```

### Step 5: Get Debug Information

```dart
Future<void> checkDebugInfo() async {
  final debugInfo = await _vCallkitPlugin.getCallManagerDebugInfo();

  print('Active Call: ${debugInfo['hasActiveCall']}');
  print('Android Version: API ${debugInfo['androidVersion']}');
  print('Foreground Service Support: ${debugInfo['hasCallForegroundService']}');
  print('Device: ${debugInfo['deviceManufacturer']} ${debugInfo['deviceModel']}');
  print('Plugin Version: ${debugInfo['pluginVersion']}');
}
```

## 🎯 Advanced Usage Examples

### Dynamic Language Switching

```dart
class CallManager {
  final Map<String, Map<String, String>> _languages = {
    'en': {
      'answerButtonText': 'Answer',
      'declineButtonText': 'Decline',
      'hangupButtonText': 'Hangup',
      'incomingVoiceCallText': 'Incoming Voice Call',
      'incomingVideoCallText': 'Incoming Video Call',
    },
    'es': {
      'answerButtonText': 'Contestar',
      'declineButtonText': 'Rechazar',
      'hangupButtonText': 'Colgar',
      'incomingVoiceCallText': 'Llamada de Voz Entrante',
      'incomingVideoCallText': 'Videollamada Entrante',
    },
    'fr': {
      'answerButtonText': 'Répondre',
      'declineButtonText': 'Refuser',
      'hangupButtonText': 'Raccrocher',
      'incomingVoiceCallText': 'Appel Vocal Entrant',
      'incomingVideoCallText': 'Appel Vidéo Entrant',
    },
  };

  Future<void> switchLanguage(String languageCode) async {
    final translation = _languages[languageCode] ?? _languages['en']!;
    await _vCallkitPlugin.setUIConfiguration(translation);
  }
}
```

### Theme-Based Call Display

```dart
class ThemeManager {
  static const Map<String, Map<String, String>> themes = {
    'dark': {
      'backgroundColor': '#1C1C1E',
      'accentColor': '#34C759',
      'textColor': '#FFFFFF',
      'secondaryTextColor': '#8E8E93',
    },
    'light': {
      'backgroundColor': '#F2F2F7',
      'accentColor': '#007AFF',
      'textColor': '#000000',
      'secondaryTextColor': '#6D6D70',
    },
    'purple': {
      'backgroundColor': '#1D1B20',
      'accentColor': '#D0BCFF',
      'textColor': '#E6E1E5',
      'secondaryTextColor': '#CAC4D0',
    },
  };

  Future<void> applyTheme(String themeName) async {
    final theme = themes[themeName] ?? themes['dark']!;
    await _vCallkitPlugin.setUIConfiguration(theme);
  }

  Future<void> showThemedCall({
    required String callerName,
    required String callerNumber,
    required String themeName,
    bool isVideoCall = false,
  }) async {
    final theme = themes[themeName] ?? themes['dark']!;

    await _vCallkitPlugin.showIncomingCallWithConfig({
      'callData': {
        'id': DateTime.now().millisecondsSinceEpoch.toString(),
        'callerName': callerName,
        'callerNumber': callerNumber,
        'isVideoCall': isVideoCall,
      },
      'config': theme,
    });
  }
}
```

### Real-Time Event Monitoring

```dart
class CallEventMonitor {
  late StreamSubscription _allEventsSubscription;
  late StreamSubscription _timerSubscription;

  void startMonitoring() {
    // Monitor all events
    _allEventsSubscription = _vCallkitPlugin.onAllEvents.listen((event) {
      _handleCallEvent(event);
    });

    // Monitor timer updates specifically
    _timerSubscription = _vCallkitPlugin.onCallTimerUpdated.listen((timerEvent) {
      _updateCallDuration(timerEvent);
    });
  }

  void _handleCallEvent(Map<String, dynamic> event) {
    switch (event['type']) {
      case 'answered':
        print('Call answered: ${event['callId']}');
        break;
      case 'ended':
        print('Call ended: ${event['callId']}');
        break;
      case 'timer_updated':
        print('Timer: ${event['duration']}s');
        break;
      case 'config_changed':
        print('Configuration updated');
        break;
    }
  }

  void _updateCallDuration(Map<String, dynamic> timerEvent) {
    final duration = timerEvent['duration'] as int;
    final callId = timerEvent['callId'] as String;
    final formattedDuration = _formatDuration(duration);

    print('Call $callId duration: $formattedDuration');
  }

  String _formatDuration(int seconds) {
    final hours = seconds ~/ 3600;
    final minutes = (seconds % 3600) ~/ 60;
    final secs = seconds % 60;

    if (hours > 0) {
      return '${hours.toString().padLeft(2, '0')}:${minutes.toString().padLeft(2, '0')}:${secs.toString().padLeft(2, '0')}';
    } else {
      return '${minutes.toString().padLeft(2, '0')}:${secs.toString().padLeft(2, '0')}';
    }
  }

  void stopMonitoring() {
    _allEventsSubscription.cancel();
    _timerSubscription.cancel();
  }
}
```

## 📱 Android Implementation Features

### Enhanced Notification System

- **CallStyle notifications** for Android 12+ with proper foreground service integration
- **Non-dismissible notifications** with live duration timers
- **Custom configuration** support for colors, text, and behavior per call
- **Robust foreground service** that survives app termination and system optimization

### Configuration Storage

- **Global UI configuration** persistence across calls
- **Per-call configuration** override capabilities
- **Real-time configuration** updates with event broadcasting
- **Debug information** access for troubleshooting

### Improved Robustness

- **Better error handling** with detailed error messages
- **Comprehensive testing** coverage for all new features
- **Enhanced logging** for debugging and monitoring
- **Platform compatibility** across Android versions

## 🧪 Testing Features

### Debug Methods

```dart
// Get comprehensive debug information
final debugInfo = await _vCallkitPlugin.getCallManagerDebugInfo();

// Test hangup notification display
await _vCallkitPlugin.forceShowHangupNotification({
  'callData': testCallData,
  'config': testConfig,
});

// Check current configuration
final currentConfig = _vCallkitPlugin.globalUIConfiguration;
```

### Event Testing

```dart
// Listen to specific call events
_vCallkitPlugin.listenToCall('specific-call-id').listen((event) {
  print('Event for call: ${event.callId}');
});

// Listen to timer updates for a specific call
_vCallkitPlugin.listenToCallTimer('call-id').listen((timerEvent) {
  print('Timer update: ${timerEvent['duration']}');
});

// Monitor configuration changes
_vCallkitPlugin.onConfigurationChanged.listen((config) {
  print('Configuration changed: $config');
});
```

## 🔧 Configuration Reference

### Complete Configuration Object

```dart
final completeConfig = {
  // Visual Theme
  'backgroundColor': '#1C1C1E',       // Main background color
  'accentColor': '#34C759',           // Accent/button color
  'textColor': '#FFFFFF',             // Primary text color
  'secondaryTextColor': '#8E8E93',    // Secondary text color

  // Text Translations
  'answerButtonText': 'Answer',
  'declineButtonText': 'Decline',
  'hangupButtonText': 'Hangup',
  'incomingVoiceCallText': 'Incoming Voice Call',
  'incomingVideoCallText': 'Incoming Video Call',
  'callInProgressText': 'Call in Progress',
  'tapToReturnText': 'Tap to return to call',
  'unknownCallerText': 'Unknown',
  'voiceCallText': 'Voice',
  'videoCallText': 'Video',
  'ongoingCallText': 'call',

  // Call Behavior
  'showCallerNumber': true,           // Show/hide caller number
  'enableVibration': true,            // Enable call vibration
  'enableRingtone': true,             // Enable call ringtone
  'showCallDuration': true,           // Show duration in hangup notification
  'callTimeoutSeconds': 60,           // Auto-dismiss timeout
  'enableCallTimeout': true,          // Enable auto-dismiss
  'useFullScreenCallUI': true,        // Use full screen call interface
  'showCallType': true,               // Show voice/video indicator
  'enableTapToReturnToCall': true,    // Enable return to call feature

  // Technical Settings
  'hangupNotificationPriority': 0,    // Notification priority
  'use24HourFormat': true,            // Time format preference
  'durationFormat': 'mm:ss',          // Duration display format

  // Accessibility
  'answerButtonContentDescription': 'Answer call button',
  'declineButtonContentDescription': 'Decline call button',
  'hangupButtonContentDescription': 'Hangup call button',
};
```

## 📚 Best Practices

### 1. **Initialize Early**

```dart
// Initialize plugin in main app initialization
void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  final plugin = VCallkitPlugin();
  plugin.initialize();

  // Set global configuration
  await plugin.setUIConfiguration(defaultConfig);

  runApp(MyApp());
}
```

### 2. **Handle Lifecycle Properly**

```dart
class CallService {
  final VCallkitPlugin _plugin = VCallkitPlugin();

  @override
  void initState() {
    super.initState();
    _plugin.initialize();
    _setupEventListeners();
  }

  @override
  void dispose() {
    _plugin.dispose();
    super.dispose();
  }
}
```

### 3. **Use Event Streams Efficiently**

```dart
// Use specific streams instead of listening to all events
_plugin.onCallTimerUpdated
  .where((event) => event['callId'] == currentCallId)
  .listen(_handleTimerUpdate);

// Use stream transformations for complex logic
_plugin.onCallEvent
  .where((event) => event.action == CallAction.answered)
  .asyncMap(_processAnsweredCall)
  .listen(_handleProcessedCall);
```

### 4. **Error Handling**

```dart
try {
  await _plugin.showIncomingCallWithConfig(callConfig);
} catch (error) {
  // Handle specific error types
  if (error.toString().contains('INVALID_CALL_DATA')) {
    _showDataError();
  } else if (error.toString().contains('UI_CONFIG_ERROR')) {
    _showConfigError();
  } else {
    _showGenericError(error);
  }
}
```

## 🚀 Getting Started

1. **Update your plugin dependency**
2. **Run the example app** to see all features in action
3. **Copy configuration patterns** from `CustomizationDemoScreen`
4. **Implement gradual customization** starting with themes, then translations, then advanced behavior
5. **Test thoroughly** with different Android versions and device types

## 📖 Migration Guide

If upgrading from a previous version:

1. **Initialize the plugin** explicitly with `plugin.initialize()`
2. **Update event listeners** to use the new enhanced streams
3. **Replace direct call methods** with the new configuration-based methods
4. **Add proper disposal** in your widget lifecycle
5. **Test all existing functionality** to ensure compatibility

The enhanced plugin maintains full backward compatibility while adding powerful new customization capabilities!
