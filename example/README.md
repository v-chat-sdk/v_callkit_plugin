# VCallKit Plugin Example App

This example app demonstrates the comprehensive features of the VCallKit plugin, including full UI customization, multiple language support, and advanced call handling capabilities.

## 🚀 Features

### ✨ UI Customization Demo

The app includes a dedicated **Customization Demo** screen that showcases:

- **🎨 Visual Themes**: Pre-built themes with different color schemes
- **🌍 Multi-language Support**: English, Spanish, French, Arabic translations
- **⚙️ Call Settings**: Configurable timeouts, caller number display, duration tracking
- **🔊 Audio & Vibration**: Customizable audio feedback and vibration settings
- **📱 Real-time Preview**: Test your configurations with live call demos

### 📞 Core Call Features

- **Incoming Call UI**: Full-screen incoming call interface with custom branding
- **Call Notifications**: System-integrated call notifications that work across all Android versions
- **Enhanced Hangup Notifications**: Non-dismissible ongoing call notifications with live duration counter
- **Real-time Duration Tracking**: ⏱️ Live call timer that updates every second (MM:SS or HH:MM:SS format)
- **User Avatars**: 👤 Displays caller profile pictures with automatic fallback to initials
- **Smart Avatar System**: 🖼️ Circular cropping, async loading, and colored initials when no photo available
- **Always Expanded Display**: 📱 Full notification details visible without clicking arrow down
- **Prominent Timer Display**: ⏱️ Duration shown prominently in notification content
- **Call Actions**: Answer/Decline from notifications even when app is terminated
- **Background Processing**: Reliable call handling even when app is not active

### 🔧 Advanced Configuration

- **Cross-Android Compatibility**: Works on Android 5.0+ (API 21+) with version-specific optimizations
- **Foreground Service Integration**: Proper foreground service usage for persistent notifications
- **Battery Optimization Handling**: Guides users through battery optimization exemptions
- **Custom Ringtones**: Support for system and custom ringtone selection
- **Debug Tools**: Built-in debugging and diagnostic information

## 🎨 UI Customization System

### Theme Configuration

The plugin supports complete visual customization:

```dart
await VCallkitPlugin.setUIConfiguration({
  // Visual Theme
  'backgroundColor': '#1C1C1E',           // Call screen background
  'accentColor': '#34C759',               // Button and accent colors
  'textColor': '#FFFFFF',                 // Primary text color
  'secondaryTextColor': '#8E8E93',        // Secondary text color

  // Button Texts (Translatable)
  'answerButtonText': 'Answer',           // Answer button text
  'declineButtonText': 'Decline',         // Decline button text
  'hangupButtonText': 'Hangup',           // Hangup button text

  // Call UI Text (Translatable)
  'incomingVoiceCallText': 'Incoming Voice Call',
  'incomingVideoCallText': 'Incoming Video Call',
  'callInProgressText': 'Call in Progress',
  'tapToReturnText': 'Tap to return to call',

  // Display Settings
  'showCallerNumber': true,               // Show/hide caller number
  'showCallDuration': true,               // Show/hide call timer
  'callTimeoutSeconds': 60,               // Auto-dismiss timeout

  // Audio & Vibration
  'enableVibration': true,                // Enable vibration
  'enableRingtone': true,                 // Enable ringtone
});
```

### Language Support

Built-in translations for:

- 🇺🇸 **English**: Complete interface in English
- 🇪🇸 **Spanish**: Full Spanish localization
- 🇫🇷 **French**: Complete French translation
- 🇸🇦 **Arabic**: Right-to-left Arabic support

### Per-Call Customization

You can also customize individual calls:

```dart
await VCallkitPlugin.showIncomingCallWithConfig({
  'callData': {
    'id': 'unique_call_id',
    'callerName': 'John Doe',
    'callerNumber': '+1234567890',
    'isVideoCall': false,
  },
  'config': {
    'backgroundColor': '#FF6B6B',
    'accentColor': '#4ECDC4',
    'answerButtonText': 'Accept Call',
    'declineButtonText': 'Reject Call',
    // ... any other configuration
  }
});
```

## 🔧 Hangup Notification System

