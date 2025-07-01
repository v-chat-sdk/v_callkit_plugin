package com.v_chat_sdk.v_callkit_plugin

import android.content.Context
import android.os.Build
import android.util.Log

/**
 * Centralized call management that ensures hangup notification shows reliably
 * on all Android versions when calls are answered
 */
class CallManager(private val context: Context) {
    
    companion object {
        private const val TAG = "CallManager"
        
        // Singleton instance for static access
        @JvmStatic
        private var instance: CallManager? = null
        
        @JvmStatic
        fun getInstance(context: Context): CallManager {
            if (instance == null) {
                instance = CallManager(context.applicationContext)
            }
            return instance!!
        }
        
        @JvmStatic
        fun clearInstance() {
            instance = null
        }
    }
    
    private var currentUIConfig: CallUIConfig = CallUIConfig()
    private var isCallActive: Boolean = false
    private var currentCallData: CallData? = null
    
    /**
     * Set the UI configuration for all call-related UI
     */
    fun setUIConfiguration(config: CallUIConfig) {
        currentUIConfig = config
        Log.d(TAG, "UI configuration updated")
    }
    
    /**
     * Get current UI configuration
     */
    fun getUIConfiguration(): CallUIConfig {
        return currentUIConfig
    }
    
    /**
     * Handle incoming call with UI configuration
     */
    fun handleIncomingCall(callData: CallData, config: CallUIConfig? = null) {
        Log.d(TAG, "Handling incoming call for: ${callData.callerName}")
        
        // Update UI config if provided
        config?.let { setUIConfiguration(it) }
        
        // Store current call data
        currentCallData = callData
        isCallActive = false
        
        // Show incoming call with current UI configuration
        // This will be handled by VCallkitPlugin but we prepare the data
        CallConnectionManager.setIncomingCallData(callData)
    }
    
    /**
     * Handle call answer - CRITICAL: This ensures hangup notification shows on ALL Android versions
     */
    fun handleCallAnswered(callData: CallData, config: CallUIConfig? = null) {
        Log.d(TAG, "Call answered for: ${callData.callerName}")
        
        // Update UI config if provided
        config?.let { setUIConfiguration(it) }
        
        // Mark call as active
        isCallActive = true
        currentCallData = callData
        
        // Update connection manager
        CallConnectionManager.answerCall(callData.id)
        
        // CRITICAL: Always show hangup notification when call is answered
        // This ensures it works on ALL Android API versions (21+)
        showHangupNotificationForActiveCall(callData, currentUIConfig)
        
        Log.d(TAG, "Hangup notification triggered for answered call: ${callData.callerName}")
    }
    
    /**
     * Handle call decline
     */
    fun handleCallDeclined(callData: CallData) {
        Log.d(TAG, "Call declined for: ${callData.callerName}")
        
        // Mark call as inactive
        isCallActive = false
        currentCallData = null
        
        // Update connection manager
        CallConnectionManager.rejectCall(callData.id)
        
        // Ensure hangup notification is hidden
        hideHangupNotification()
    }
    
    /**
     * Handle call end/hangup
     */
    fun handleCallEnded(callData: CallData) {
        Log.d(TAG, "Call ended for: ${callData.callerName}")
        
        // Mark call as inactive
        isCallActive = false
        currentCallData = null
        
        // Update connection manager
        CallConnectionManager.endCall(callData.id)
        
        // Hide hangup notification
        hideHangupNotification()
    }
    
