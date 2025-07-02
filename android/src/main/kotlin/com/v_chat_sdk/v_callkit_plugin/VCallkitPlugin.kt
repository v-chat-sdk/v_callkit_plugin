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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Icon
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
// Note: MediaPlayer and Ringtone imports removed - using system-managed notification ringtone
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
import androidx.core.graphics.drawable.IconCompat
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import kotlin.math.min

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
        
        // UI Configuration storage for translations and customizations
        @JvmStatic
        private var uiConfiguration: Map<String, Any> = emptyMap()
        
        /**
         * Static method to get translated text with fallback to default English
         */
        @JvmStatic
        fun getTranslatedText(key: String, fallback: String): String {
            return uiConfiguration[key] as? String ?: fallback
        }
        
        /**
         * Static method to get UI configuration value
         */
        @JvmStatic
        fun getUIConfigValue(key: String, fallback: Any): Any {
            return uiConfiguration[key] ?: fallback
        }
        
        /**
         * Static method to set UI configuration
         */
        @JvmStatic
        fun setUIConfiguration(config: Map<String, Any>) {
            uiConfiguration = config
            Log.d(TAG, "UI Configuration updated: ${config.keys}")
        }
        
        /**
         * Note: stopCallSoundsStatic removed - system now manages ringtone lifecycle automatically
         * Notification dismissal automatically stops ringtone
         */
        
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
    private var wakeLock: PowerManager.WakeLock? = null
    // Note: ringtone and mediaPlayer removed - system handles ringtone automatically
    
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
            "hasPermissions" -> {
                result.success(hasRequiredPermissions())
            }
            "requestPermissions" -> {
                handleRequestPermissions(result)
            }
            "showIncomingCallWithConfig" -> {
                handleShowIncomingCallWithConfig(call, result)
            }
            "answerCall" -> {
                handleAnswerCall(call, result)
            }
            "isCallActive" -> {
                result.success(CallConnectionManager.hasActiveCall())
            }
            "getActiveCallData" -> {
                result.success(CallConnectionManager.getActiveCallData())
            }
            "startOutgoingCallNotification" -> {
                handleStartOutgoingCallNotification(call, result)
            }
            "stopCallForegroundService" -> {
                handleStopCallForegroundService(result)
            }
            else -> {
                result.notImplemented()
            }
        }
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ringtoneUri = customRingtoneUri ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            
            // Incoming calls channel with SYSTEM-MANAGED ringtone
            val incomingChannel = NotificationChannel(
                CALL_NOTIFICATION_CHANNEL_ID,
                "Incoming Calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for incoming VoIP calls - system managed audio"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                
                // USE SYSTEM RINGTONE - automatic lifecycle management
                setSound(ringtoneUri, AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setLegacyStreamType(AudioManager.STREAM_RING)
                    .build())
                
                Log.d(TAG, "Notification channel using system-managed ringtone for automatic lifecycle")
                
                // Respect Do Not Disturb settings - let user control this in system settings
                setBypassDnd(false)
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
            }
            
            notificationManager.createNotificationChannel(incomingChannel)
            notificationManager.createNotificationChannel(ongoingChannel)
            Log.d(TAG, "Notification channels created: system-managed ringtone for incoming, silent for ongoing")
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
            
            // System notification handles ringtone automatically - no manual management needed
            Log.d(TAG, "Using system-managed ringtone - automatic start/stop with notification lifecycle")
            
            // Load avatar asynchronously and show notification
            loadAvatarAsync(callData) { avatarIcon ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    // Use CallStyle for Android 12 and above
                    showCallStyleNotification(callData, result, avatarIcon)
                } else {
                    // Use regular notification for older Android versions
                    showLegacyCallNotification(callData, result, avatarIcon)
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error showing call notification: ${e.message}")
            releaseCallWakeLock()
            result.error("NOTIFICATION_ERROR", "Failed to show call notification: ${e.message}", null)
        }
    }
    
    /**
     * Load avatar image asynchronously from URL or generate from initials
     * Executes callback with the resulting Icon (Android 7+) or null for older versions
     */
    private fun loadAvatarAsync(callData: CallData, callback: (Icon?) -> Unit) {
        // For Android versions below 7.0, Icon class is not available
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            callback(null)
            return
        }
        
        Thread {
            try {
                val avatarIcon = if (!callData.callerAvatar.isNullOrEmpty()) {
                    // Try to load avatar from URL
                    loadAvatarFromUrl(callData.callerAvatar!!) ?: generateInitialsAvatar(callData.callerName)
                } else {
                    // Generate avatar from initials
                    generateInitialsAvatar(callData.callerName)
                }
                
                // Execute callback on main thread
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    callback(avatarIcon)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading avatar: ${e.message}")
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    callback(generateInitialsAvatar(callData.callerName))
                }
            }
        }.start()
    }
    
    /**
     * Load avatar image from URL
     * Returns Icon for Android 7+ or null if loading fails
     */
    private fun loadAvatarFromUrl(avatarUrl: String): Icon? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return null
        
        return try {
            Log.d(TAG, "Loading avatar from URL: $avatarUrl")
            
            val url = URL(avatarUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 5000 // 5 second timeout
            connection.readTimeout = 10000 // 10 second timeout
            connection.doInput = true
            connection.connect()
            
            val inputStream: InputStream = connection.inputStream
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            connection.disconnect()
            
            if (bitmap != null) {
                val circularBitmap = createCircularBitmap(bitmap)
                Log.d(TAG, "Avatar loaded successfully from URL")
                Icon.createWithBitmap(circularBitmap)
            } else {
                Log.w(TAG, "Failed to decode bitmap from URL")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load avatar from URL: ${e.message}")
            null
        }
    }
    
    /**
     * Generate circular avatar from user's initials
     * Returns Icon for Android 7+ or null for older versions
     */
    private fun generateInitialsAvatar(name: String): Icon? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return null
        
        return try {
            val initials = extractInitials(name)
            val bitmap = createInitialsBitmap(initials)
            val circularBitmap = createCircularBitmap(bitmap)
            
            Log.d(TAG, "Generated initials avatar for: $name -> $initials")
            Icon.createWithBitmap(circularBitmap)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate initials avatar: ${e.message}")
            null
        }
    }
    
    /**
     * Extract initials from a person's name
     */
    private fun extractInitials(name: String): String {
        val words = name.trim().split("\\s+".toRegex())
        return when {
            words.isEmpty() -> "?"
            words.size == 1 -> words[0].take(2).uppercase()
            else -> "${words.first().take(1)}${words.last().take(1)}".uppercase()
        }
    }
    
    /**
     * Create a bitmap with initials text
     */
    private fun createInitialsBitmap(initials: String): Bitmap {
        val size = 256 // Avatar size in pixels
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Background color - use a gradient of colors based on initials
        val backgroundColor = generateColorFromInitials(initials)
        canvas.drawColor(backgroundColor)
        
        // Text paint
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = size * 0.4f // 40% of avatar size
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        
        // Calculate text position (center)
        val textBounds = Rect()
        textPaint.getTextBounds(initials, 0, initials.length, textBounds)
        val x = size / 2f
        val y = size / 2f + textBounds.height() / 2f
        
        // Draw text
        canvas.drawText(initials, x, y, textPaint)
        
        return bitmap
    }
    
    /**
     * Generate a consistent color from initials
     */
    private fun generateColorFromInitials(initials: String): Int {
        val colors = intArrayOf(
            Color.parseColor("#FF6B6B"), // Red
            Color.parseColor("#4ECDC4"), // Teal
            Color.parseColor("#45B7D1"), // Blue
            Color.parseColor("#FFA07A"), // Light Salmon
            Color.parseColor("#98D8C8"), // Mint
            Color.parseColor("#F7DC6F"), // Yellow
            Color.parseColor("#BB8FCE"), // Purple
            Color.parseColor("#85C1E9"), // Light Blue
            Color.parseColor("#F8C471"), // Orange
            Color.parseColor("#82E0AA")  // Green
        )
        
        val hash = initials.hashCode()
        return colors[Math.abs(hash) % colors.size]
    }
    
    /**
     * Create a circular bitmap from any bitmap
     */
    private fun createCircularBitmap(bitmap: Bitmap): Bitmap {
        val size = min(bitmap.width, bitmap.height)
        val circularBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(circularBitmap)
        
        // Create circular path
        val paint = Paint().apply {
            isAntiAlias = true
        }
        
        val radius = size / 2f
        canvas.drawCircle(radius, radius, radius, paint)
        
        // Use source bitmap as mask
        paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
        
        // Scale and center the original bitmap
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, size, size, true)
        canvas.drawBitmap(scaledBitmap, 0f, 0f, paint)
        
        return circularBitmap
    }
    
    @RequiresApi(Build.VERSION_CODES.S)
    private fun showCallStyleNotification(callData: CallData, result: Result, avatarIcon: Icon?) {
        // Create Person object for the caller with avatar
        val caller = Person.Builder()
            .setName(callData.callerName)
            .setImportant(true)
            .apply {
                // Add avatar if available (Android 7+ only)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && avatarIcon != null) {
                    setIcon(avatarIcon)
                    Log.d(TAG, "CallStyle notification: Avatar set for ${callData.callerName}")
                } else {
                    Log.d(TAG, "CallStyle notification: No avatar available for ${callData.callerName}")
                }
            }
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
            .apply {
                // Set large icon (avatar) if available
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && avatarIcon != null) {
                    setLargeIcon(avatarIcon)
                    Log.d(TAG, "CallStyle notification: Large icon (avatar) set")
                }
            }
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
            
            Log.d(TAG, "CallStyle notification shown for: ${callData.callerName} with avatar support")
            result.success(true)
        } else {
            result.error("NOTIFICATIONS_DISABLED", "Notifications are disabled for this app", null)
        }
    }
    
    private fun showLegacyCallNotification(callData: CallData, result: Result, avatarIcon: Icon?) {
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
        
        // Get translated text
        val incomingCallText = if (callData.isVideoCall) {
            getTranslatedText("incomingVideoCallText", "Incoming video call")
        } else {
            getTranslatedText("incomingVoiceCallText", "Incoming voice call")
        }
        val answerText = getTranslatedText("answerButtonText", "Answer")
        val declineText = getTranslatedText("declineButtonText", "Decline")
        
        // Build the notification
        val notificationBuilder = NotificationCompat.Builder(context, CALL_NOTIFICATION_CHANNEL_ID)
            .setContentTitle(callData.callerName)
            .setContentText(incomingCallText)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .apply {
                // Set large icon (avatar) if available (Android API 16+ supports large icons)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && avatarIcon != null) {
                    // Convert Icon to Bitmap for NotificationCompat
                    try {
                        val bitmap = iconToBitmap(avatarIcon)
                        if (bitmap != null) {
                            setLargeIcon(bitmap)
                            Log.d(TAG, "Legacy notification: Large icon (avatar) set for ${callData.callerName}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to convert icon to bitmap: ${e.message}")
                    }
                } else {
                    Log.d(TAG, "Legacy notification: No avatar available for ${callData.callerName}")
                }
            }
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(false)
            .setOngoing(true)
            .setTimeoutAfter(60000) // Auto dismiss after 60 seconds
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel, 
                declineText, 
                declinePendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_call, 
                answerText, 
                answerPendingIntent
            )
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColorized(true) // For API 30 and below to get higher priority
            .setColor(ContextCompat.getColor(context, android.R.color.holo_green_dark))
            
        // Add sound and vibration for older Android versions (respecting system settings)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            // CRITICAL FIX: Disable notification sound for ALL Android versions to prevent double playback
            // Manual ringtone handling provides better control across all devices and API levels
            notificationBuilder.setSound(null)
            
            // Only add vibration if system allows vibration
            if (shouldVibrate()) {
                notificationBuilder.setVibrate(longArrayOf(0, 1000, 500, 1000))
            }
        }
        
        val notification = notificationBuilder.build()
        
        // Make notification non-dismissible by user swipe (heads-up only)
        notification.flags = notification.flags or Notification.FLAG_NO_CLEAR or Notification.FLAG_ONGOING_EVENT
        
        // Show the notification
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            notificationManager.notify(CALL_NOTIFICATION_ID, notification)
            
            // Start vibration manually for better control
            startCallVibration()
            
            Log.d(TAG, "Legacy call notification shown for: ${callData.callerName} with avatar support")
            result.success(true)
        } else {
            result.error("NOTIFICATIONS_DISABLED", "Notifications are disabled for this app", null)
        }
    }
    
    /**
     * Convert Icon to Bitmap for compatibility with NotificationCompat
     * Used for legacy notifications on older Android versions
     */
    private fun iconToBitmap(icon: Icon): Bitmap? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return null
        
        return try {
            val drawable = icon.loadDrawable(context)
            if (drawable != null) {
                val bitmap = Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to convert Icon to Bitmap: ${e.message}")
            null
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
    
    private fun dismissCallNotification() {
        notificationManager.cancel(CALL_NOTIFICATION_ID)
        notificationManager.cancel(ONGOING_CALL_NOTIFICATION_ID)
        stopCallDurationTimer()
        
        // Stop foreground service if running
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CallForegroundService.stopService(context)
        }
    }
    
    /**
     * Note: stopCallSounds removed - system now manages ringtone automatically
     * Only need to handle vibration and wake lock cleanup manually
     */
    private fun stopCallVibrationAndWakeLock() {
        stopCallVibration()
        releaseCallWakeLock()
        Log.d(TAG, "Stopped vibration and released wake lock - ringtone handled by system")
    }
    
    private fun startCallVibration() {
        try {
            // Check if vibration should be played based on system settings
            if (!shouldVibrate()) {
                Log.d(TAG, "Vibration skipped due to system settings (silent mode, DND, or vibration disabled)")
                return
            }

            // Enhanced vibration for background context - use longer pattern for better visibility
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val pattern = longArrayOf(0, 1000, 300, 1000, 300, 1000, 300, 1000)
                val vibrationEffect = VibrationEffect.createWaveform(pattern, 0) // 0 means repeat
                
                // Use AUDIO_ATTRIBUTES for better background compatibility
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .build()
                
                @Suppress("DEPRECATION") // VibrationEffect.createWaveform with AudioAttributes is newer
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Android 13+ - Use createWaveform with AudioAttributes for better background support
                    try {
                        val enhancedEffect = VibrationEffect.createWaveform(pattern, -1)
                        vibrator.vibrate(enhancedEffect, audioAttributes)
                        Log.d(TAG, "Enhanced call vibration started for Android 13+ (background-compatible)")
                    } catch (e: Exception) {
                        // Fallback to standard method
                        vibrator.vibrate(vibrationEffect)
                        Log.d(TAG, "Fallback call vibration started for Android 13+ (${e.message})")
                    }
                } else {
                    vibrator.vibrate(vibrationEffect)
                    Log.d(TAG, "Call vibration started for Android 8+ (respecting system settings)")
                }
            } else {
                @Suppress("DEPRECATION")
                val pattern = longArrayOf(0, 1000, 300, 1000, 300, 1000, 300, 1000)
                vibrator.vibrate(pattern, 0) // 0 means repeat
                Log.d(TAG, "Call vibration started (legacy mode, respecting system settings)")
            }
            
            // Log vibration success for debugging
            Log.d(TAG, "Vibration successfully triggered - pattern will repeat until stopped")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error starting call vibration: ${e.message}")
            e.printStackTrace()
            
            // Try alternative vibration method for background processes
            tryAlternativeVibration()
        }
    }
    
    /**
     * Alternative vibration method for background processes or when main method fails
     */
    private fun tryAlternativeVibration() {
        try {
            Log.d(TAG, "Attempting alternative vibration method for background context")
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Use a simple one-shot vibration that's more likely to work in background
                val vibrationEffect = VibrationEffect.createOneShot(2000, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(vibrationEffect)
                
                // Chain multiple one-shots for repeated effect
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    try {
                        vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
                    } catch (e: Exception) {
                        Log.w(TAG, "Secondary vibration failed: ${e.message}")
                    }
                }, 2500)
                
                Log.d(TAG, "Alternative vibration method started (one-shot for background)")
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(2000)
                Log.d(TAG, "Alternative vibration method started (legacy one-shot)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Alternative vibration method also failed: ${e.message}")
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
        
        // Dismiss incoming call notification (system automatically stops ringtone)
        notificationManager.cancel(CALL_NOTIFICATION_ID)
        Log.d(TAG, "Notification dismissed - system automatically stopped ringtone")
        
        if (CallConnectionManager.answerCall(callId)) {
            // NOTE: No automatic ongoing notification - controlled from Flutter side
            Log.d(TAG, "Call answered successfully - notification control delegated to Flutter: $callId")
            result.success(true)
        } else {
            result.error("NO_ACTIVE_CALL", "No active call to answer", null)
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
        // Note: Removed stopRingtonePlayback() - system handles ringtone automatically
        releaseCallWakeLock()
        stopCallDurationTimer()
    }
    
    // Helper methods for custom ROM detection (for informational purposes only)
    // Note: These methods are no longer used for ringtone control as we now use unified ringtone handling
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
    
    // Helper methods for system audio state checking
    
    // Note: shouldPlayRingtone method removed - system notification automatically respects:
    // - Silent mode and Do Not Disturb settings
    // - Ringtone volume levels
    // - User configuration for enableRingtone (handled via notification channel creation)
    
    /**
     * Checks if vibration should be activated based on system settings and user configuration
     * Respects ringer mode and Do Not Disturb settings
     */
    private fun shouldVibrate(): Boolean {
        try {
            Log.d(TAG, "=== VIBRATION CHECK DEBUG START ===")
            
            // Check if vibration is disabled in UI configuration
            val enableVibration = getUIConfigValue("enableVibration", true) as? Boolean ?: true
            Log.d(TAG, "UI Config enableVibration: $enableVibration")
            if (!enableVibration) {
                Log.d(TAG, "Vibration blocked: Disabled in UI configuration")
                return false
            }
            
            val ringerMode = audioManager.ringerMode
            Log.d(TAG, "Ringer mode: $ringerMode (NORMAL=2, VIBRATE=1, SILENT=0)")
            
            // Allow vibration in vibrate mode, block in silent mode
            if (ringerMode == AudioManager.RINGER_MODE_SILENT) {
                Log.d(TAG, "Vibration blocked: Phone is in silent mode")
                return false
            }
            
            // Check Do Not Disturb status (API 23+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val interruptionFilter = notificationManager.currentInterruptionFilter
                Log.d(TAG, "DND Interruption filter: $interruptionFilter")
                
                // Block vibration in strict DND modes
                if (interruptionFilter == NotificationManager.INTERRUPTION_FILTER_NONE) {
                    Log.d(TAG, "Vibration blocked: Do Not Disturb - Total silence mode")
                    return false
                }
                
                if (interruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALARMS) {
                    Log.d(TAG, "Vibration blocked: Do Not Disturb - Alarms only mode")
                    return false
                }
                
                // In priority mode, check if calls are allowed
                if (interruptionFilter == NotificationManager.INTERRUPTION_FILTER_PRIORITY) {
                    Log.d(TAG, "DND Priority mode detected, checking call permissions...")
                    if (notificationManager.isNotificationPolicyAccessGranted) {
                        val policy = notificationManager.notificationPolicy
                        val callsAllowed = (policy.priorityCategories and NotificationManager.Policy.PRIORITY_CATEGORY_CALLS) != 0
                        Log.d(TAG, "Calls allowed in priority mode: $callsAllowed")
                        if (!callsAllowed) {
                            Log.d(TAG, "Vibration blocked: Do Not Disturb - Priority mode without calls")
                            return false
                        }
                    } else {
                        Log.d(TAG, "Vibration blocked: Do Not Disturb - Priority mode (no policy access)")
                        return false
                    }
                }
            }
            
            // Check if device has vibrator capability
            val hasVibrator = vibrator.hasVibrator()
            Log.d(TAG, "Device has vibrator: $hasVibrator")
            if (!hasVibrator) {
                Log.d(TAG, "Vibration blocked: Device has no vibrator")
                return false
            }
            
            // For API 26+, check if vibration is enabled in settings
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    val vibrationSetting = Settings.System.getInt(
                        context.contentResolver,
                        Settings.System.VIBRATE_WHEN_RINGING,
                        1
                    )
                    Log.d(TAG, "System vibration setting: $vibrationSetting (1=enabled, 0=disabled)")
                    if (vibrationSetting == 0) {
                        Log.d(TAG, "Vibration blocked: System vibration for calls is disabled")
                        return false
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Could not check vibration setting: ${e.message}")
                }
            }
            
            // Check vibrator permissions
            val hasVibratePermission = ContextCompat.checkSelfPermission(
                context, 
                Manifest.permission.VIBRATE
            ) == PackageManager.PERMISSION_GRANTED
            Log.d(TAG, "VIBRATE permission granted: $hasVibratePermission")
            
            Log.d(TAG, "=== VIBRATION CHECK RESULT: ALLOWED ===")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error checking vibration conditions: ${e.message}")
            e.printStackTrace()
            return false
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
    
    // Note: Manual ringtone management removed - now using system-managed notification ringtone
    // System automatically handles ringtone lifecycle when notification is shown/dismissed
    
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
            
            // Load avatar and show initial notification
            loadAvatarAsync(callData) { avatarIcon ->
                updateOngoingCallNotification(0, avatarIcon)
            }
            
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
        
        // Load avatar once and reuse it for timer updates
        val callData = currentOngoingCallData
        if (callData != null) {
            loadAvatarAsync(callData) { avatarIcon ->
                callDurationRunnable = object : Runnable {
                    override fun run() {
                        val durationSeconds = (System.currentTimeMillis() - callStartTime) / 1000
                        updateOngoingCallNotification(durationSeconds, avatarIcon)
                        
                        // Schedule next update in 1 second
                        callDurationTimer?.postDelayed(this, 1000)
                    }
                }
                
                // Start the timer
                callDurationRunnable?.let { callDurationTimer?.post(it) }
                Log.d(TAG, "Call duration timer started with avatar support")
            }
        } else {
            Log.e(TAG, "Cannot start duration timer: no current call data")
        }
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
    private fun updateOngoingCallNotification(durationSeconds: Long, avatarIcon: Icon? = null) {
        val callData = currentOngoingCallData ?: return
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Use CallStyle for ongoing call in Android 12+ (API 31+)
                showOngoingCallStyleNotification(callData, durationSeconds, avatarIcon)
                Log.d(TAG, "Updated CallStyle notification with duration: ${formatCallDuration(durationSeconds)}")
            } else {
                // Use regular ongoing notification for Android 11 and below
                showLegacyOngoingCallNotification(callData, durationSeconds, avatarIcon)
                Log.d(TAG, "Updated legacy notification with duration: ${formatCallDuration(durationSeconds)}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating ongoing call notification: ${e.message}")
            e.printStackTrace()
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.S)
    private fun showOngoingCallStyleNotification(callData: CallData, durationSeconds: Long, avatarIcon: Icon? = null) {
        // Create Person object for the caller with avatar
        val caller = Person.Builder()
            .setName(callData.callerName)
            .setImportant(true)
            .apply {
                // Add avatar if available (Android 7+ only)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && avatarIcon != null) {
                    setIcon(avatarIcon)
                    Log.d(TAG, "Ongoing CallStyle notification: Avatar set for ${callData.callerName}")
                }
            }
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
        
        // Get translated text
        val callTypeText = if (callData.isVideoCall) {
            getTranslatedText("videoCallText", "Video")
        } else {
            getTranslatedText("voiceCallText", "Voice")
        }
        val ongoingText = getTranslatedText("ongoingCallText", "call")
        val tapToReturnText = getTranslatedText("tapToReturnText", "Tap to return to call")
        
        // Create ongoing call notification with CallStyle - CRITICAL: setOngoing(true) must be in builder
        val builder = Notification.Builder(context, ONGOING_CALL_NOTIFICATION_CHANNEL_ID)
            .setContentTitle(callData.callerName)
            .setContentText("$callTypeText $ongoingText • $durationText")
            .setSubText(tapToReturnText)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .apply {
                // Set large icon (avatar) if available
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && avatarIcon != null) {
                    setLargeIcon(avatarIcon)
                    Log.d(TAG, "Ongoing CallStyle notification: Large icon (avatar) set")
                }
            }
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
            Log.d(TAG, "Android 14+ CallStyle ongoing notification shown with avatar (non-dismissible)")
        } else {
            // Android 12-13: CallStyle with foreground service association for high priority
            val notification = builder.build()
            notification.flags = notification.flags or 
                Notification.FLAG_NO_CLEAR or 
                Notification.FLAG_FOREGROUND_SERVICE
            notificationManager.notify(ONGOING_CALL_NOTIFICATION_ID, notification)
            Log.d(TAG, "Android 12-13 CallStyle ongoing notification shown with avatar and foreground service")
        }
    }
    
    private fun showLegacyOngoingCallNotification(callData: CallData, durationSeconds: Long, avatarIcon: Icon? = null) {
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
        
        // Get translated text
        val callTypeText = if (callData.isVideoCall) {
            getTranslatedText("videoCallText", "Video")
        } else {
            getTranslatedText("voiceCallText", "Voice")
        }
        val ongoingText = getTranslatedText("ongoingCallText", "call")
        val tapToReturnText = getTranslatedText("tapToReturnText", "Tap to return to call")
        val hangupText = getTranslatedText("hangupButtonText", "Hang Up")
        
        // Build the ongoing call notification with duration - for Android 11 and below
        val notification = NotificationCompat.Builder(context, ONGOING_CALL_NOTIFICATION_CHANNEL_ID)
            .setContentTitle(callData.callerName)
            .setContentText("$callTypeText $ongoingText • $durationText")
            .setSubText(tapToReturnText)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .apply {
                // Set large icon (avatar) if available (Android API 16+ supports large icons)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && avatarIcon != null) {
                    // Convert Icon to Bitmap for NotificationCompat
                    try {
                        val bitmap = iconToBitmap(avatarIcon)
                        if (bitmap != null) {
                            setLargeIcon(bitmap)
                            Log.d(TAG, "Legacy ongoing notification: Large icon (avatar) set for ${callData.callerName}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to convert icon to bitmap for ongoing notification: ${e.message}")
                    }
                } else {
                    Log.d(TAG, "Legacy ongoing notification: No avatar available for ${callData.callerName}")
                }
            }
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true) // CRITICAL: Must be set in builder for proper behavior
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setContentIntent(appLaunchPendingIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                hangupText,
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
        Log.d(TAG, "Legacy ongoing notification shown with avatar support (non-dismissible)")
    }
    
    /**
     * Creates an ongoing notification for use by the foreground service
     * This ensures the notification is compatible with foreground service requirements
     * and follows Android's CallStyle/non-dismissible notification guidelines
     */
    fun createOngoingNotificationForService(callData: CallData, durationSeconds: Long, callType: String = "ongoing"): android.app.Notification? {
        return try {
            Log.d(TAG, "Creating foreground service notification for: ${callData.callerName}")
            
            // For foreground service, we need to create the notification synchronously
            // so we'll generate an initials avatar if no URL avatar is available
            val avatarIcon = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (!callData.callerAvatar.isNullOrEmpty()) {
                    // Try to load from URL, but fallback to initials if it fails
                    loadAvatarFromUrl(callData.callerAvatar!!) ?: generateInitialsAvatar(callData.callerName)
                } else {
                    generateInitialsAvatar(callData.callerName)
                }
            } else {
                null
            }
            
            // For Android 12+ (API 31+), use CallStyle with proper foreground service
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Create Person object for the caller with avatar
                val caller = Person.Builder()
                    .setName(callData.callerName)
                    .setImportant(true)
                    .apply {
                        if (avatarIcon != null) {
                            setIcon(avatarIcon)
                            Log.d(TAG, "Foreground service CallStyle: Avatar set for ${callData.callerName}")
                        }
                    }
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
                
                // Get translated text for different call types
                val callTypeText = when (callType) {
                    "outgoing" -> {
                        val outgoingText = getTranslatedText("outgoingCallText", "Outgoing")
                        val callText = if (callData.isVideoCall) {
                            getTranslatedText("videoCallText", "video")
                        } else {
                            getTranslatedText("voiceCallText", "voice")
                        }
                        val ongoingText = getTranslatedText("ongoingCallText", "call")
                        "$outgoingText $callText $ongoingText"
                    }
                    "incoming" -> {
                        val incomingText = getTranslatedText("incomingCallText", "Incoming")
                        val callText = if (callData.isVideoCall) {
                            getTranslatedText("videoCallText", "video")
                        } else {
                            getTranslatedText("voiceCallText", "voice")
                        }
                        val ongoingText = getTranslatedText("ongoingCallText", "call")
                        "$incomingText $callText $ongoingText"
                    }
                    else -> {
                        val callText = if (callData.isVideoCall) {
                            getTranslatedText("videoCallText", "Video")
                        } else {
                            getTranslatedText("voiceCallText", "Voice")
                        }
                        val ongoingText = getTranslatedText("ongoingCallText", "call")
                        "$callText $ongoingText"
                    }
                }
                val tapToReturnText = getTranslatedText("tapToReturnText", "Tap to return to call")
                
                // Use CallStyle for Android 12+ foreground service
                val notification = Notification.Builder(context, ONGOING_CALL_NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(callData.callerName)
                    .setContentText(tapToReturnText)
                    .setSubText("$callTypeText • $durationText")
                    .setSmallIcon(android.R.drawable.ic_menu_call)
                    .apply {
                        // Set large icon (avatar) if available
                        if (avatarIcon != null) {
                            setLargeIcon(avatarIcon)
                            Log.d(TAG, "Foreground service CallStyle: Large icon (avatar) set")
                        }
                    }
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
                
                Log.d(TAG, "CallStyle foreground service notification created with avatar support")
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
                
                // Get translated text for different call types
                val callTypeText = when (callType) {
                    "outgoing" -> {
                        val outgoingText = getTranslatedText("outgoingCallText", "Outgoing")
                        val callText = if (callData.isVideoCall) {
                            getTranslatedText("videoCallText", "video")
                        } else {
                            getTranslatedText("voiceCallText", "voice")
                        }
                        val ongoingText = getTranslatedText("ongoingCallText", "call")
                        "$outgoingText $callText $ongoingText"
                    }
                    "incoming" -> {
                        val incomingText = getTranslatedText("incomingCallText", "Incoming")
                        val callText = if (callData.isVideoCall) {
                            getTranslatedText("videoCallText", "video")
                        } else {
                            getTranslatedText("voiceCallText", "voice")
                        }
                        val ongoingText = getTranslatedText("ongoingCallText", "call")
                        "$incomingText $callText $ongoingText"
                    }
                    else -> {
                        val callText = if (callData.isVideoCall) {
                            getTranslatedText("videoCallText", "Video")
                        } else {
                            getTranslatedText("voiceCallText", "Voice")
                        }
                        val ongoingText = getTranslatedText("ongoingCallText", "call")
                        "$callText $ongoingText"
                    }
                }
                val tapToReturnText = getTranslatedText("tapToReturnText", "Tap to return to call")
                val hangupText = getTranslatedText("hangupButtonText", "Hang Up")
                
                val notification = NotificationCompat.Builder(context, ONGOING_CALL_NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(callData.callerName)
                    .setContentText(tapToReturnText)
                    .setSubText("$callTypeText • $durationText")
                    .setSmallIcon(android.R.drawable.ic_menu_call)
                    .apply {
                        // Set large icon (avatar) if available
                        if (avatarIcon != null) {
                            try {
                                val bitmap = iconToBitmap(avatarIcon)
                                if (bitmap != null) {
                                    setLargeIcon(bitmap)
                                    Log.d(TAG, "Legacy foreground service: Large icon (avatar) set for ${callData.callerName}")
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to convert icon to bitmap for foreground service: ${e.message}")
                            }
                        }
                    }
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setCategory(NotificationCompat.CATEGORY_CALL)
                    .setOngoing(true) // CRITICAL for non-dismissible behavior
                    .setAutoCancel(false)
                    .setOnlyAlertOnce(true)
                    .setContentIntent(appLaunchPendingIntent)
                    .addAction(
                        android.R.drawable.ic_menu_close_clear_cancel,
                        hangupText,
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
                            .bigText(tapToReturnText)
                            .setBigContentTitle(callData.callerName)
                            .setSummaryText("$callTypeText • $durationText")
                    )
                    .build()
                
                // Set flags for non-dismissible foreground service behavior
                notification.flags = notification.flags or 
                    android.app.Notification.FLAG_NO_CLEAR or 
                    android.app.Notification.FLAG_FOREGROUND_SERVICE or
                    android.app.Notification.FLAG_ONGOING_EVENT
                
                Log.d(TAG, "Legacy foreground service notification created with avatar support")
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
            setUIConfiguration(arguments)
            Log.d(TAG, "UI configuration set successfully with translations")
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

    /**
     * Show an incoming call with custom configuration
     */
    private fun handleShowIncomingCallWithConfig(call: MethodCall, result: Result) {
        val arguments = call.arguments as? Map<String, Any>
        if (arguments == null) {
            result.error("INVALID_ARGUMENTS", "Configuration and call data required", null)
            return
        }
        
        val callData = arguments["callData"] as? Map<String, Any>
        val config = arguments["config"] as? Map<String, Any>
        
        if (callData == null) {
            result.error("INVALID_CALL_DATA", "Valid call data required", null)
            return
        }
        
        val parsedCallData = parseCallData(callData)
        if (parsedCallData == null) {
            result.error("INVALID_CALL_DATA", "Valid call data required", null)
            return
        }
        
        try {
            // Store configuration for this call if provided
            if (config != null) {
                Log.d(TAG, "Using custom configuration for call with translations")
                // Store the configuration temporarily for this call
                setUIConfiguration(config)
            }
            
            // Show the call with the provided configuration
            showIncomingCallNotification(parsedCallData, result)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error showing call with config: ${e.message}")
            result.error("CALL_CONFIG_ERROR", "Failed to show call with configuration: ${e.message}", null)
        }
    }

    /**
     * Force show hangup notification for testing purposes
     */
    private fun handleForceShowHangupNotification(call: MethodCall, result: Result) {
        val arguments = call.arguments as? Map<String, Any>
        if (arguments == null) {
            result.error("INVALID_ARGUMENTS", "Configuration and call data required", null)
            return
        }
        
        val callData = arguments["callData"] as? Map<String, Any>
        val config = arguments["config"] as? Map<String, Any>
        
        if (callData == null) {
            result.error("INVALID_CALL_DATA", "Valid call data required", null)
            return
        }
        
        val parsedCallData = parseCallData(callData)
        if (parsedCallData == null) {
            result.error("INVALID_CALL_DATA", "Valid call data required", null)
            return
        }
        
        try {
            Log.d(TAG, "Force showing hangup notification for testing: ${parsedCallData.callerName}")
            
            if (config != null) {
                Log.d(TAG, "Using custom configuration for hangup notification: $config")
            }
            
            // Start the foreground service with hangup notification
            // This creates a non-dismissible notification with duration timer
            CallForegroundService.startService(context, parsedCallData)
            
            Log.d(TAG, "Hangup notification forced to show successfully")
            result.success(true)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error forcing hangup notification: ${e.message}")
            result.error("FORCE_HANGUP_ERROR", "Failed to force show hangup notification: ${e.message}", null)
        }
    }
}
