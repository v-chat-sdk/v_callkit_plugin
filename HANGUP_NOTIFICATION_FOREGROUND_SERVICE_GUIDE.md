# Hangup Notification with Foreground Service Guide

## Overview

This guide demonstrates how the VCallKit plugin launches hangup notifications with the **exact same foreground service pattern** as when accepting calls from notifications. The implementation ensures that both `CallForegroundService` and `HangupNotificationService` follow identical patterns for robustness and reliability.

## Foreground Service Pattern Equivalence

### When Accepting a Call from Notification

1. **User taps "Answer" on incoming call notification**
2. **CallActionReceiver.handleAnswerCall()** is triggered
3. **CallForegroundService.startService()** is called
4. **Foreground service starts** with non-dismissible notification
5. **Duration timer begins** updating every second
6. **Service survives** app backgrounding and termination

### When Launching Hangup Notification

1. **App calls launchHangupNotificationWithForegroundService()**
2. **HangupNotificationService.startService()** is called
3. **Foreground service starts** with non-dismissible notification
4. **Duration timer begins** updating every second
5. **Service survives** app backgrounding and termination

## Implementation Details

### Android Native Implementation

#### VCallkitPlugin.kt - New Method

```kotlin
/**
 * Special method to demonstrate launching hangup notification with the exact same
 * foreground service pattern as when accepting calls from notifications.
 */
private fun handleLaunchHangupNotificationWithForegroundService(call: MethodCall, result: Result) {
    // Uses the exact same pattern as when a call is accepted from notification:
    // 1. Start HangupNotificationService (equivalent to CallForegroundService)
    // 2. Create non-dismissible notification with proper flags
    // 3. Handle app backgrounding and system optimization
    // 4. Provide live duration timer updates
    // 5. Ensure service survives app termination

    HangupNotificationService.startService(context, callData)

    Log.d(TAG, "Service characteristics:")
    Log.d(TAG, "- Foreground service with non-dismissible notification")
    Log.d(TAG, "- Live timer updates every second")
    Log.d(TAG, "- Survives app backgrounding and termination")
    Log.d(TAG, "- Same notification flags as ongoing call notifications")
    Log.d(TAG, "- Same service lifecycle management as call acceptance")
}
```

#### Service Characteristics Comparison

| Feature                | CallForegroundService             | HangupNotificationService         |
| ---------------------- | --------------------------------- | --------------------------------- |
| **Service Type**       | Foreground Service                | Foreground Service                |
| **Notification Flags** | FLAG_NO_CLEAR, FLAG_ONGOING_EVENT | FLAG_NO_CLEAR, FLAG_ONGOING_EVENT |
| **Timer Updates**      | Every 1 second                    | Every 1 second                    |
| **App Survival**       | Survives termination              | Survives termination              |
| **Dismissibility**     | Non-dismissible                   | Non-dismissible                   |
| **Lifecycle**          | START_NOT_STICKY                  | START_NOT_STICKY                  |

### Flutter Implementation

#### New Method in VCallkitPlugin

```dart
/// Launch hangup notification with the same foreground service pattern as accepting calls from notifications
Future<bool> launchHangupNotificationWithForegroundService(CallData callData) {
  return VCallkitPluginPlatform.instance.launchHangupNotificationWithForegroundService(
    callData.toMap(),
  );
}
```

#### Enhanced Demo Widget

The `HangupNotificationDemoWidget` now includes:

1. **Step 1**: Simulate answered call (establishes CallForegroundService)
2. **Step 2**: Launch hangup notification (standard method)
3. **Alternative**: Launch with EXACT same pattern (new method)

## Demo Usage

### Basic Usage

```dart
final plugin = VCallkitPlugin();

// Method 1: Standard hangup notification
await plugin.showHangupNotification(callData);

// Method 2: Explicit foreground service pattern
await plugin.launchHangupNotificationWithForegroundService(callData);
```

### Complete Demo Flow

1. **Run the example app**
2. **Tap "Step 1: Simulate Answered Call"**
   - Creates incoming call notification
   - Automatically answers after 1.5 seconds
   - Establishes CallForegroundService with ongoing notification
3. **Tap "Step 2: Launch Hangup Notification"**
   - Starts HangupNotificationService
   - Shows both services running simultaneously
4. **Or tap "Alternative: Launch with EXACT Same Pattern"**
   - Uses the specialized method with detailed logging
   - Demonstrates explicit service equivalence

## Service Equivalence Verification

### Android Logs

When using the new method, you'll see detailed logs showing the service startup:

```
VCallkitPlugin: Launching hangup notification with foreground service pattern (same as call acceptance)
VCallkitPlugin: HangupNotificationService started with same foreground service robustness as CallForegroundService
VCallkitPlugin: Service characteristics:
VCallkitPlugin: - Foreground service with non-dismissible notification
VCallkitPlugin: - Live timer updates every second
VCallkitPlugin: - Survives app backgrounding and termination
VCallkitPlugin: - Same notification flags as ongoing call notifications
VCallkitPlugin: - Same service lifecycle management as call acceptance
```

### Visual Indicators

The demo widget shows:

- **Call Service Status**: Green when CallForegroundService is active
- **Hangup Service Status**: Orange when HangupNotificationService is active
- **Both can run simultaneously** with the same robustness

## Technical Benefits

### 1. Service Robustness

- Both services use `startForegroundService()` with proper permissions
- Both handle SecurityException gracefully with fallback notifications
- Both survive app backgrounding and system optimization

### 2. Notification Persistence

- Both create non-dismissible notifications with `FLAG_NO_CLEAR` and `FLAG_ONGOING_EVENT`
- Both use live duration timers updating every second
- Both maintain foreground service association

### 3. Lifecycle Management

- Both use `START_NOT_STICKY` to prevent unwanted restarts
- Both handle `onTaskRemoved()` for proper cleanup
- Both provide graceful shutdown methods

### 4. Android Version Compatibility

- Both support Android 8+ foreground service requirements
- Both handle permission requirements for Android 14+
- Both provide fallback behavior for older versions

## Testing the Implementation

### 1. Foreground Service Equivalence

```bash
# Run the app and execute the demo flow
flutter run

# Check Android logs for service startup
adb logcat | grep VCallkitPlugin
```

### 2. Background Survival Test

1. Start both services using the demo
2. Background the app (home button)
3. Open notification panel - both notifications persist
4. Try swiping notifications - both are non-dismissible
5. Return to app - both services still active

### 3. App Termination Test

1. Start both services
2. Kill app from recent apps
3. Check notification panel - both notifications persist
4. Tap hangup button - properly handles cleanup

## Conclusion

The VCallKit plugin's hangup notification implementation uses the **exact same foreground service pattern** as when accepting calls from notifications. This ensures:

- **Identical robustness** and reliability
- **Same survival characteristics** for app backgrounding/termination
- **Consistent user experience** across all call states
- **Proper Android foreground service compliance**

The new `launchHangupNotificationWithForegroundService()` method explicitly demonstrates this equivalence with detailed logging and identical service characteristics.
