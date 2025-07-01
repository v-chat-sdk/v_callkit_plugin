package com.v_chat_sdk.v_callkit_plugin

/**
 * Constants used throughout the V CallKit plugin
 * Centralized to avoid magic numbers and improve maintainability
 */
object CallConstants {
    
    // Plugin configuration
    const val PLUGIN_TAG = "VCallkitPlugin"
    const val CHANNEL_NAME = "v_callkit_plugin"
    
    // Notification channels
    const val CALL_NOTIFICATION_CHANNEL_ID = "incoming_calls"
    const val ONGOING_CALL_NOTIFICATION_CHANNEL_ID = "ongoing_calls"
    const val HANGUP_NOTIFICATION_CHANNEL_ID = "hangup_notification_channel"
    
    // Notification IDs
    const val CALL_NOTIFICATION_ID = 1001
    const val ONGOING_CALL_NOTIFICATION_ID = 1002
    const val HANGUP_NOTIFICATION_ID = 1004
    
    // Service IDs
    const val FOREGROUND_SERVICE_ID = 1003
    
    // Wake lock tags
    const val WAKE_LOCK_TAG = "VCallKit:IncomingCall"
    
    // Intent actions
    const val ACTION_ANSWER = "ANSWER"
    const val ACTION_DECLINE = "DECLINE"
    const val ACTION_HANGUP = "HANGUP"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    
    // Intent extras
    const val EXTRA_CALL_DATA = "call_data"
    const val EXTRA_CALL_ID = "callId"
    const val EXTRA_ACTION = "action"
    const val EXTRA_CONFIG = "config"
    
    // Timeouts and durations
    const val WAKE_LOCK_TIMEOUT_MS = 10 * 60 * 1000L // 10 minutes
    const val DUPLICATE_ACTION_THRESHOLD_MS = 2000L // 2 seconds
    const val ACTION_CLEANUP_THRESHOLD_MS = 10000L // 10 seconds
    const val CALL_DURATION_UPDATE_INTERVAL_MS = 1000L // 1 second
    
    // Vibration patterns
    val INCOMING_CALL_VIBRATION_PATTERN = longArrayOf(0, 1000, 500, 1000)
    
    // PendingIntent request codes
    const val ANSWER_PENDING_INTENT_REQUEST_CODE = 1
    const val DECLINE_PENDING_INTENT_REQUEST_CODE = 2
    const val HANGUP_PENDING_INTENT_REQUEST_CODE = 3
    const val CONTENT_PENDING_INTENT_REQUEST_CODE = 4
    
    // Call UI configuration
    const val AVATAR_SIZE_DP = 120
    const val BUTTON_SIZE_DP = 80
    const val BUTTON_SPACING_DP = 100
    
    // Audio configuration
    const val AUDIO_STREAM_TYPE = android.media.AudioManager.STREAM_RING
    
    // Colors
    const val COLOR_BACKGROUND = "#1C1C1E"
    const val COLOR_TEXT_PRIMARY = "#FFFFFF"
    const val COLOR_TEXT_SECONDARY = "#8E8E93"
    const val COLOR_ANSWER_BUTTON = "#34C759"
    const val COLOR_DECLINE_BUTTON = "#FF3B30"
    const val COLOR_AVATAR_PLACEHOLDER = "#4CAF50"
    
    // Notification priorities (for backward compatibility)
    const val NOTIFICATION_PRIORITY_DEFAULT = 0
    const val NOTIFICATION_PRIORITY_HIGH = 1
    const val NOTIFICATION_PRIORITY_LOW = -1
    
    // Manufacturer detection
    const val MANUFACTURER_XIAOMI = "xiaomi"
    const val MANUFACTURER_HUAWEI = "huawei"
    const val MANUFACTURER_OPPO = "oppo"
    const val MANUFACTURER_VIVO = "vivo"
    const val MANUFACTURER_ONEPLUS = "oneplus"
    const val MANUFACTURER_SAMSUNG = "samsung"
    
    // Chinese ROM manufacturers that need special handling
    val CHINESE_ROM_MANUFACTURERS = setOf(
        MANUFACTURER_XIAOMI,
        MANUFACTURER_HUAWEI,
        MANUFACTURER_OPPO,
        MANUFACTURER_VIVO,
        MANUFACTURER_ONEPLUS
    )
    
