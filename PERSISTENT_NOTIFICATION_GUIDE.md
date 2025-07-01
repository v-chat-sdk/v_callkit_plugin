# Persistent Hangup Notification Implementation Guide

## Overview

The v_callkit_plugin implements a truly persistent hangup notification that cannot be dismissed by swiping and is only dismissible via the "Hangup" button. This implementation is based on the [Google Maps Navigation SDK pattern](https://developers.google.com/maps/documentation/navigation/android-sdk/consolidating-notifications) for consolidating foreground service notifications.

## Key Architecture Components

### 1. ForegroundServiceManager

Based on Google's design pattern, the `ForegroundServiceManager` provides:

- **Singleton Pattern**: Ensures only one persistent notification across all foreground services
- **Notification Consolidation**: All services use the same notification ID to prevent multiple persistent notifications
- **Cross-API Compatibility**: Handles Android versions from API 21+ with proper foreground service types
- **True Persistence**: Implements Google's recommendations for non-dismissible notifications

#### Key Features from Google Maps SDK Pattern:

```kotlin
// Initialize with custom notification provider
ForegroundServiceManager.initForegroundServiceManagerProvider(
    application,
    notificationId = 2001,
    notificationProvider = customProvider
)

// Start service in foreground with shared notification
manager.startForeground(service, serviceId)

// Update shared notification
manager.updateNotification()
```

### 2. NotificationContentProvider Interface

Following Google's pattern, the system uses a provider interface for custom notification content:

```kotlin
interface NotificationContentProvider {
    fun getNotification(): Notification
}
```

### 3. HangupNotificationContentProvider

Custom implementation that creates persistent notifications with:

- **Real-time duration updates**
- **Custom avatar support with fallback to initials**
- **Expanded layout showing all information without user interaction**
- **Proper persistence flags for Android O and newer**

## Implementation Details

### API Level Compatibility

The implementation handles different Android versions according to [Android's foreground service requirements](https://developer.android.com/develop/background-work/services/fgs):

#### Pre-API 26 (Android 7.1 and below)

- No notification channels needed
- Basic foreground service with `startForeground()`
- Uses deprecated NotificationCompat.Builder constructor

#### API 26-28 (Android 8.0 - 9.0)

- **Notification channels required** for foreground services
- Uses `startForegroundService()` to start the service
- Basic foreground service without service type

#### API 29-33 (Android 10 - 13)

- Optional `foregroundServiceType` in manifest (recommended)
- Uses `startForeground()` with `ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL`

#### API 34+ (Android 14+)

- **Mandatory `foregroundServiceType`** in manifest as per [Android 14 requirements](https://developer.android.com/about/versions/14/changes/fgs-types-required)
- Requires `FOREGROUND_SERVICE_PHONE_CALL` permission
- Strict foreground service type enforcement

### Critical Notification Flags

Based on Google's documentation and Android's requirements, the notification uses these persistence flags:

```kotlin
notification.flags = notification.flags or
    Notification.FLAG_NO_CLEAR or        // Prevents swipe-to-dismiss
    Notification.FLAG_ONGOING_EVENT or    // Marks as ongoing
    Notification.FLAG_FOREGROUND_SERVICE  // Marks as foreground service notification

// Explicitly disable auto-cancel
notification.flags = notification.flags and Notification.FLAG_AUTO_CANCEL.inv()
```

Additionally:

- `setOngoing(true)`: Makes notification non-dismissible
- `setAutoCancel(false)`: Prevents auto-dismissal
- `setDeleteIntent(null)`: Disables swipe-to-dismiss action

### Service Lifecycle Management

#### ForegroundServiceManager Pattern

The manager handles service lifecycle automatically:

```kotlin
// Start multiple services using the same notification
manager.startForeground(service1, "service_1")
manager.startForeground(service2, "service_2")

// Stop services individually while maintaining notification
manager.stopForeground(service1, "service_1", removeNotification = false)
// Notification remains for service2

// Stop last service and remove notification
manager.stopForeground(service2, "service_2", removeNotification = true)
```

#### START_STICKY Return Value

The service returns `START_STICKY` to ensure the system restarts it if killed:

```kotlin
override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    // ... handle intent
    return START_STICKY  // Ensures service restarts if killed
}
```

#### onTaskRemoved Handler

Handles app removal from recents (critical for MIUI and other customized Android versions):

```kotlin
override fun onTaskRemoved(rootIntent: Intent?) {
    Log.w(TAG, "onTaskRemoved called - ForegroundServiceManager should maintain persistence")
    // The ForegroundServiceManager handles persistence automatically
    super.onTaskRemoved(rootIntent)
}
```

## Manufacturer-Specific Considerations

### Problem Statement

As noted in the [Google Maps documentation](https://developers.google.com/maps/documentation/navigation/android-sdk/consolidating-notifications), **beginning with Android API level 26, persistent notifications are required for foreground services**. This requirement prevents hiding services that might put excessive demands on system resources.

However, manufacturer customizations can still interfere with notification persistence.

### Xiaomi (MIUI)

MIUI aggressively kills background services. Users must:

1. **Enable Autostart**:

   - Settings → Apps → Manage apps → [Your App] → Autostart → Enable

2. **Disable Battery Optimization**:

   - Settings → Battery & performance → App battery saver → [Your App] → No restrictions

3. **Lock App in Recents**:
   - Open recent apps → Long press on app → Lock

### Huawei (EMUI)

Similar to MIUI, requires:

1. **Protected Apps**:

   - Settings → Advanced Settings → Battery Manager → Protected apps → Enable for your app

2. **App Launch Management**:
   - Settings → Apps → App launch → [Your App] → Manage manually → Enable all toggles

### Oppo/Vivo/OnePlus

1. **Battery Optimization**:

   - Settings → Battery → Battery Optimization → [Your App] → Don't optimize

2. **App Freeze**:
   - Settings → Battery → App Freeze → Disable for your app

## Usage in Your App

### Starting the Persistent Notification

```kotlin
// Start hangup notification service with ForegroundServiceManager
HangupNotificationService.startService(
    context = context,
    callData = CallData(
        id = "call_123",
        callerName = "John Doe",
        callerAvatar = "https://example.com/avatar.jpg"
    ),
    config = mapOf(
        "showDuration" to true,
        "enableTapToReturn" to true,
        "hangupButtonText" to "End Call"
    )
)
```

### Stopping the Notification

```kotlin
// Stop hangup notification service
HangupNotificationService.stopService(context)
```

### Handling in Your Flutter App

```dart
// Check if manufacturer-specific settings are needed
final deviceInfo = await vCallkitPlugin.getDeviceManufacturerInfo();

if (deviceInfo.requiresSpecialSettings) {
  // Show dialog with manufacturer-specific instructions
  showManufacturerSettingsDialog(deviceInfo.settingsInstructions);

  // Request battery optimization exemption
  await vCallkitPlugin.requestBatteryOptimizationExemption();
}
```

## Testing Persistence

To test notification persistence following Google's pattern:

1. **Swipe Test**: Try to swipe the notification away - it should not dismiss
2. **App Kill Test**: Force stop the app from Settings - the notification should remain
3. **Multiple Service Test**: Start multiple foreground services - should share one notification
4. **Recents Clear Test**: Remove app from recents - the ForegroundServiceManager should maintain persistence
5. **Reboot Test**: Restart device - the notification should clear (this is expected)
6. **Hangup Button Test**: Tap the hangup button - this should be the only way to dismiss

## Debugging Tips

Enable verbose logging to monitor the ForegroundServiceManager lifecycle:

```kotlin
Log.d(TAG, "ForegroundServiceManager state: ...")
```

Key log messages to look for:

- "ForegroundServiceManager initialized with notification ID: ..."
- "Service X started foreground with PHONE_CALL type"
- "Persistent notification created with flags: ... (NO_CLEAR=true, ONGOING=true)"
- "Notification is truly non-dismissible and consolidated"

## Permissions Required

Ensure these permissions are in your app's AndroidManifest.xml:

```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_PHONE_CALL" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />
```

And declare the service with appropriate type:

```xml
<service
    android:name=".HangupNotificationService"
    android:exported="false"
    android:foregroundServiceType="phoneCall" />
```

## Summary

The implementation ensures persistent hangup notifications through:

1. **Google Maps SDK Pattern**: ForegroundServiceManager for notification consolidation
2. **Proper notification flags**: `FLAG_NO_CLEAR`, `FLAG_ONGOING_EVENT`, `FLAG_FOREGROUND_SERVICE`
3. **Service lifecycle management**: START_STICKY, proper foreground service handling
4. **API level compatibility**: Handles requirements from API 21 through API 34+
5. **Custom notification provider**: Real-time updates with avatar support
6. **Manufacturer-specific workarounds**: Detection and guidance for problematic OEMs

The key insight from Google's approach is that **persistent notifications are required for foreground services starting with Android API level 26**, and our ForegroundServiceManager ensures proper consolidation and persistence across all Android versions.

Users on customized Android versions (MIUI, EMUI, etc.) may need to manually enable autostart and disable battery optimization for optimal persistence, but the underlying notification architecture follows Google's recommended patterns for maximum compatibility.
