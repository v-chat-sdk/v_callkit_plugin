package com.v_chat_sdk.v_callkit_plugin

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log

/**
 * Foreground service to ensure ongoing call notifications are persistent
 * on high Android API levels (Android 14+)
 */
class CallForegroundService : Service() {
    
    companion object {
        private const val TAG = "CallForegroundService"
        private const val SERVICE_ID = 1003
        
        fun startService(context: Context, callData: CallData) {
            startServiceWithType(context, callData, "ongoing")
        }
        
        /**
         * Start foreground service for outgoing calls
         * This creates a persistent notification for outgoing calls once they are accepted
         */
        fun startOutgoingCallService(context: Context, callData: CallData) {
            startServiceWithType(context, callData, "outgoing")
        }
        
        /**
         * Start foreground service for incoming calls (existing functionality)
         */
        fun startIncomingCallService(context: Context, callData: CallData) {
            startServiceWithType(context, callData, "incoming")
        }
        /**
         * Show legacy ongoing notification for Android 9 and below
         * This creates a non-dismissible notification without foreground service
         */
        private fun showLegacyOngoingNotification(context: Context, callData: CallData, callType: String) {
            try {
                Log.d(TAG, "Showing legacy ongoing notification for Android ${Build.VERSION.SDK_INT}")
                
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
                    as android.app.NotificationManager
                
                // Create notification channel if needed (Android 8.0+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = android.app.NotificationChannel(
                        VCallkitPlugin.ONGOING_CALL_NOTIFICATION_CHANNEL_ID,
                        "Ongoing Calls",
                        android.app.NotificationManager.IMPORTANCE_DEFAULT
                    ).apply {
                        description = "Non-dismissible notifications for active calls"
                        setShowBadge(true)
                        lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                        enableVibration(false)
                        enableLights(false)
                        setSound(null, null) // No sound for ongoing calls
                        setBypassDnd(false)
                    }
                    notificationManager.createNotificationChannel(channel)
                }
                
                // Create hangup action
                val hangupIntent = android.content.Intent(context, CallActionReceiver::class.java).apply {
                    putExtra("action", "HANGUP")
                    putExtra("callId", callData.id)
                }
                val hangupPendingIntent = android.app.PendingIntent.getBroadcast(
                    context,
                    3,
                    hangupIntent,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                    } else {
                        android.app.PendingIntent.FLAG_UPDATE_CURRENT
                    }
                )
                