### Cross-Version Compatibility

The plugin ensures hangup notifications work on all Android versions:

- **Android 8.0+ (API 26+)**: Uses foreground service with notification channels
- **Android 7.0 and below (API 21-25)**: Uses persistent notification with compatibility flags
- **All Versions**: Non-dismissible notifications that can only be cleared by ending the call

### Testing Hangup Notifications

Use the built-in test function:

```dart
await VCallkitPlugin.forceShowHangupNotification({
  'callData': {
    'id': 'test_call',
    'callerName': 'Test Caller',
    'callerNumber': '+1234567890',
    'isVideoCall': false,
  },
  'config': {
    'title': 'Custom Call in Progress',
    'contentText': '{callerName} - {duration}',
    'hangupButtonText': 'End Call',
    'showDuration': true,
  }
});
```

## 📱 Getting Started

1. **Clone the repository**:

   ```bash
   git clone <repository-url>
   cd v_callkit_plugin/example
   ```

2. **Install dependencies**:

   ```bash
   flutter pub get
   ```

3. **Run the app**:

   ```bash
   flutter run
   ```

4. **Grant permissions**:

   - Allow notification permissions
   - Exempt from battery optimization
   - Allow system alert window (if prompted)

5. **Try the demos**:
   - Open **"UI Customization Demo"** from the home screen
   - Test different themes and languages
   - Try voice and video call demos
   - Test hangup notifications

## 🔍 Debugging and Troubleshooting

### Debug Information

Access debug info programmatically:

```dart
final debugInfo = await VCallkitPlugin.getCallManagerDebugInfo();
print('Android Version: API ${debugInfo['androidVersion']}');
print('Call Active: ${debugInfo['isCallActive']}');
print('Foreground Service Support: ${debugInfo['hasHangupNotificationService']}');
```

### Common Issues

**Notifications not showing:**

- Check notification permissions in Settings > Apps > [Your App] > Notifications
- Ensure the app is not battery optimized
- Verify Android version compatibility

**Calls not launching app:**

- Confirm notification actions are properly configured
- Check that `CallActionReceiver` is declared in AndroidManifest.xml
- Verify the app has required permissions

**Hangup notifications being dismissed:**

- This shouldn't happen with proper implementation
- Check Android logs for service lifecycle issues
- Ensure foreground service permissions are granted

## 📋 Requirements

- **Flutter**: 3.0.0 or higher
- **Android**: API 21 (Android 5.0) or higher
- **Permissions**:
  - POST_NOTIFICATIONS (Android 13+)
  - FOREGROUND_SERVICE
  - FOREGROUND_SERVICE_PHONE_CALL (Android 10+)
  - SYSTEM_ALERT_WINDOW
  - WAKE_LOCK
  - VIBRATE

## 🎯 Production Usage

When implementing in your production app:

1. **Set global configuration** once during app initialization
2. **Use consistent theming** that matches your app's design system
3. **Implement proper error handling** for all call operations
4. **Test on various Android versions** and manufacturers
5. **Handle battery optimization** proactively
6. **Provide clear user guidance** for permission requests

## 📚 API Reference

### Core Methods

- `VCallkitPlugin.setUIConfiguration()` - Set global UI configuration
- `VCallkitPlugin.showIncomingCallWithConfig()` - Show call with custom config
- `VCallkitPlugin.forceShowHangupNotification()` - Test hangup notifications
- `VCallkitPlugin.getCallManagerDebugInfo()` - Get debug information

### Events

- `VCallkitPlugin.onCallAnswered` - Call answered events
- `VCallkitPlugin.onCallRejected` - Call rejected events
- `VCallkitPlugin.onCallEnded` - Call ended events

For complete API documentation, see the main plugin README.

## 💡 Tips

- **Test on real devices** for the most accurate experience
- **Use the customization demo** to preview changes before implementing
- **Check debug info** if encountering issues
- **Follow Android guidelines** for notification and foreground service usage
- **Consider user preferences** when setting default configurations

---

This example app demonstrates the full capabilities of the VCallKit plugin. Feel free to use it as a reference for implementing callkit functionality in your own applications.
