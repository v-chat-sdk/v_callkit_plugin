package com.v_chat_sdk.v_callkit_plugin

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Person
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.util.UUID

/** VCallkitPlugin */
class VCallkitPlugin: FlutterPlugin, MethodCallHandler {
    
    companion object {
        private const val TAG = "VCallkitPlugin"
        private const val CHANNEL_NAME = "v_callkit_plugin"
        const val CALL_NOTIFICATION_CHANNEL_ID = "incoming_calls"
        const val ONGOING_CALL_NOTIFICATION_CHANNEL_ID = "ongoing_calls"
        const val CALL_NOTIFICATION_ID = 1001
        const val ONGOING_CALL_NOTIFICATION_ID = 1002
        
        // Static reference to method channel for callbacks
        @JvmStatic
        var methodChannel: MethodChannel? = null
        
        // Custom ringtone URI
        @JvmStatic
        var customRingtoneUri: Uri? = null
        
        // Static reference to plugin instance for call management
        @JvmStatic
        var pluginInstance: VCallkitPlugin? = null
        
        // Call action launch data
        @JvmStatic
        private var lastCallActionLaunch: CallActionLaunchData? = null
        
        /**
         * Static method to stop call sounds from anywhere (like BroadcastReceiver)
         */
        @JvmStatic
        fun stopCallSoundsStatic() {
            pluginInstance?.stopCallSounds()
        }
        
        /**
         * Static method to set call action launch data when app is launched from notification
         */
        @JvmStatic
        fun setCallActionLaunchData(action: String, callData: CallData) {
            lastCallActionLaunch = CallActionLaunchData(
                action = action,
                callData = callData,
                timestamp = System.currentTimeMillis()
            )
            Log.d(TAG, "Call action launch data set: $action for ${callData.callerName}")
        }
        
        /**
         * Static method to get and clear call action launch data
         */
        @JvmStatic
        fun getAndClearCallActionLaunchData(): CallActionLaunchData? {
            val data = lastCallActionLaunch
            lastCallActionLaunch = null
            return data
        }
        
        /**
         * Static method to check if app was launched from call action
         */
        @JvmStatic
        fun hasCallActionLaunchData(): Boolean {
            return lastCallActionLaunch != null
        }
        
        /**
         * Static method to show incoming call directly from native context
         * Used for background tasks like WorkManager where Flutter context is not available
         */
        @JvmStatic
        fun showIncomingCallFromNative(context: Context, callData: CallData): Boolean {
            return try {
                val instance = VCallkitPlugin()
                instance.context = context
                instance.initializeNativeServices()
                
                // Create a dummy result callback
                val result = object : Result {
                    override fun success(result: Any?) {
                        Log.d(TAG, "Native incoming call shown successfully")
                    }
                    override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
                        Log.e(TAG, "Native incoming call error: $errorCode - $errorMessage")
                    }
                    override fun notImplemented() {
                        Log.e(TAG, "Native incoming call not implemented")
                    }
                }
                
                instance.showIncomingCallNotification(callData, result)
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error showing native incoming call: ${e.message}")
                false
            }
        }
    }
    
    /**
     * Data class to store call action launch information
     */
    data class CallActionLaunchData(
        val action: String, // "ANSWER" or "DECLINE"
        val callData: CallData,
        val timestamp: Long
    ) {
        fun toMap(): Map<String, Any> {
            return mapOf(
                "action" to action,
                "callData" to callData.toMap(),
                "timestamp" to timestamp
            )
        }
    }
    
    private lateinit var channel: MethodChannel
    private lateinit var context: Context
    private lateinit var notificationManager: NotificationManager
    private lateinit var vibrator: Vibrator
    private lateinit var audioManager: AudioManager
    private lateinit var powerManager: PowerManager
    private var ringtone: Ringtone? = null
    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    
    // Call duration tracking
    private var callStartTime: Long = 0
    private var callDurationTimer: android.os.Handler? = null
    private var callDurationRunnable: Runnable? = null
    private var currentOngoingCallData: CallData? = null
    
    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, CHANNEL_NAME)
        channel.setMethodCallHandler(this)
        
        // Set static references for callbacks
        methodChannel = channel
        pluginInstance = this
        
        // Initialize notification manager
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        
        createNotificationChannels()
    }
    
    /**
     * Initialize native services when called from background context
     */
    private fun initializeNativeServices() {
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        createNotificationChannels()
    }
    
    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "getPlatformVersion" -> {
                result.success("Android ${android.os.Build.VERSION.RELEASE}")
            }
            "hasPermissions" -> {
                result.success(hasRequiredPermissions())
            }
            "requestPermissions" -> {
                handleRequestPermissions(result)
            }
            "showIncomingCall" -> {
                handleShowIncomingCall(call, result)
            }
            "showIncomingCallNative" -> {
                handleShowIncomingCallNative(call, result)
            }
            "endCall" -> {
                handleEndCall(call, result)
            }
            "answerCall" -> {
                handleAnswerCall(call, result)
            }
            "rejectCall" -> {
                handleRejectCall(call, result)
            }
            "muteCall" -> {
                handleMuteCall(call, result)
            }
            "holdCall" -> {
                handleHoldCall(call, result)
            }
            "isCallActive" -> {
                result.success(CallConnectionManager.hasActiveCall())
            }
            "getActiveCallData" -> {
                result.success(CallConnectionManager.getActiveCallData())
            }
            "setCustomRingtone" -> {
                handleSetCustomRingtone(call, result)
            }
            "getSystemRingtones" -> {
                handleGetSystemRingtones(result)
            }
            "checkBatteryOptimization" -> {
                result.success(isBatteryOptimizationIgnored())
            }
            "requestBatteryOptimization" -> {
                handleRequestBatteryOptimization(result)
            }
            "getDeviceManufacturer" -> {
                result.success(Build.MANUFACTURER.lowercase())
            }
            "getLastCallActionLaunch" -> {
                handleGetLastCallActionLaunch(result)
            }
            "hasCallActionLaunchData" -> {
                result.success(hasCallActionLaunchData())
            }
            "clearCallActionLaunchData" -> {
                handleClearCallActionLaunchData(result)
            }
            "forceShowOngoingNotification" -> {
                handleForceShowOngoingNotification(call, result)
            }
            "showHangupNotification" -> {
                handleShowHangupNotification(call, result)
            }
            "hideHangupNotification" -> {
                handleHideHangupNotification(result)
            }
            "updateHangupNotification" -> {
                handleUpdateHangupNotification(call, result)
            }
            "launchHangupNotificationWithForegroundService" -> {
                handleLaunchHangupNotificationWithForegroundService(call, result)
            }
            "startOutgoingCallNotification" -> {
                handleStartOutgoingCallNotification(call, result)
            }
            "startIncomingCallNotification" -> {
                handleStartIncomingCallNotification(call, result)
            }
            "stopCallForegroundService" -> {
                handleStopCallForegroundService(result)
            }
            "updateCallForegroundService" -> {
                handleUpdateCallForegroundService(call, result)
            }
            "setUIConfiguration" -> {
                handleSetUIConfiguration(call, result)
            }
            "getCallManagerDebugInfo" -> {
                handleGetCallManagerDebugInfo(result)
            }
            else -> {
                result.notImplemented()
            }
        }
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ringtoneUri = customRingtoneUri ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            
            // Incoming calls channel (high priority with sound)
            val incomingChannel = NotificationChannel(
                CALL_NOTIFICATION_CHANNEL_ID,
                "Incoming Calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for incoming VoIP calls"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                
                // Set audio attributes for ringtone
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .build()
                setSound(ringtoneUri, audioAttributes)
                
                // Make it bypass Do Not Disturb
                setBypassDnd(true)
            }
            
            // Ongoing calls channel (default priority, no sound, non-dismissible)
            val ongoingChannel = NotificationChannel(
                ONGOING_CALL_NOTIFICATION_CHANNEL_ID,
                "Ongoing Calls",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Non-dismissible notifications for active calls with duration timer. Required for proper call functionality."
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                enableVibration(false)
                enableLights(false)
                setSound(null, null) // No sound for ongoing calls
                setBypassDnd(false) // Don't need to bypass DND for ongoing calls
                // Note: Android automatically makes foreground service notifications non-dismissible
            }
            
            notificationManager.createNotificationChannel(incomingChannel)
            notificationManager.createNotificationChannel(ongoingChannel)
            Log.d(TAG, "Notification channels created: incoming and ongoing calls")
        }
    }
    
    private fun hasRequiredPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == 
                PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
    
    private fun handleRequestPermissions(result: Result) {
        result.success(hasRequiredPermissions())
    }
    
    private fun handleShowIncomingCall(call: MethodCall, result: Result) {
        val arguments = call.arguments as? Map<String, Any>
        if (arguments == null) {
            result.error("INVALID_ARGUMENTS", "Call arguments required", null)
            return
        }
        
        val callData = parseCallData(arguments)
        if (callData == null) {
            result.error("INVALID_CALL_DATA", "Valid call data required", null)
            return
        }
        
        showIncomingCallNotification(callData, result)
    }
    
    private fun handleShowIncomingCallNative(call: MethodCall, result: Result) {
        val arguments = call.arguments as? Map<String, Any>
        if (arguments == null) {
            result.error("INVALID_ARGUMENTS", "Call arguments required", null)
            return
        }
        
        val callData = parseCallData(arguments)
        if (callData == null) {
            result.error("INVALID_CALL_DATA", "Valid call data required", null)
            return
        }
        
        Log.d(TAG, "Native incoming call request for: ${callData.callerName}")
        
        // Use the static method to show incoming call from native context
        val success = showIncomingCallFromNative(context, callData)
        
        if (success) {
            Log.d(TAG, "Native incoming call shown successfully for: ${callData.callerName}")
            result.success(true)
        } else {
            Log.e(TAG, "Failed to show native incoming call for: ${callData.callerName}")
            result.error("NATIVE_CALL_ERROR", "Failed to show native incoming call", null)
        }
    }
    
    private fun parseCallData(arguments: Map<String, Any>): CallData? {
        return try {
            CallData(
                id = arguments["id"] as? String ?: UUID.randomUUID().toString(),
                callerName = arguments["callerName"] as? String ?: "Unknown",
                callerNumber = arguments["callerNumber"] as? String ?: "",
                callerAvatar = arguments["callerAvatar"] as? String,
                isVideoCall = arguments["isVideoCall"] as? Boolean ?: false,
                extra = arguments["extra"] as? Map<String, Any> ?: emptyMap()
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private fun showIncomingCallNotification(callData: CallData, result: Result) {
        try {
            // Store call data for callback handling
            CallConnectionManager.setIncomingCallData(callData)
            
            // Acquire wake lock for call
            acquireCallWakeLock()
            
            // Start playing ringtone immediately for MIUI/EMUI devices
            if (isCustomChineseRom()) {
                startRingtonePlayback()
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Use CallStyle for Android 12 and above
                showCallStyleNotification(callData, result)
            } else {
                // Use regular notification for older Android versions
                showLegacyCallNotification(callData, result)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error showing call notification: ${e.message}")
            releaseCallWakeLock()
            stopRingtonePlayback()
            result.error("NOTIFICATION_ERROR", "Failed to show call notification: ${e.message}", null)
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.S)
    private fun showCallStyleNotification(callData: CallData, result: Result) {
        // Create Person object for the caller
        val caller = Person.Builder()
            .setName(callData.callerName)
            .setImportant(true)
            .build()
        
        // Create full-screen intent for incoming call
        val fullScreenIntent = createCallScreenIntent(callData)
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            fullScreenIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create answer action - directly launch app
        val answerIntent = createMainActivityLaunchIntent("ANSWER", callData)
        val answerPendingIntent = PendingIntent.getActivity(
            context, 
            1, 
            answerIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create decline action - handle silently without launching app
        val declineIntent = createCallActionIntent("DECLINE", callData.id)
        val declinePendingIntent = PendingIntent.getBroadcast(
            context, 
            2, 
            declineIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create CallStyle notification
        val notification = Notification.Builder(context, CALL_NOTIFICATION_CHANNEL_ID)
            .setContentTitle(callData.callerName)
            .setContentText(callData.callerNumber)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setStyle(
                Notification.CallStyle.forIncomingCall(caller, declinePendingIntent, answerPendingIntent)
            )
            .addPerson(caller)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_CALL)
            .setAutoCancel(false)
            .setOnlyAlertOnce(false)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)
            .setColorized(true)
            .setColor(ContextCompat.getColor(context, android.R.color.holo_green_dark))
            .build()
        
        // Show the notification
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            notificationManager.notify(CALL_NOTIFICATION_ID, notification)
            
            // Start vibration manually for better control
            startCallVibration()
            
            Log.d(TAG, "CallStyle notification shown for: ${callData.callerName}")
            result.success(true)
        } else {
            result.error("NOTIFICATIONS_DISABLED", "Notifications are disabled for this app", null)
        }
    }
    
    private fun showLegacyCallNotification(callData: CallData, result: Result) {
        // Create full-screen intent for incoming call
        val fullScreenIntent = createCallScreenIntent(callData)
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            fullScreenIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create answer action - directly launch app
        val answerIntent = createMainActivityLaunchIntent("ANSWER", callData)
        val answerPendingIntent = PendingIntent.getActivity(
            context, 
            1, 
            answerIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create decline action - handle silently without launching app
        val declineIntent = createCallActionIntent("DECLINE", callData.id)
        val declinePendingIntent = PendingIntent.getBroadcast(
            context, 
            2, 
            declineIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Get ringtone URI
        val ringtoneUri = customRingtoneUri ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        
        // Build the notification
        val notificationBuilder = NotificationCompat.Builder(context, CALL_NOTIFICATION_CHANNEL_ID)
            .setContentTitle(callData.callerName)
            .setContentText("Incoming ${if (callData.isVideoCall) "video" else "voice"} call")
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(false)
            .setOngoing(true)
            .setTimeoutAfter(60000) // Auto dismiss after 60 seconds
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel, 
                "Decline", 
                declinePendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_call, 
                "Answer", 
                answerPendingIntent
            )
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColorized(true) // For API 30 and below to get higher priority
            .setColor(ContextCompat.getColor(context, android.R.color.holo_green_dark))
            
        // Add sound and vibration for older Android versions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder
                .setSound(ringtoneUri, AudioManager.STREAM_RING)
                .setVibrate(longArrayOf(0, 1000, 500, 1000))
        }
        
        val notification = notificationBuilder.build()
        
        // Make notification non-dismissible by user swipe (heads-up only)
        notification.flags = notification.flags or Notification.FLAG_NO_CLEAR or Notification.FLAG_ONGOING_EVENT
        
        // Show the notification
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            notificationManager.notify(CALL_NOTIFICATION_ID, notification)
            
            // Start vibration manually for better control
            startCallVibration()
            
            Log.d(TAG, "Legacy call notification shown for: ${callData.callerName}")
            result.success(true)
        } else {
            result.error("NOTIFICATIONS_DISABLED", "Notifications are disabled for this app", null)
        }
    }
    
    private fun createCallScreenIntent(callData: CallData): Intent {
        // Use IncomingCallActivity for better system integration
        return Intent(context, IncomingCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                   Intent.FLAG_ACTIVITY_NO_USER_ACTION or
                   Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("call_data", callData.toBundle())
        }
    }
    
    // No longer needed - using CallData's built-in methods
    
    private fun createCallActionIntent(action: String, callId: String): Intent {
        return Intent(context, CallActionReceiver::class.java).apply {
            putExtra("action", action)
            putExtra("callId", callId)
        }
    }
    
    /**
     * Creates an intent to launch the CallActionActivity which handles proper call action processing
     * This ensures the app launches even when completely closed/terminated
     */
    private fun createMainActivityLaunchIntent(action: String, callData: CallData): Intent {
        return CallActionActivity.createIntent(context, action, callData)
    }
    
    private fun handleEndCall(call: MethodCall, result: Result) {
        val callId = call.arguments as? String
        dismissCallNotification()
        if (CallConnectionManager.endCall(callId)) {
            result.success(true)
        } else {
            result.error("NO_ACTIVE_CALL", "No active call to end", null)
        }
    }
    
    private fun dismissCallNotification() {
        notificationManager.cancel(CALL_NOTIFICATION_ID)
        notificationManager.cancel(ONGOING_CALL_NOTIFICATION_ID)
        stopCallSounds()
        stopCallDurationTimer()
        
        // Stop foreground service if running
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CallForegroundService.stopService(context)
        }
    }
    
    /**
     * Stop ringtone and vibration without dismissing notification
     * Used when call is answered (notification stays but sounds stop)
     */
    fun stopCallSounds() {
        stopCallVibration()
        stopRingtonePlayback()
        releaseCallWakeLock()
    }
    
    private fun startCallVibration() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
            val vibrationEffect = VibrationEffect.createWaveform(pattern, 0) // 0 means repeat
            vibrator.vibrate(vibrationEffect)
        } else {
            @Suppress("DEPRECATION")
            val pattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
            vibrator.vibrate(pattern, 0) // 0 means repeat
        }
    }
    
    private fun stopCallVibration() {
        vibrator.cancel()
    }
    
    private fun handleSetCustomRingtone(call: MethodCall, result: Result) {
        val ringtoneUriString = call.arguments as? String
        
        if (ringtoneUriString != null) {
            try {
                customRingtoneUri = Uri.parse(ringtoneUriString)
                // Recreate notification channels with new ringtone
                createNotificationChannels()
                Log.d(TAG, "Custom ringtone set: $ringtoneUriString")
                result.success(true)
            } catch (e: Exception) {
                Log.e(TAG, "Error setting custom ringtone: ${e.message}")
                result.error("INVALID_RINGTONE", "Invalid ringtone URI: ${e.message}", null)
            }
        } else {
            // Reset to default ringtone
            customRingtoneUri = null
            createNotificationChannels()
            Log.d(TAG, "Reset to default ringtone")
            result.success(true)
        }
    }
    
    private fun handleGetSystemRingtones(result: Result) {
        try {
            val ringtoneManager = RingtoneManager(context)
            ringtoneManager.setType(RingtoneManager.TYPE_RINGTONE)
            
            val cursor = ringtoneManager.cursor
            val ringtones = mutableListOf<Map<String, String>>()
            
            // Add default ringtone
            val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ringtones.add(mapOf(
                "title" to "Default Ringtone",
                "uri" to defaultUri.toString(),
                "isDefault" to "true"
            ))
            
            // Add system ringtones
            while (cursor.moveToNext()) {
                val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                val uri = ringtoneManager.getRingtoneUri(cursor.position)
                
                ringtones.add(mapOf(
                    "title" to title,
                    "uri" to uri.toString(),
                    "isDefault" to "false"
                ))
            }
            
            cursor.close()
            result.success(ringtones)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting system ringtones: ${e.message}")
            result.error("RINGTONE_ERROR", "Failed to get system ringtones: ${e.message}", null)
        }
    }
    
    private fun handleAnswerCall(call: MethodCall, result: Result) {
        val callId = call.arguments as? String
        
        Log.d(TAG, "Handling answer call from Flutter for call ID: $callId")
        
        // Stop sounds and dismiss incoming call notification
        stopCallSounds()
        notificationManager.cancel(CALL_NOTIFICATION_ID)
        
        if (CallConnectionManager.answerCall(callId)) {
            // Show ongoing call notification if available
            val callData = CallConnectionManager.getCallData(callId ?: "")
            if (callData != null) {
                Log.d(TAG, "Found call data, showing ongoing notification for: ${callData.callerName}")
                forceShowOngoingNotification(callData)
            } else {
                Log.e(TAG, "No call data found for answered call: $callId")
                
                // Try to get from incoming call data as fallback
                val incomingData = CallConnectionManager.getIncomingCallData()
                if (incomingData != null) {
                    Log.d(TAG, "Using incoming call data for ongoing notification")
                    forceShowOngoingNotification(incomingData)
                }
            }
            result.success(true)
        } else {
            result.error("NO_ACTIVE_CALL", "No active call to answer", null)
        }
    }
    
    private fun handleRejectCall(call: MethodCall, result: Result) {
        val callId = call.arguments as? String
        
        // Stop sounds and dismiss all call notifications
        dismissCallNotification()
        
        if (CallConnectionManager.rejectCall(callId)) {
            result.success(true)
        } else {
            result.error("NO_ACTIVE_CALL", "No active call to reject", null)
        }
    }
    
    private fun handleMuteCall(call: MethodCall, result: Result) {
        val arguments = call.arguments as? Map<String, Any>
        val callId = arguments?.get("callId") as? String
        val isMuted = arguments?.get("isMuted") as? Boolean ?: false
        
        if (CallConnectionManager.muteCall(callId, isMuted)) {
            result.success(true)
        } else {
            result.error("NO_ACTIVE_CALL", "No active call to mute/unmute", null)
        }
    }
    
    private fun handleHoldCall(call: MethodCall, result: Result) {
        val arguments = call.arguments as? Map<String, Any>
        val callId = arguments?.get("callId") as? String
        val isOnHold = arguments?.get("isOnHold") as? Boolean ?: false
        
        if (CallConnectionManager.holdCall(callId, isOnHold)) {
            result.success(true)
        } else {
            result.error("NO_ACTIVE_CALL", "No active call to hold/unhold", null)
        }
    }
    
    private fun handleGetLastCallActionLaunch(result: Result) {
        val launchData = getAndClearCallActionLaunchData()
        if (launchData != null) {
            result.success(launchData.toMap())
        } else {
            result.success(null)
        }
    }
    
    private fun handleClearCallActionLaunchData(result: Result) {
        getAndClearCallActionLaunchData()
        result.success(true)
    }
    
    private fun handleForceShowOngoingNotification(call: MethodCall, result: Result) {
        val arguments = call.arguments as? Map<String, Any>
        if (arguments == null) {
            result.error("INVALID_ARGUMENTS", "Call arguments required", null)
            return
        }
        
        val callData = parseCallData(arguments)
        if (callData == null) {
            result.error("INVALID_CALL_DATA", "Valid call data required", null)
            return
        }
        
        Log.d(TAG, "Force show ongoing notification request from Flutter for: ${callData.callerName}")
        
        try {
            forceShowOngoingNotification(callData)
            result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error force showing ongoing notification: ${e.message}")
            result.error("NOTIFICATION_ERROR", "Failed to show ongoing notification: ${e.message}", null)
        }
    }
    
    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        methodChannel = null
        pluginInstance = null
        stopRingtonePlayback()
        releaseCallWakeLock()
        stopCallDurationTimer()
    }
    
    // Helper methods for custom ROM detection
    private fun isCustomChineseRom(): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return manufacturer in listOf("xiaomi", "redmi", "poco", "huawei", "honor", "oppo", "vivo", "realme", "oneplus")
    }
    
    private fun isMiui(): Boolean {
        return !getSystemProperty("ro.miui.ui.version.name").isNullOrEmpty()
    }
    
    private fun isEmui(): Boolean {
        return !getSystemProperty("ro.build.version.emui").isNullOrEmpty()
    }
    
    private fun getSystemProperty(key: String): String? {
        return try {
            val process = Runtime.getRuntime().exec("getprop $key")
            process.inputStream.bufferedReader().use { it.readText().trim() }
        } catch (e: Exception) {
            null
        }
    }
    
    // Battery optimization handling
    private fun isBatteryOptimizationIgnored(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true
        }
    }
    
    private fun handleRequestBatteryOptimization(result: Result) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!isBatteryOptimizationIgnored()) {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    result.success(true)
                } catch (e: Exception) {
                    // Some devices don't support this intent
                    Log.e(TAG, "Battery optimization request failed: ${e.message}")
                    result.error("BATTERY_OPT_ERROR", "Cannot request battery optimization: ${e.message}", null)
                }
            } else {
                result.success(true)
            }
        } else {
            result.success(true)
        }
    }
    
    // Wake lock management
    private fun acquireCallWakeLock() {
        if (wakeLock == null || !wakeLock!!.isHeld) {
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "${context.packageName}:IncomingCall"
            ).apply {
                acquire(60000) // 60 seconds timeout
            }
            Log.d(TAG, "Wake lock acquired for incoming call")
        }
    }
    
    private fun releaseCallWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
                Log.d(TAG, "Wake lock released")
            }
        }
        wakeLock = null
    }
    
    // Enhanced ringtone playback for custom ROMs
    private fun startRingtonePlayback() {
        try {
            // Stop any existing playback
            stopRingtonePlayback()
            
            val ringtoneUri = customRingtoneUri ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            
            // For MIUI/EMUI, use MediaPlayer for better control
            if (isCustomChineseRom()) {
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                            .setLegacyStreamType(AudioManager.STREAM_RING)
                            .build()
                    )
                    
                    // Set data source and prepare
                    setDataSource(context, ringtoneUri)
                    isLooping = true
                    
                    // Ensure volume is at max for ringtone stream
                    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)
                    audioManager.setStreamVolume(AudioManager.STREAM_RING, maxVolume, 0)
                    
                    prepare()
                    start()
                    
                    Log.d(TAG, "MediaPlayer ringtone started for ${Build.MANUFACTURER}")
                }
            } else {
                // Use standard Ringtone for other devices
                ringtone = RingtoneManager.getRingtone(context, ringtoneUri).apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        isLooping = true
                    }
                    play()
                    Log.d(TAG, "Standard ringtone started")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting ringtone: ${e.message}")
        }
    }
    
    private fun stopRingtonePlayback() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            mediaPlayer = null
            
            ringtone?.let {
                if (it.isPlaying) {
                    it.stop()
                }
            }
            ringtone = null
            
            Log.d(TAG, "Ringtone playback stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping ringtone: ${e.message}")
        }
    }
    
    /**
     * Shows an ongoing call notification after call is answered
     * 
     * This notification is designed to be NON-DISMISSIBLE according to Android's
     * foreground service notification behavior. As per Android documentation:
     * "A notification is required when your app is running a foreground service...
     * This notification can't be dismissed like other notifications."
     * 
     * The notification will only be removed when:
     * - The call ends and the foreground service is stopped
     * - The app explicitly dismisses it through CallConnectionManager.endCall()
     * 
     * @param callData The call information to display in the notification
     */
    fun showOngoingCallNotification(callData: CallData) {
        try {
            Log.d(TAG, "Starting ongoing call notification for: ${callData.callerName}")
            
            // Store current call data and start duration tracking
            currentOngoingCallData = callData
            
            // Ensure the call is marked as answered in the connection manager
            CallConnectionManager.answerCall(callData.id)
            
            // For Android 8+ (API 26+), use foreground service for better persistence
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d(TAG, "Starting foreground service for ongoing call")
                CallForegroundService.startService(context, callData)
            }
            
            // Start the duration timer
            startCallDurationTimer()
            
            // Show initial notification
            updateOngoingCallNotification(0)
            
            Log.d(TAG, "Ongoing call notification shown successfully for: ${callData.callerName}")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing ongoing call notification: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Starts the call duration timer that updates the notification every second
     */
    private fun startCallDurationTimer() {
        stopCallDurationTimer() // Stop any existing timer
        
        callStartTime = System.currentTimeMillis()
        callDurationTimer = android.os.Handler(android.os.Looper.getMainLooper())
        
        callDurationRunnable = object : Runnable {
            override fun run() {
                val durationSeconds = (System.currentTimeMillis() - callStartTime) / 1000
                updateOngoingCallNotification(durationSeconds)
                
                // Schedule next update in 1 second
                callDurationTimer?.postDelayed(this, 1000)
            }
        }
        
        // Start the timer
        callDurationRunnable?.let { callDurationTimer?.post(it) }
        Log.d(TAG, "Call duration timer started")
    }
    
    /**
     * Stops the call duration timer
     */
    private fun stopCallDurationTimer() {
        callDurationRunnable?.let { callDurationTimer?.removeCallbacks(it) }
        callDurationRunnable = null
        callDurationTimer = null
        currentOngoingCallData = null
        Log.d(TAG, "Call duration timer stopped")
    }
    
    /**
     * Public method to stop call duration timer (called from external classes)
     */
    fun stopCallTimer() {
        stopCallDurationTimer()
    }
    
    /**
     * Force show ongoing call notification (public method for external access)
     */
    fun forceShowOngoingNotification(callData: CallData) {
        Log.d(TAG, "Force showing ongoing notification for: ${callData.callerName}")
        showOngoingCallNotification(callData)
    }
    
    /**
     * Updates the ongoing call notification with current duration
     * 
     * Uses appropriate notification style based on Android version:
     * - Android 12+: CallStyle notifications (non-dismissible with proper API support)
     * - Android 8-11: Legacy notifications with foreground service association
     * - Android 7 and below: Basic persistent notifications
     */
    private fun updateOngoingCallNotification(durationSeconds: Long) {
        val callData = currentOngoingCallData ?: return
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Use CallStyle for ongoing call in Android 12+ (API 31+)
                showOngoingCallStyleNotification(callData, durationSeconds)
                Log.d(TAG, "Updated CallStyle notification with duration: ${formatCallDuration(durationSeconds)}")
            } else {
                // Use regular ongoing notification for Android 11 and below
                showLegacyOngoingCallNotification(callData, durationSeconds)
                Log.d(TAG, "Updated legacy notification with duration: ${formatCallDuration(durationSeconds)}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating ongoing call notification: ${e.message}")
            e.printStackTrace()
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.S)
    private fun showOngoingCallStyleNotification(callData: CallData, durationSeconds: Long) {
        // Create Person object for the caller
        val caller = Person.Builder()
            .setName(callData.callerName)
            .setImportant(true)
            .build()
        
        // Create hang up action
        val hangupIntent = createCallActionIntent("HANGUP", callData.id)
        val hangupPendingIntent = PendingIntent.getBroadcast(
            context,
            3,
            hangupIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create app launch intent for tapping the notification
        val appLaunchIntent = createMainActivityLaunchIntent("RETURN_TO_CALL", callData)
        val appLaunchPendingIntent = PendingIntent.getActivity(
            context,
            4,
            appLaunchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Format duration
        val durationText = formatCallDuration(durationSeconds)
        
        // Create ongoing call notification with CallStyle - CRITICAL: setOngoing(true) must be in builder
        val builder = Notification.Builder(context, ONGOING_CALL_NOTIFICATION_CHANNEL_ID)
            .setContentTitle(callData.callerName)
            .setContentText("${if (callData.isVideoCall) "Video" else "Voice"} call • $durationText")
            .setSubText("Tap to return to call")
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setContentIntent(appLaunchPendingIntent)
            .setStyle(
                Notification.CallStyle.forOngoingCall(caller, hangupPendingIntent)
            )
            .addPerson(caller)
            .setCategory(Notification.CATEGORY_CALL)
            .setOnlyAlertOnce(true)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setColorized(true)
            .setColor(ContextCompat.getColor(context, android.R.color.holo_green_dark))
            .setWhen(System.currentTimeMillis() - (durationSeconds * 1000))
            .setUsesChronometer(true)
            .setChronometerCountDown(false)
            .setShowWhen(true)
            // CRITICAL: For Android 14+ CallStyle notifications to be non-dismissible
            .setOngoing(true)
        
        // For Android 14+ (API 34+), add the FLAG_ONGOING_EVENT to make it non-dismissible
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14+ specific: Use FLAG_ONGOING_EVENT with CallStyle for non-dismissible behavior
            val notification = builder.build()
            notification.flags = notification.flags or Notification.FLAG_ONGOING_EVENT
            notificationManager.notify(ONGOING_CALL_NOTIFICATION_ID, notification)
            Log.d(TAG, "Android 14+ CallStyle ongoing notification shown (non-dismissible)")
        } else {
            // Android 12-13: CallStyle with foreground service association for high priority
            val notification = builder.build()
            notification.flags = notification.flags or 
                Notification.FLAG_NO_CLEAR or 
                Notification.FLAG_FOREGROUND_SERVICE
            notificationManager.notify(ONGOING_CALL_NOTIFICATION_ID, notification)
            Log.d(TAG, "Android 12-13 CallStyle ongoing notification shown with foreground service")
        }
    }
    
    private fun showLegacyOngoingCallNotification(callData: CallData, durationSeconds: Long) {
        // Create hang up action
        val hangupIntent = createCallActionIntent("HANGUP", callData.id)
        val hangupPendingIntent = PendingIntent.getBroadcast(
            context,
            3,
            hangupIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create app launch intent for tapping the notification
        val appLaunchIntent = createMainActivityLaunchIntent("RETURN_TO_CALL", callData)
        val appLaunchPendingIntent = PendingIntent.getActivity(
            context,
            4,
            appLaunchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Format duration
        val durationText = formatCallDuration(durationSeconds)
        
        // Build the ongoing call notification with duration - for Android 11 and below
        val notification = NotificationCompat.Builder(context, ONGOING_CALL_NOTIFICATION_CHANNEL_ID)
            .setContentTitle(callData.callerName)
            .setContentText("${if (callData.isVideoCall) "Video" else "Voice"} call • $durationText")
            .setSubText("Tap to return to call")
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true) // CRITICAL: Must be set in builder for proper behavior
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setContentIntent(appLaunchPendingIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Hang Up",
                hangupPendingIntent
            )
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColorized(true) // Important for Android 30 and earlier to achieve high ranking
            .setColor(ContextCompat.getColor(context, android.R.color.holo_green_dark))
            .setWhen(System.currentTimeMillis() - (durationSeconds * 1000))
            .setUsesChronometer(true)
            .setChronometerCountDown(false)
            .setShowWhen(true)
            .build()
        
        // Make the notification non-dismissible by setting appropriate flags
        // These flags work in conjunction with the foreground service
        notification.flags = notification.flags or 
            Notification.FLAG_NO_CLEAR or 
            Notification.FLAG_ONGOING_EVENT or
            Notification.FLAG_FOREGROUND_SERVICE
        
        // Show the notification
        notificationManager.notify(ONGOING_CALL_NOTIFICATION_ID, notification)
        Log.d(TAG, "Legacy ongoing notification shown with foreground service association (non-dismissible)")
    }
    
    /**
     * Creates an ongoing notification for use by the foreground service
     * This ensures the notification is compatible with foreground service requirements
     * and follows Android's CallStyle/non-dismissible notification guidelines
     */
    fun createOngoingNotificationForService(callData: CallData, durationSeconds: Long, callType: String = "ongoing"): android.app.Notification? {
        return try {
            Log.d(TAG, "Creating foreground service notification for: ${callData.callerName}")
            
            // For Android 12+ (API 31+), use CallStyle with proper foreground service
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Create Person object for the caller
                val caller = Person.Builder()
                    .setName(callData.callerName)
                    .setImportant(true)
                    .build()
                
                // Create hang up action
                val hangupIntent = createCallActionIntent("HANGUP", callData.id)
                val hangupPendingIntent = PendingIntent.getBroadcast(
                    context,
                    3,
                    hangupIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                // Create app launch intent
                val appLaunchIntent = createMainActivityLaunchIntent("RETURN_TO_CALL", callData)
                val appLaunchPendingIntent = PendingIntent.getActivity(
                    context,
                    4,
                    appLaunchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                val durationText = formatCallDuration(durationSeconds)
                val callTypeText = when (callType) {
                    "outgoing" -> "Outgoing ${if (callData.isVideoCall) "video" else "voice"} call"
                    "incoming" -> "Incoming ${if (callData.isVideoCall) "video" else "voice"} call"
                    else -> "${if (callData.isVideoCall) "Video" else "Voice"} call"
                }
                
                // Use CallStyle for Android 12+ foreground service
                val notification = Notification.Builder(context, ONGOING_CALL_NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(callData.callerName)
                    .setContentText("Tap to return to call")
                    .setSubText("$callTypeText • $durationText")
                    .setSmallIcon(android.R.drawable.ic_menu_call)
                    .setContentIntent(appLaunchPendingIntent)
                    .setStyle(
                        Notification.CallStyle.forOngoingCall(caller, hangupPendingIntent)
                    )
                    .addPerson(caller)
                    .setCategory(Notification.CATEGORY_CALL)
                    .setOnlyAlertOnce(true)
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setColorized(true)
                    .setColor(ContextCompat.getColor(context, android.R.color.holo_green_dark))
                    .setWhen(System.currentTimeMillis() - (durationSeconds * 1000))
                    .setUsesChronometer(true)
                    .setChronometerCountDown(false)
                    .setShowWhen(true)
                    .setOngoing(true) // CRITICAL for non-dismissible behavior
                    .setAutoCancel(false) // Prevent dismissal
                    .setDefaults(0) // No sound, vibration for ongoing calls
                    .build()
                
                // Apply flags based on Android version
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    notification.flags = notification.flags or Notification.FLAG_ONGOING_EVENT
                } else {
                    notification.flags = notification.flags or 
                        Notification.FLAG_NO_CLEAR or 
                        Notification.FLAG_FOREGROUND_SERVICE
                }
                
                Log.d(TAG, "CallStyle foreground service notification created")
                notification
            } else {
                // Legacy notification for Android 11 and below
                val hangupIntent = createCallActionIntent("HANGUP", callData.id)
                val hangupPendingIntent = PendingIntent.getBroadcast(
                    context,
                    3,
                    hangupIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                val appLaunchIntent = createMainActivityLaunchIntent("RETURN_TO_CALL", callData)
                val appLaunchPendingIntent = PendingIntent.getActivity(
                    context,
                    4,
                    appLaunchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                val durationText = formatCallDuration(durationSeconds)
                val callTypeText = when (callType) {
                    "outgoing" -> "Outgoing ${if (callData.isVideoCall) "video" else "voice"} call"
                    "incoming" -> "Incoming ${if (callData.isVideoCall) "video" else "voice"} call"
                    else -> "${if (callData.isVideoCall) "Video" else "Voice"} call"
                }
                
                val notification = NotificationCompat.Builder(context, ONGOING_CALL_NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(callData.callerName)
                    .setContentText("Tap to return to call")
                    .setSubText("$callTypeText • $durationText")
                    .setSmallIcon(android.R.drawable.ic_menu_call)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setCategory(NotificationCompat.CATEGORY_CALL)
                    .setOngoing(true) // CRITICAL for non-dismissible behavior
                    .setAutoCancel(false)
                    .setOnlyAlertOnce(true)
                    .setContentIntent(appLaunchPendingIntent)
                    .addAction(
                        android.R.drawable.ic_menu_close_clear_cancel,
                        "Hang Up",
                        hangupPendingIntent
                    )
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setColorized(true) // Important for high priority on Android 30 and earlier
                    .setColor(ContextCompat.getColor(context, android.R.color.holo_green_dark))
                    .setWhen(System.currentTimeMillis() - (durationSeconds * 1000))
                    .setUsesChronometer(true)
                    .setChronometerCountDown(false)
                    .setShowWhen(true)
                    .setDefaults(0) // No sound, vibration for ongoing calls
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText("Tap to return to call")
                            .setBigContentTitle(callData.callerName)
                            .setSummaryText("$callTypeText • $durationText")
                    )
                    .build()
                
                // Set flags for non-dismissible foreground service behavior
                notification.flags = notification.flags or 
                    android.app.Notification.FLAG_NO_CLEAR or 
                    android.app.Notification.FLAG_FOREGROUND_SERVICE or
                    android.app.Notification.FLAG_ONGOING_EVENT
                
                Log.d(TAG, "Legacy foreground service notification created")
                notification
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating foreground service notification: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Formats call duration in seconds to a readable string (MM:SS or HH:MM:SS)
     */
    private fun formatCallDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format("%02d:%02d", minutes, secs)
        }
    }
    
    // Hangup Notification Handler Methods
    
    private fun handleShowHangupNotification(call: MethodCall, result: Result) {
        val arguments = call.arguments as? Map<String, Any>
        if (arguments == null) {
            result.error("INVALID_ARGUMENTS", "Call arguments required", null)
            return
        }
        
        val callData = parseCallData(arguments)
        if (callData == null) {
            result.error("INVALID_CALL_DATA", "Valid call data required", null)
            return
        }
        
        try {
            // Use CallForegroundService for hangup notifications since it provides the same functionality
            CallForegroundService.startService(context, callData)
            Log.d(TAG, "Call foreground service started for hangup notification: ${callData.callerName}")
            result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting hangup notification: ${e.message}")
            result.error("HANGUP_NOTIFICATION_ERROR", "Failed to show hangup notification: ${e.message}", null)
        }
    }
    
    private fun handleHideHangupNotification(result: Result) {
        try {
            // Use CallForegroundService for hangup notifications since it provides the same functionality
            CallForegroundService.stopService(context)
            Log.d(TAG, "Call foreground service stopped for hangup notification")
            result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping hangup notification: ${e.message}")
            result.error("HANGUP_NOTIFICATION_ERROR", "Failed to hide hangup notification: ${e.message}", null)
        }
    }
    
    private fun handleUpdateHangupNotification(call: MethodCall, result: Result) {
        val arguments = call.arguments as? Map<String, Any>
        if (arguments == null) {
            result.error("INVALID_ARGUMENTS", "Call arguments required", null)
            return
        }
        
        val callData = parseCallData(arguments)
        if (callData == null) {
            result.error("INVALID_CALL_DATA", "Valid call data required", null)
            return
        }
        
        try {
            // Use CallForegroundService for hangup notifications since it provides the same functionality
            CallForegroundService.updateService(context, callData)
            Log.d(TAG, "Call foreground service updated for hangup notification: ${callData.callerName}")
            result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating hangup notification: ${e.message}")
            result.error("HANGUP_NOTIFICATION_ERROR", "Failed to update hangup notification: ${e.message}", null)
        }
    }
    
    /**
     * Special method to demonstrate launching hangup notification with the exact same
     * foreground service pattern as when accepting calls from notifications.
     * 
     * This method uses CallForegroundService which provides:
     * 1. The same service robustness and lifecycle management
     * 2. Non-dismissible notification flags
     * 3. Live duration timer updates
     * 4. App backgrounding and termination survival
     * 5. The exact pattern used when accepting calls from notifications
     */
    private fun handleLaunchHangupNotificationWithForegroundService(call: MethodCall, result: Result) {
        val arguments = call.arguments as? Map<String, Any>
        if (arguments == null) {
            result.error("INVALID_ARGUMENTS", "Call arguments required", null)
            return
        }
        
        val callData = parseCallData(arguments)
        if (callData == null) {
            result.error("INVALID_CALL_DATA", "Valid call data required", null)
            return
        }
        
        try {
            Log.d(TAG, "Launching hangup notification with foreground service pattern (same as call acceptance) for: ${callData.callerName}")
            
            // Use CallForegroundService which provides the exact same pattern as when a call is accepted from notification:
            // 1. Start CallForegroundService with non-dismissible notification
            // 2. Create notification with proper flags and hangup button
            // 3. Handle app backgrounding and system optimization
            // 4. Provide live duration timer updates
            // 5. Ensure service survives app termination
            
            CallForegroundService.startService(context, callData)
            
            // Log the service startup to show it follows the same pattern
            Log.d(TAG, "CallForegroundService started with foreground service robustness")
            Log.d(TAG, "Service characteristics:")
            Log.d(TAG, "- Foreground service with non-dismissible notification")
            Log.d(TAG, "- Live timer updates every second")
            Log.d(TAG, "- Survives app backgrounding and termination") 
            Log.d(TAG, "- Same notification flags as ongoing call notifications")
            Log.d(TAG, "- Same service lifecycle management as call acceptance")
            
            result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error launching hangup notification with foreground service pattern: ${e.message}")
            result.error("HANGUP_FOREGROUND_SERVICE_ERROR", "Failed to launch hangup notification with foreground service pattern: ${e.message}", null)
        }
    }
    
    // New Outgoing Call Notification Handler Methods
    
    /**
     * Start foreground service for outgoing calls
     * This creates a persistent notification for outgoing calls once they are accepted
     */
    private fun handleStartOutgoingCallNotification(call: MethodCall, result: Result) {
        val arguments = call.arguments as? Map<String, Any>
        if (arguments == null) {
            result.error("INVALID_ARGUMENTS", "Call arguments required", null)
            return
        }
        
        val callData = parseCallData(arguments)
        if (callData == null) {
            result.error("INVALID_CALL_DATA", "Valid call data required", null)
            return
        }
        
        try {
            CallForegroundService.startOutgoingCallService(context, callData)
            Log.d(TAG, "Outgoing call foreground service started for: ${callData.callerName}")
            result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting outgoing call notification: ${e.message}")
            result.error("OUTGOING_CALL_ERROR", "Failed to start outgoing call notification: ${e.message}", null)
        }
    }
    
    /**
     * Start foreground service for incoming calls
     * This provides explicit control over incoming call notifications
     */
    private fun handleStartIncomingCallNotification(call: MethodCall, result: Result) {
        val arguments = call.arguments as? Map<String, Any>
        if (arguments == null) {
            result.error("INVALID_ARGUMENTS", "Call arguments required", null)
            return
        }
        
        val callData = parseCallData(arguments)
        if (callData == null) {
            result.error("INVALID_CALL_DATA", "Valid call data required", null)
            return
        }
        
        try {
            CallForegroundService.startIncomingCallService(context, callData)
            Log.d(TAG, "Incoming call foreground service started for: ${callData.callerName}")
            result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting incoming call notification: ${e.message}")
            result.error("INCOMING_CALL_ERROR", "Failed to start incoming call notification: ${e.message}", null)
        }
    }
    
    /**
     * Stop the call foreground service
     * This stops any active foreground service and removes the persistent notification
     */
    private fun handleStopCallForegroundService(result: Result) {
        try {
            CallForegroundService.stopService(context)
            Log.d(TAG, "Call foreground service stopped")
            result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping call foreground service: ${e.message}")
            result.error("STOP_SERVICE_ERROR", "Failed to stop call foreground service: ${e.message}", null)
        }
    }
    
    /**
     * Update the call foreground service with new call data
     * This updates the persistent notification with new information while keeping the service running
     */
    private fun handleUpdateCallForegroundService(call: MethodCall, result: Result) {
        val arguments = call.arguments as? Map<String, Any>
        if (arguments == null) {
            result.error("INVALID_ARGUMENTS", "Call arguments required", null)
            return
        }
        
        val callData = parseCallData(arguments)
        if (callData == null) {
            result.error("INVALID_CALL_DATA", "Valid call data required", null)
            return
        }
        
        try {
            CallForegroundService.updateService(context, callData)
            Log.d(TAG, "Call foreground service updated for: ${callData.callerName}")
            result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating call foreground service: ${e.message}")
            result.error("UPDATE_SERVICE_ERROR", "Failed to update call foreground service: ${e.message}", null)
        }
    }
    
    /**
     * Set global UI configuration
     */
    private fun handleSetUIConfiguration(call: MethodCall, result: Result) {
        val arguments = call.arguments as? Map<String, Any>
        if (arguments == null) {
            result.error("INVALID_ARGUMENTS", "UI configuration required", null)
            return
        }
        
        try {
            // Store UI configuration for future use
            // This can be used to customize notification appearance, text, etc.
            Log.d(TAG, "UI configuration set: $arguments")
            result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting UI configuration: ${e.message}")
            result.error("UI_CONFIG_ERROR", "Failed to set UI configuration: ${e.message}", null)
        }
    }
    
    /**
     * Get call manager debug information
     */
    private fun handleGetCallManagerDebugInfo(result: Result) {
        try {
            val debugInfo = mapOf(
                "hasActiveCall" to CallConnectionManager.hasActiveCall(),
                "activeCallData" to (CallConnectionManager.getActiveCallData() ?: "null"),
                "androidVersion" to Build.VERSION.SDK_INT,
                "hasCallForegroundService" to (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O),
                "deviceManufacturer" to Build.MANUFACTURER,
                "deviceModel" to Build.MODEL,
                "pluginVersion" to "1.0.0"
            )
            
            Log.d(TAG, "Debug info requested: $debugInfo")
            result.success(debugInfo)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting debug info: ${e.message}")
            result.error("DEBUG_INFO_ERROR", "Failed to get debug info: ${e.message}", null)
        }
    }
}