                // Create app launch intent
                val packageManager = context.packageManager
                val appLaunchIntent = packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra("call_id", callData.id)
                    putExtra("from_ongoing_notification", true)
                }
                
                val appLaunchPendingIntent = appLaunchIntent?.let {
                    android.app.PendingIntent.getActivity(
                        context,
                        4,
                        it,
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                        } else {
                            android.app.PendingIntent.FLAG_UPDATE_CURRENT
                        }
                    )
                }
                
                // Create call type text
                val callTypeText = when (callType) {
                    "outgoing" -> "Outgoing ${if (callData.isVideoCall) "video" else "voice"} call"
                    "incoming" -> "Incoming ${if (callData.isVideoCall) "video" else "voice"} call"
                    else -> "Ongoing ${if (callData.isVideoCall) "video" else "voice"} call"
                }
                
                // Create non-dismissible ongoing notification for legacy Android versions
                val notificationBuilder = androidx.core.app.NotificationCompat.Builder(context, VCallkitPlugin.ONGOING_CALL_NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(callData.callerName)
                    .setContentText("Tap to return to call")
                    .setSubText(callTypeText)
                    .setSmallIcon(android.R.drawable.ic_menu_call)
                    .setOngoing(true) // CRITICAL: Makes it non-dismissible
                    .setAutoCancel(false) // CRITICAL: Prevents auto-dismissal
                    .setCategory(androidx.core.app.NotificationCompat.CATEGORY_CALL)
                    .setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC)
                    .setContentIntent(appLaunchPendingIntent)
                    .addAction(
                        android.R.drawable.ic_menu_close_clear_cancel,
                        "Hang Up",
                        hangupPendingIntent
                    )
                    .setDeleteIntent(null) // CRITICAL: Disable swipe-to-dismiss
                    .setLocalOnly(true)
                    .setOnlyAlertOnce(true)
                    .setDefaults(0) // No sound, vibration for ongoing calls
                    .setStyle(
                        androidx.core.app.NotificationCompat.BigTextStyle()
                            .bigText("Tap to return to call")
                            .setBigContentTitle(callData.callerName)
                            .setSummaryText(callTypeText)
                    )
                
                // Set priority based on API level
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    // For Android 7.0 and below, use high priority to ensure visibility
                    notificationBuilder.setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                } else {
                    notificationBuilder.setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                }
                
                val notification = notificationBuilder.build()
                
                // CRITICAL: Set flags for non-dismissible behavior on all versions
                notification.flags = android.app.Notification.FLAG_NO_CLEAR or 
                    android.app.Notification.FLAG_ONGOING_EVENT
                
                // Show the notification
                notificationManager.notify(VCallkitPlugin.ONGOING_CALL_NOTIFICATION_ID, notification)
                
                Log.d(TAG, "Legacy ongoing notification shown successfully for Android ${Build.VERSION.SDK_INT}")
            } catch (e: Exception) {
                Log.e(TAG, "Error showing legacy ongoing notification: ${e.message}")
                e.printStackTrace()
            }
        }
        
        
        /**
         * Start foreground service with specific call type
         * For Android 9 and below, use regular non-dismissible notifications
         */
        private fun startServiceWithType(context: Context, callData: CallData, callType: String) {
            // For Android 9 and below (API 28 and below), use regular notifications
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                Log.d(TAG, "Android ${Build.VERSION.SDK_INT} detected - using regular notification for $callType call")
                showLegacyOngoingNotification(context, callData, callType)
                return
            }
            
            // For Android 10+ (API 29+), use foreground service
            val intent = Intent(context, CallForegroundService::class.java).apply {
                putExtra("call_data", callData.toBundle())
                putExtra("action", "start")
                putExtra("call_type", callType)
            }
            
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                
                Log.d(TAG, "Call foreground service started for $callType call: ${callData.callerName}")
            } catch (e: SecurityException) {
                Log.e(TAG, "Permission denied starting foreground service: ${e.message}")
                showLegacyOngoingNotification(context, callData, callType)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start foreground service: ${e.message}")
                showLegacyOngoingNotification(context, callData, callType)
            }
        }

        
        private fun showRegularOngoingNotification(context: Context, callData: CallData) {
            try {
                Log.d(TAG, "Showing regular ongoing notification as fallback")
                
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
                    as android.app.NotificationManager
                
                // Create notification channel if needed
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = android.app.NotificationChannel(
                        VCallkitPlugin.ONGOING_CALL_NOTIFICATION_CHANNEL_ID,
                        "Ongoing Calls",
                        android.app.NotificationManager.IMPORTANCE_DEFAULT
                    )
                    notificationManager.createNotificationChannel(channel)
                }
                
                // Create non-dismissible ongoing notification without foreground service
                val notification = androidx.core.app.NotificationCompat.Builder(context, VCallkitPlugin.ONGOING_CALL_NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(callData.callerName)
                    .setContentText("Tap to return to call")
                    .setSubText("Ongoing call")
                    .setSmallIcon(android.R.drawable.ic_menu_call)
                    .setOngoing(true)
                    .setAutoCancel(false)
                    .setCategory(androidx.core.app.NotificationCompat.CATEGORY_CALL)
                    .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                    .setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC)
                    .setDefaults(0) // No sound, vibration for ongoing calls
                    .setStyle(
                        androidx.core.app.NotificationCompat.BigTextStyle()
                            .bigText("Tap to return to call")
                            .setBigContentTitle(callData.callerName)
                            .setSummaryText("Ongoing call")
                    )
                    .build()
                
                // Set flags to make notification non-dismissible even without foreground service
                notification.flags = notification.flags or 
                    android.app.Notification.FLAG_NO_CLEAR or 
                    android.app.Notification.FLAG_ONGOING_EVENT
                
                notificationManager.notify(VCallkitPlugin.ONGOING_CALL_NOTIFICATION_ID, notification)
                Log.d(TAG, "Regular ongoing notification shown successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to show regular ongoing notification: ${e.message}")
                e.printStackTrace()
            }
        }
        
        fun stopService(context: Context) {
            // For Android 9 and below, just cancel the notification
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                Log.d(TAG, "Android ${Build.VERSION.SDK_INT} detected - canceling legacy notification")
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
                    as android.app.NotificationManager
                notificationManager.cancel(VCallkitPlugin.ONGOING_CALL_NOTIFICATION_ID)
                Log.d(TAG, "Legacy ongoing notification canceled")
                return
            }
            
            // For Android 10+, stop the foreground service
            val intent = Intent(context, CallForegroundService::class.java)
            context.stopService(intent)
            
            // Also cancel the notification as fallback
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
                as android.app.NotificationManager
            notificationManager.cancel(VCallkitPlugin.ONGOING_CALL_NOTIFICATION_ID)
            
            Log.d(TAG, "Call foreground service stopped")
        }
        
        fun updateService(context: Context, callData: CallData, callType: String = "ongoing") {
            // For Android 9 and below, just update the notification
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                Log.d(TAG, "Android ${Build.VERSION.SDK_INT} detected - updating legacy notification")
                showLegacyOngoingNotification(context, callData, callType)
                return
            }
            
            // For Android 10+, update the foreground service
            val intent = Intent(context, CallForegroundService::class.java).apply {
                putExtra("call_data", callData.toBundle())
                putExtra("action", "update")
                putExtra("call_type", callType)
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
    
    private var currentCallData: CallData? = null
    private var currentCallType: String = "ongoing"
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "CallForegroundService created")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.getStringExtra("action") ?: "start"
        val callDataBundle = intent?.getBundleExtra("call_data")
        val callType = intent?.getStringExtra("call_type") ?: "ongoing"
        
        if (callDataBundle != null) {
            currentCallData = CallData.fromBundle(callDataBundle)
            currentCallType = callType
            
            when (action) {
                "start" -> {
                    startForegroundWithNotification()
                }
                "update" -> {
                    updateForegroundNotification()
                }
                "stop" -> {
                    stopForeground(true)
                    stopSelf()
                }
            }
        } else {
            Log.e(TAG, "No call data provided to foreground service")
            stopSelf()
        }
        
        // Return START_NOT_STICKY so the service doesn't restart if killed
        return START_NOT_STICKY
    }
    
    private fun startForegroundWithNotification() {
        val callData = currentCallData ?: return
        
        try {
            Log.d(TAG, "Creating foreground notification for $currentCallType call: ${callData.callerName}")
            
            // Get the notification from the plugin with call type context
            val notification = VCallkitPlugin.pluginInstance?.createOngoingNotificationForService(callData, 0, currentCallType)
            
            if (notification != null) {
                startForeground(SERVICE_ID, notification)
                Log.d(TAG, "Foreground service started successfully with $currentCallType call notification")
            } else {
                Log.e(TAG, "Failed to create notification for foreground service - plugin instance null or notification creation failed")
                
                // Try to create a basic notification as fallback
                createFallbackNotification(callData)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting foreground service: ${e.message}")
            e.printStackTrace()
            
            // Try fallback notification
            createFallbackNotification(callData)
        }
    }
    
    private fun createFallbackNotification(callData: CallData) {
        try {
            Log.d(TAG, "Creating fallback notification due to permission error")
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            
            // Create notification channel if needed
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val channel = android.app.NotificationChannel(
                    VCallkitPlugin.ONGOING_CALL_NOTIFICATION_CHANNEL_ID,
                    "Ongoing Calls",
                    android.app.NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationManager.createNotificationChannel(channel)
            }
            
            // Create non-dismissible ongoing call notification  
            val notification = androidx.core.app.NotificationCompat.Builder(this, VCallkitPlugin.ONGOING_CALL_NOTIFICATION_CHANNEL_ID)
                .setContentTitle(callData.callerName)
                .setContentText("Tap to return to call")
                .setSubText("Ongoing call")
                .setSmallIcon(android.R.drawable.ic_menu_call)
                .setOngoing(true)
                .setAutoCancel(false)
                .setCategory(androidx.core.app.NotificationCompat.CATEGORY_CALL)
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(0) // No sound, vibration for ongoing calls
                .setStyle(
                    androidx.core.app.NotificationCompat.BigTextStyle()
                        .bigText("Tap to return to call")
                        .setBigContentTitle(callData.callerName)
                        .setSummaryText("Ongoing call")
                )
                .build()
            
            // Set flags to make notification non-dismissible
            notification.flags = notification.flags or 
                android.app.Notification.FLAG_NO_CLEAR or 
                android.app.Notification.FLAG_FOREGROUND_SERVICE or
                android.app.Notification.FLAG_ONGOING_EVENT
            
            try {
                // Try to start as foreground service, but without phoneCall type
                startForeground(SERVICE_ID, notification)
                Log.d(TAG, "Fallback foreground notification started successfully")
            } catch (securityException: SecurityException) {
                Log.w(TAG, "Cannot start foreground service, showing regular notification instead")
                
                // If foreground service fails completely, just show a regular ongoing notification
                notificationManager.notify(VCallkitPlugin.ONGOING_CALL_NOTIFICATION_ID, notification)
                stopSelf()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create fallback notification: ${e.message}")
            e.printStackTrace()
            stopSelf()
        }
    }
    
    private fun updateForegroundNotification() {
        val callData = currentCallData ?: return
        
        try {
            // This will be called when the notification needs to be updated
            // The actual updating is handled by the VCallkitPlugin timer
            Log.d(TAG, "Foreground notification update requested")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating foreground notification: ${e.message}")
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        currentCallData = null
        Log.d(TAG, "CallForegroundService destroyed")
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        // This is not a bound service
        return null
    }
} 