    /**
     * Show hangup notification for active call - works on ALL Android versions
     */
    private fun showHangupNotificationForActiveCall(callData: CallData, config: CallUIConfig) {
        try {
            Log.d(TAG, "Showing hangup notification for Android API ${Build.VERSION.SDK_INT}")
            
            // Prepare hangup notification configuration
            val hangupConfig = createHangupNotificationConfig(config)
            
            // For Android 8.0+ (API 26+), use the CallForegroundService (foreground service)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d(TAG, "Using CallForegroundService for API ${Build.VERSION.SDK_INT}")
                CallForegroundService.startService(context, callData)
            } else {
                // For Android 7.0 and below (API 21-25), use fallback persistent notification
                Log.d(TAG, "Using fallback notification method for API ${Build.VERSION.SDK_INT}")
                showFallbackHangupNotification(callData, config)
            }
            
            Log.d(TAG, "Hangup notification shown successfully for API ${Build.VERSION.SDK_INT}")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing hangup notification: ${e.message}")
            e.printStackTrace()
            
            // Fallback to basic notification
            showFallbackHangupNotification(callData, config)
        }
    }
    
    /**
     * Create hangup notification configuration from UI config
     */
    private fun createHangupNotificationConfig(config: CallUIConfig): Map<String, Any> {
        return mapOf(
            "title" to (config.hangupNotificationTitle ?: config.callInProgressText),
            "contentText" to (config.hangupNotificationContent ?: ""),
            "subText" to (config.hangupNotificationSubText ?: config.tapToReturnText),
            "hangupButtonText" to config.hangupButtonText,
            "showDuration" to config.showCallDuration,
            "enableTapToReturn" to config.enableTapToReturnToCall,
            "priority" to config.hangupNotificationPriority
        )
    }
    
    /**
     * Fallback hangup notification for older Android versions (API 21-25)
     */
    private fun showFallbackHangupNotification(callData: CallData, config: CallUIConfig) {
        try {
            Log.d(TAG, "Showing fallback hangup notification for API ${Build.VERSION.SDK_INT}")
            
            // Use the regular notification manager to show a persistent notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
                as android.app.NotificationManager
            
            // Create hangup action intent
            val hangupIntent = android.content.Intent(context, CallActionReceiver::class.java).apply {
                putExtra(CallConstants.EXTRA_ACTION, CallConstants.ACTION_HANGUP)
                putExtra(CallConstants.EXTRA_CALL_ID, callData.id)
            }
            
            val hangupPendingIntent = android.app.PendingIntent.getBroadcast(
                context,
                CallConstants.HANGUP_PENDING_INTENT_REQUEST_CODE,
                hangupIntent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                } else {
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT
                }
            )
            
            // Create content intent to return to app
            val contentIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                addFlags(CallConstants.ACTIVITY_LAUNCH_FLAGS)
                putExtra("call_id", callData.id)
                putExtra("from_hangup_notification", true)
            }
            
            val contentPendingIntent = contentIntent?.let {
                android.app.PendingIntent.getActivity(
                    context,
                    CallConstants.CONTENT_PENDING_INTENT_REQUEST_CODE,
                    it,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                    } else {
                        android.app.PendingIntent.FLAG_UPDATE_CURRENT
                    }
                )
            }
            
            // Build notification using NotificationCompat for compatibility
            val notificationBuilder = androidx.core.app.NotificationCompat.Builder(
                context, 
                CallConstants.HANGUP_NOTIFICATION_CHANNEL_ID
            )
                .setContentTitle(config.hangupNotificationTitle ?: config.callInProgressText)
                .setContentText("${callData.callerName} - ${config.getCallTypeText(callData.isVideoCall)} ${config.ongoingCallText}")
                .setSubText(config.hangupNotificationSubText ?: config.tapToReturnText)
                .setSmallIcon(android.R.drawable.ic_menu_call)
                .setOngoing(true) // CRITICAL: Makes it non-dismissible
                .setAutoCancel(false) // CRITICAL: Prevents auto-dismissal
                .setCategory(androidx.core.app.NotificationCompat.CATEGORY_CALL)
                .setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(contentPendingIntent)
                .addAction(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    config.hangupButtonText,
                    hangupPendingIntent
                )
                .setDeleteIntent(null) // CRITICAL: Disable swipe-to-dismiss
                .setLocalOnly(true)
                .setOnlyAlertOnce(true)
            
            // Set priority based on API level
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                when (config.hangupNotificationPriority) {
                    1 -> notificationBuilder.setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                    -1 -> notificationBuilder.setPriority(androidx.core.app.NotificationCompat.PRIORITY_LOW)
                    else -> notificationBuilder.setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                }
                
                // No sound or vibration for hangup notifications
                notificationBuilder.setSound(null)
                notificationBuilder.setVibrate(null)
            }
            
            val notification = notificationBuilder.build()
            
            // CRITICAL: Set flags for non-dismissible behavior on all versions
            notification.flags = android.app.Notification.FLAG_NO_CLEAR or 
                android.app.Notification.FLAG_ONGOING_EVENT
            
            // Ensure notification channel exists for older versions that support it
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createHangupNotificationChannel(notificationManager, config)
            }
            
            // Show the notification
            notificationManager.notify(CallConstants.HANGUP_NOTIFICATION_ID, notification)
            
            Log.d(TAG, "Fallback hangup notification shown successfully for API ${Build.VERSION.SDK_INT}")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing fallback hangup notification: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Create hangup notification channel for Android 8.0+
     */
    private fun createHangupNotificationChannel(
        notificationManager: android.app.NotificationManager,
        config: CallUIConfig
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = config.notificationChannelName ?: "Call Hangup"
            val channelDescription = config.notificationChannelDescription 
                ?: "Persistent notification with hangup button for active calls"
            
            val channel = android.app.NotificationChannel(
                CallConstants.HANGUP_NOTIFICATION_CHANNEL_ID,
                channelName,
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = channelDescription
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                enableVibration(false)
                enableLights(false)
                setSound(null, null)
                setBypassDnd(false)
            }
            
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Hangup notification channel created")
        }
    }
    
    /**
     * Hide hangup notification
     */
    private fun hideHangupNotification() {
        try {
            // Stop the hangup notification service
            CallForegroundService.stopService(context)
            
            // Also cancel the notification directly as fallback
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
                as android.app.NotificationManager
            notificationManager.cancel(CallConstants.HANGUP_NOTIFICATION_ID)
            
            Log.d(TAG, "Hangup notification hidden")
        } catch (e: Exception) {
            Log.e(TAG, "Error hiding hangup notification: ${e.message}")
        }
    }
    
    /**
     * Update hangup notification with new call data or configuration
     */
    fun updateHangupNotification(callData: CallData, config: CallUIConfig? = null) {
        if (isCallActive) {
            config?.let { setUIConfiguration(it) }
            showHangupNotificationForActiveCall(callData, currentUIConfig)
            Log.d(TAG, "Hangup notification updated")
        }
    }
    
    /**
     * Check if call is currently active
     */
    fun isCallActive(): Boolean {
        return isCallActive
    }
    
    /**
     * Get current call data
     */
    fun getCurrentCallData(): CallData? {
        return currentCallData
    }
    
    /**
     * Force show hangup notification (for external calls)
     */
    fun forceShowHangupNotification(callData: CallData, config: CallUIConfig? = null) {
        config?.let { setUIConfiguration(it) }
        isCallActive = true
        currentCallData = callData
        showHangupNotificationForActiveCall(callData, currentUIConfig)
        Log.d(TAG, "Hangup notification forced to show")
    }
    
    /**
     * Get debug information about call state
     */
    fun getDebugInfo(): Map<String, Any> {
        return mapOf(
            "isCallActive" to isCallActive,
            "currentCallData" to (currentCallData?.toMap() ?: "null"),
            "androidVersion" to Build.VERSION.SDK_INT,
            "hasCallForegroundService" to (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O),
            "uiConfig" to currentUIConfig.toMap()
        )
    }
} 