    // Error codes
    const val ERROR_INVALID_ARGUMENTS = "INVALID_ARGUMENTS"
    const val ERROR_INVALID_CALL_DATA = "INVALID_CALL_DATA"
    const val ERROR_NOTIFICATION_ERROR = "NOTIFICATION_ERROR"
    const val ERROR_PERMISSIONS_DENIED = "PERMISSIONS_DENIED"
    const val ERROR_NATIVE_CALL_ERROR = "NATIVE_CALL_ERROR"
    const val ERROR_SERVICE_ERROR = "SERVICE_ERROR"
    
    // Method names
    const val METHOD_GET_PLATFORM_VERSION = "getPlatformVersion"
    const val METHOD_HAS_PERMISSIONS = "hasPermissions"
    const val METHOD_REQUEST_PERMISSIONS = "requestPermissions"
    const val METHOD_SHOW_INCOMING_CALL = "showIncomingCall"
    const val METHOD_SHOW_INCOMING_CALL_NATIVE = "showIncomingCallNative"
    const val METHOD_END_CALL = "endCall"
    const val METHOD_ANSWER_CALL = "answerCall"
    const val METHOD_REJECT_CALL = "rejectCall"
    const val METHOD_MUTE_CALL = "muteCall"
    const val METHOD_HOLD_CALL = "holdCall"
    const val METHOD_IS_CALL_ACTIVE = "isCallActive"
    const val METHOD_GET_ACTIVE_CALL_DATA = "getActiveCallData"
    const val METHOD_SET_CUSTOM_RINGTONE = "setCustomRingtone"
    const val METHOD_GET_SYSTEM_RINGTONES = "getSystemRingtones"
    const val METHOD_CHECK_BATTERY_OPTIMIZATION = "checkBatteryOptimization"
    const val METHOD_REQUEST_BATTERY_OPTIMIZATION = "requestBatteryOptimization"
    const val METHOD_GET_DEVICE_MANUFACTURER = "getDeviceManufacturer"
    const val METHOD_GET_LAST_CALL_ACTION_LAUNCH = "getLastCallActionLaunch"
    const val METHOD_HAS_CALL_ACTION_LAUNCH_DATA = "hasCallActionLaunchData"
    const val METHOD_CLEAR_CALL_ACTION_LAUNCH_DATA = "clearCallActionLaunchData"
    const val METHOD_FORCE_SHOW_ONGOING_NOTIFICATION = "forceShowOngoingNotification"
    const val METHOD_SHOW_HANGUP_NOTIFICATION = "showHangupNotification"
    const val METHOD_HIDE_HANGUP_NOTIFICATION = "hideHangupNotification"
    const val METHOD_UPDATE_HANGUP_NOTIFICATION = "updateHangupNotification"
    
    // Flutter callback methods
    const val CALLBACK_ON_CALL_ANSWERED = "onCallAnswered"
    const val CALLBACK_ON_CALL_REJECTED = "onCallRejected"
    const val CALLBACK_ON_CALL_ENDED = "onCallEnded"
    const val CALLBACK_ON_CALL_MUTE = "onCallMute"
    const val CALLBACK_ON_CALL_HOLD = "onCallHold"
    
    // Call state strings
    const val CALL_STATE_RINGING = "ringing"
    const val CALL_STATE_ACTIVE = "active"
    const val CALL_STATE_HOLDING = "holding"
    const val CALL_STATE_ENDED = "ended"
    
    // End call reasons
    const val END_REASON_HANGUP = "hangup"
    const val END_REASON_ENDED = "ended"
    const val END_REASON_REJECTED = "rejected"
    
    // Notification text templates
    const val NOTIFICATION_INCOMING_VOICE_CALL = "Incoming Voice Call"
    const val NOTIFICATION_INCOMING_VIDEO_CALL = "Incoming Video Call"
    const val NOTIFICATION_CALL_IN_PROGRESS = "Call in Progress"
    const val NOTIFICATION_TAP_TO_RETURN = "Tap to return to call"
    const val NOTIFICATION_HANGUP_BUTTON_TEXT = "Hangup"
    
    // Intent flags
    const val ACTIVITY_LAUNCH_FLAGS = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or 
        android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
    
    const val CALL_SCREEN_LAUNCH_FLAGS = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or 
        android.content.Intent.FLAG_ACTIVITY_NO_USER_ACTION or 
        android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
} 