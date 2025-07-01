package com.v_chat_sdk.v_callkit_plugin

import android.os.Bundle

/**
 * Configuration class for customizable UI parameters and text translations
 * Allows Flutter to customize all aspects of the call UI and notifications
 */
data class CallUIConfig(
    // Button texts
    val answerButtonText: String = "Answer",
    val declineButtonText: String = "Decline", 
    val hangupButtonText: String = "Hangup",
    
    // Notification texts
    val incomingVoiceCallText: String = "Incoming Voice Call",
    val incomingVideoCallText: String = "Incoming Video Call",
    val callInProgressText: String = "Call in Progress",
    val tapToReturnText: String = "Tap to return to call",
    val ongoingCallText: String = "call",
    val voiceCallText: String = "Voice",
    val videoCallText: String = "Video",
    
    // Call screen texts
    val incomingCallLabel: String = "Incoming Call",
    val unknownCallerText: String = "Unknown",
    
    // Hangup notification configuration
    val hangupNotificationTitle: String? = null, // null = use default
    val hangupNotificationContent: String? = null, // null = use default
    val hangupNotificationSubText: String? = null, // null = use default
    val showCallDuration: Boolean = true,
    val enableTapToReturnToCall: Boolean = true,
    val hangupNotificationPriority: Int = 0, // -1=low, 0=default, 1=high
    
    // Call timeout configuration
    val callTimeoutSeconds: Int = 60, // Auto-dismiss after this time
    val enableCallTimeout: Boolean = true,
    
    // Audio configuration
    val enableVibration: Boolean = true,
    val enableRingtone: Boolean = true,
    val vibrateOnSilentMode: Boolean = true,
    
    // Visual customization
    val accentColor: String? = null, // Hex color for buttons and accents
    val backgroundColor: String? = null, // Background color for call screen
    val textColor: String? = null, // Primary text color
    val secondaryTextColor: String? = null, // Secondary text color
    
    // Avatar configuration
    val avatarUrl: String? = null, // Custom avatar URL
    val showAvatarPlaceholder: Boolean = true,
    val avatarShape: String = "circle", // "circle" or "square"
    
    // Layout configuration
    val useFullScreenCallUI: Boolean = true,
    val showCallerNumber: Boolean = true,
    val showCallType: Boolean = true, // Show "Voice Call" or "Video Call"
    
    // Advanced notification settings
    val notificationChannelName: String? = null,
    val notificationChannelDescription: String? = null,
    val useCustomNotificationSound: Boolean = false,
    val notificationSoundUri: String? = null,
    
    // Accessibility
    val answerButtonContentDescription: String? = null,
    val declineButtonContentDescription: String? = null,
    val hangupButtonContentDescription: String? = null,
    
    // Localization
    val locale: String? = null, // Language code for system format (duration, etc.)
    val use24HourFormat: Boolean = true,
    val durationFormat: String = "mm:ss" // "mm:ss" or "hh:mm:ss" or custom
) {
    
    /**
     * Convert to Map for inter-process communication
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "answerButtonText" to answerButtonText,
            "declineButtonText" to declineButtonText,
            "hangupButtonText" to hangupButtonText,
            "incomingVoiceCallText" to incomingVoiceCallText,
            "incomingVideoCallText" to incomingVideoCallText,
            "callInProgressText" to callInProgressText,
            "tapToReturnText" to tapToReturnText,
            "ongoingCallText" to ongoingCallText,
            "voiceCallText" to voiceCallText,
            "videoCallText" to videoCallText,
            "incomingCallLabel" to incomingCallLabel,
            "unknownCallerText" to unknownCallerText,
            "hangupNotificationTitle" to hangupNotificationTitle,
            "hangupNotificationContent" to hangupNotificationContent,
            "hangupNotificationSubText" to hangupNotificationSubText,
            "showCallDuration" to showCallDuration,
            "enableTapToReturnToCall" to enableTapToReturnToCall,
            "hangupNotificationPriority" to hangupNotificationPriority,
            "callTimeoutSeconds" to callTimeoutSeconds,
            "enableCallTimeout" to enableCallTimeout,
            "enableVibration" to enableVibration,
            "enableRingtone" to enableRingtone,
            "vibrateOnSilentMode" to vibrateOnSilentMode,
            "accentColor" to accentColor,
            "backgroundColor" to backgroundColor,
            "textColor" to textColor,
            "secondaryTextColor" to secondaryTextColor,
            "avatarUrl" to avatarUrl,
            "showAvatarPlaceholder" to showAvatarPlaceholder,
            "avatarShape" to avatarShape,
            "useFullScreenCallUI" to useFullScreenCallUI,
            "showCallerNumber" to showCallerNumber,
            "showCallType" to showCallType,
            "notificationChannelName" to notificationChannelName,
            "notificationChannelDescription" to notificationChannelDescription,
            "useCustomNotificationSound" to useCustomNotificationSound,
            "notificationSoundUri" to notificationSoundUri,
            "answerButtonContentDescription" to answerButtonContentDescription,
            "declineButtonContentDescription" to declineButtonContentDescription,
            "hangupButtonContentDescription" to hangupButtonContentDescription,
            "locale" to locale,
            "use24HourFormat" to use24HourFormat,
            "durationFormat" to durationFormat
        )
    }
    
    /**
     * Convert to Bundle for Activity/Service communication
     */
    fun toBundle(): Bundle {
        return Bundle().apply {
            putString("answerButtonText", answerButtonText)
            putString("declineButtonText", declineButtonText)
            putString("hangupButtonText", hangupButtonText)
            putString("incomingVoiceCallText", incomingVoiceCallText)
            putString("incomingVideoCallText", incomingVideoCallText)
            putString("callInProgressText", callInProgressText)
            putString("tapToReturnText", tapToReturnText)
            putString("ongoingCallText", ongoingCallText)
            putString("voiceCallText", voiceCallText)
            putString("videoCallText", videoCallText)
            putString("incomingCallLabel", incomingCallLabel)
            putString("unknownCallerText", unknownCallerText)
            putString("hangupNotificationTitle", hangupNotificationTitle)
            putString("hangupNotificationContent", hangupNotificationContent)
            putString("hangupNotificationSubText", hangupNotificationSubText)
            putBoolean("showCallDuration", showCallDuration)
            putBoolean("enableTapToReturnToCall", enableTapToReturnToCall)
            putInt("hangupNotificationPriority", hangupNotificationPriority)
            putInt("callTimeoutSeconds", callTimeoutSeconds)
            putBoolean("enableCallTimeout", enableCallTimeout)
            putBoolean("enableVibration", enableVibration)
            putBoolean("enableRingtone", enableRingtone)
            putBoolean("vibrateOnSilentMode", vibrateOnSilentMode)
            putString("accentColor", accentColor)
            putString("backgroundColor", backgroundColor)
            putString("textColor", textColor)
            putString("secondaryTextColor", secondaryTextColor)
            putString("avatarUrl", avatarUrl)
            putBoolean("showAvatarPlaceholder", showAvatarPlaceholder)
            putString("avatarShape", avatarShape)
            putBoolean("useFullScreenCallUI", useFullScreenCallUI)
            putBoolean("showCallerNumber", showCallerNumber)
            putBoolean("showCallType", showCallType)
            putString("notificationChannelName", notificationChannelName)
            putString("notificationChannelDescription", notificationChannelDescription)
            putBoolean("useCustomNotificationSound", useCustomNotificationSound)
            putString("notificationSoundUri", notificationSoundUri)
            putString("answerButtonContentDescription", answerButtonContentDescription)
            putString("declineButtonContentDescription", declineButtonContentDescription)
            putString("hangupButtonContentDescription", hangupButtonContentDescription)
            putString("locale", locale)
            putBoolean("use24HourFormat", use24HourFormat)
            putString("durationFormat", durationFormat)
        }
    }
    
    /**
     * Get the appropriate incoming call text based on video call status
     */
    fun getIncomingCallText(isVideoCall: Boolean): String {
        return if (isVideoCall) incomingVideoCallText else incomingVoiceCallText
    }
    
    /**
     * Get the appropriate call type text
     */
    fun getCallTypeText(isVideoCall: Boolean): String {
        return if (isVideoCall) videoCallText else voiceCallText
    }
    
    /**
     * Get safe color value or fallback to default
     */
    fun getSafeColor(colorValue: String?, fallback: String): String {
        return if (colorValue?.startsWith("#") == true && colorValue.length in 7..9) {
            colorValue
        } else {
            fallback
        }
    }
    
    /**
     * Format call duration based on configuration
     */
    fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        
        return when (durationFormat.lowercase()) {
            "mm:ss" -> String.format("%02d:%02d", minutes, secs)
            "hh:mm:ss" -> String.format("%02d:%02d:%02d", hours, minutes, secs)
            "auto" -> {
                if (hours > 0) {
                    String.format("%02d:%02d:%02d", hours, minutes, secs)
                } else {
                    String.format("%02d:%02d", minutes, secs)
                }
            }
            else -> { // Custom format or fallback
                if (hours > 0) {
                    String.format("%02d:%02d:%02d", hours, minutes, secs)
                } else {
                    String.format("%02d:%02d", minutes, secs)
                }
            }
        }
    }

    companion object {
        /**
         * Create CallUIConfig from a Map (from Flutter method channel)
         */
        @JvmStatic
        fun fromMap(map: Map<String, Any>?): CallUIConfig {
            if (map == null) return CallUIConfig()
            
            return CallUIConfig(
                // Button texts
                answerButtonText = map["answerButtonText"] as? String ?: "Answer",
                declineButtonText = map["declineButtonText"] as? String ?: "Decline",
                hangupButtonText = map["hangupButtonText"] as? String ?: "Hangup",
                
                // Notification texts
                incomingVoiceCallText = map["incomingVoiceCallText"] as? String ?: "Incoming Voice Call",
                incomingVideoCallText = map["incomingVideoCallText"] as? String ?: "Incoming Video Call",
                callInProgressText = map["callInProgressText"] as? String ?: "Call in Progress",
                tapToReturnText = map["tapToReturnText"] as? String ?: "Tap to return to call",
                ongoingCallText = map["ongoingCallText"] as? String ?: "call",
                voiceCallText = map["voiceCallText"] as? String ?: "Voice",
                videoCallText = map["videoCallText"] as? String ?: "Video",
                
                // Call screen texts
                incomingCallLabel = map["incomingCallLabel"] as? String ?: "Incoming Call",
                unknownCallerText = map["unknownCallerText"] as? String ?: "Unknown",
                
                // Hangup notification configuration
                hangupNotificationTitle = map["hangupNotificationTitle"] as? String,
                hangupNotificationContent = map["hangupNotificationContent"] as? String,
                hangupNotificationSubText = map["hangupNotificationSubText"] as? String,
                showCallDuration = map["showCallDuration"] as? Boolean ?: true,
                enableTapToReturnToCall = map["enableTapToReturnToCall"] as? Boolean ?: true,
                hangupNotificationPriority = (map["hangupNotificationPriority"] as? Number)?.toInt() ?: 0,
                
                // Call timeout configuration
                callTimeoutSeconds = (map["callTimeoutSeconds"] as? Number)?.toInt() ?: 60,
                enableCallTimeout = map["enableCallTimeout"] as? Boolean ?: true,
                
                // Audio configuration
                enableVibration = map["enableVibration"] as? Boolean ?: true,
                enableRingtone = map["enableRingtone"] as? Boolean ?: true,
                vibrateOnSilentMode = map["vibrateOnSilentMode"] as? Boolean ?: true,
                
                // Visual customization
                accentColor = map["accentColor"] as? String,
                backgroundColor = map["backgroundColor"] as? String,
                textColor = map["textColor"] as? String,
                secondaryTextColor = map["secondaryTextColor"] as? String,
                
                // Avatar configuration
                avatarUrl = map["avatarUrl"] as? String,
                showAvatarPlaceholder = map["showAvatarPlaceholder"] as? Boolean ?: true,
                avatarShape = map["avatarShape"] as? String ?: "circle",
                
                // Layout configuration
                useFullScreenCallUI = map["useFullScreenCallUI"] as? Boolean ?: true,
                showCallerNumber = map["showCallerNumber"] as? Boolean ?: true,
                showCallType = map["showCallType"] as? Boolean ?: true,
                
                // Advanced notification settings
                notificationChannelName = map["notificationChannelName"] as? String,
                notificationChannelDescription = map["notificationChannelDescription"] as? String,
                useCustomNotificationSound = map["useCustomNotificationSound"] as? Boolean ?: false,
                notificationSoundUri = map["notificationSoundUri"] as? String,
                
                // Accessibility
                answerButtonContentDescription = map["answerButtonContentDescription"] as? String,
                declineButtonContentDescription = map["declineButtonContentDescription"] as? String,
                hangupButtonContentDescription = map["hangupButtonContentDescription"] as? String,
                
                // Localization
                locale = map["locale"] as? String,
                use24HourFormat = map["use24HourFormat"] as? Boolean ?: true,
                durationFormat = map["durationFormat"] as? String ?: "mm:ss"
            )
        }
        
        /**
         * Create from Bundle
         */
        @JvmStatic
        fun fromBundle(bundle: Bundle?): CallUIConfig {
            if (bundle == null) return CallUIConfig()
            
            return CallUIConfig(
                answerButtonText = bundle.getString("answerButtonText") ?: "Answer",
                declineButtonText = bundle.getString("declineButtonText") ?: "Decline",
                hangupButtonText = bundle.getString("hangupButtonText") ?: "Hangup",
                incomingVoiceCallText = bundle.getString("incomingVoiceCallText") ?: "Incoming Voice Call",
                incomingVideoCallText = bundle.getString("incomingVideoCallText") ?: "Incoming Video Call",
                callInProgressText = bundle.getString("callInProgressText") ?: "Call in Progress",
                tapToReturnText = bundle.getString("tapToReturnText") ?: "Tap to return to call",
                ongoingCallText = bundle.getString("ongoingCallText") ?: "call",
                voiceCallText = bundle.getString("voiceCallText") ?: "Voice",
                videoCallText = bundle.getString("videoCallText") ?: "Video",
                incomingCallLabel = bundle.getString("incomingCallLabel") ?: "Incoming Call",
                unknownCallerText = bundle.getString("unknownCallerText") ?: "Unknown",
                hangupNotificationTitle = bundle.getString("hangupNotificationTitle"),
                hangupNotificationContent = bundle.getString("hangupNotificationContent"),
                hangupNotificationSubText = bundle.getString("hangupNotificationSubText"),
                showCallDuration = bundle.getBoolean("showCallDuration", true),
                enableTapToReturnToCall = bundle.getBoolean("enableTapToReturnToCall", true),
                hangupNotificationPriority = bundle.getInt("hangupNotificationPriority", 0),
                callTimeoutSeconds = bundle.getInt("callTimeoutSeconds", 60),
                enableCallTimeout = bundle.getBoolean("enableCallTimeout", true),
                enableVibration = bundle.getBoolean("enableVibration", true),
                enableRingtone = bundle.getBoolean("enableRingtone", true),
                vibrateOnSilentMode = bundle.getBoolean("vibrateOnSilentMode", true),
                accentColor = bundle.getString("accentColor"),
                backgroundColor = bundle.getString("backgroundColor"),
                textColor = bundle.getString("textColor"),
                secondaryTextColor = bundle.getString("secondaryTextColor"),
                avatarUrl = bundle.getString("avatarUrl"),
                showAvatarPlaceholder = bundle.getBoolean("showAvatarPlaceholder", true),
                avatarShape = bundle.getString("avatarShape") ?: "circle",
                useFullScreenCallUI = bundle.getBoolean("useFullScreenCallUI", true),
                showCallerNumber = bundle.getBoolean("showCallerNumber", true),
                showCallType = bundle.getBoolean("showCallType", true),
                notificationChannelName = bundle.getString("notificationChannelName"),
                notificationChannelDescription = bundle.getString("notificationChannelDescription"),
                useCustomNotificationSound = bundle.getBoolean("useCustomNotificationSound", false),
                notificationSoundUri = bundle.getString("notificationSoundUri"),
                answerButtonContentDescription = bundle.getString("answerButtonContentDescription"),
                declineButtonContentDescription = bundle.getString("declineButtonContentDescription"),
                hangupButtonContentDescription = bundle.getString("hangupButtonContentDescription"),
                locale = bundle.getString("locale"),
                use24HourFormat = bundle.getBoolean("use24HourFormat", true),
                durationFormat = bundle.getString("durationFormat") ?: "mm:ss"
            )
        }
    }
}