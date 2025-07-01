package com.v_chat_sdk.v_callkit_plugin

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.concurrent.ConcurrentHashMap

/**
 * Foreground Service Manager for handling persistent notifications
 * Based on Google Maps Navigation SDK pattern for consolidating notifications
 * 
 * This manager ensures that:
 * 1. Only one persistent notification is shown across all foreground services
 * 2. The notification cannot be dismissed by the user
 * 3. Services can share the same notification ID for consolidation
 * 4. Proper lifecycle management for Android O and newer
 */
class ForegroundServiceManager private constructor(
    private val application: Application,
    private val notificationId: Int,
    private val notificationProvider: NotificationContentProvider?
) {
    
    companion object {
        private const val TAG = "ForegroundServiceManager"
        private const val DEFAULT_NOTIFICATION_ID = 2001
        private const val HANGUP_CHANNEL_ID = "hangup_notification_channel"
        
        @Volatile
        private var INSTANCE: ForegroundServiceManager? = null
        
        /**
         * Initialize the foreground service manager with a notification provider
         * This follows the Google Maps SDK pattern for custom notification handling
         */
        @JvmStatic
        fun initForegroundServiceManagerProvider(
            application: Application,
            notificationId: Int? = null,
            notificationProvider: NotificationContentProvider? = null
        ): ForegroundServiceManager {
            if (INSTANCE != null) {
                throw RuntimeException("ForegroundServiceManager already initialized. Call clearForegroundServiceManager() first.")
            }
            
            val actualNotificationId = notificationId ?: DEFAULT_NOTIFICATION_ID
            INSTANCE = ForegroundServiceManager(application, actualNotificationId, notificationProvider)
            
            Log.d(TAG, "ForegroundServiceManager initialized with notification ID: $actualNotificationId")
            return INSTANCE!!
        }
        
        /**
         * Initialize the foreground service manager with default message and intent
         */
        @JvmStatic
        fun initForegroundServiceManagerMessageAndIntent(
            application: Application,
            notificationId: Int? = null,
            defaultMessage: String? = null,
            resumeIntent: PendingIntent? = null
        ): ForegroundServiceManager {
            val provider = DefaultNotificationContentProvider(application, defaultMessage, resumeIntent)
            return initForegroundServiceManagerProvider(application, notificationId, provider)
        }
        
        /**
         * Get the foreground service manager instance
         */
        @JvmStatic
        fun getForegroundServiceManager(application: Application): ForegroundServiceManager {
            return INSTANCE ?: initForegroundServiceManagerMessageAndIntent(application)
        }
        
        /**
         * Clear the foreground service manager singleton
         */
        @JvmStatic
        fun clearForegroundServiceManager() {
            INSTANCE?.shutdown()
            INSTANCE = null
            Log.d(TAG, "ForegroundServiceManager cleared")
        }
    }
    
    private val notificationManager: NotificationManager = 
        application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    private val activeServices = ConcurrentHashMap<String, Service>()
    private var currentNotification: Notification? = null
    
    init {
        createNotificationChannel()
    }
    
    /**
     * Create notification channel for Android O and newer
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                HANGUP_CHANNEL_ID,
                "Call Hangup Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Persistent notifications for active calls with hangup button. Required for proper call functionality."
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                enableVibration(false)
                enableLights(false)
                setSound(null, null) // No sound for hangup notifications
                setBypassDnd(false)
                // Critical: Make the channel non-dismissible
                setBlockable(false)
            }
            
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created for API ${Build.VERSION.SDK_INT}")
        }
    }
    
    /**
     * Start a service in foreground using the shared persistent notification
     * This is the key method that ensures all services use the same notification
     */
    fun startForeground(service: Service, serviceId: String) {
        try {
            activeServices[serviceId] = service
            
            val notification = getCurrentNotification()
            
            // Start foreground with appropriate API level handling
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ with foreground service type
                service.startForeground(
                    notificationId, 
                    notification, 
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
                )
                Log.d(TAG, "Service $serviceId started foreground with PHONE_CALL type (API ${Build.VERSION.SDK_INT})")
            } else {
                // Android 5.0 to 9.0
                service.startForeground(notificationId, notification)
                Log.d(TAG, "Service $serviceId started foreground (API ${Build.VERSION.SDK_INT})")
            }
            
            Log.d(TAG, "Service $serviceId successfully started in foreground with persistent notification")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error starting service $serviceId in foreground: ${e.message}")
            throw e
        }
    }
    
    /**
     * Stop a service from foreground but keep notification if other services are active
     */
    fun stopForeground(service: Service, serviceId: String, removeNotification: Boolean = false) {
        try {
            activeServices.remove(serviceId)
            
            if (removeNotification || activeServices.isEmpty()) {
                // Remove notification if requested or no more active services
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    service.stopForeground(Service.STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    service.stopForeground(true)
                }
                currentNotification = null
                Log.d(TAG, "Service $serviceId stopped foreground and notification removed")
            } else {
                // Keep notification for other active services
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    service.stopForeground(Service.STOP_FOREGROUND_DETACH)
                } else {
                    @Suppress("DEPRECATION")
                    service.stopForeground(false)
                }
                Log.d(TAG, "Service $serviceId stopped foreground but notification kept for other services")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping service $serviceId from foreground: ${e.message}")
        }
    }
    
    /**
     * Update the persistent notification
     * This triggers a re-render of the notification with current content
     */
    fun updateNotification() {
        try {
            currentNotification = null // Force regeneration
            val notification = getCurrentNotification()
            
            // Update notification for all active services
            notificationManager.notify(notificationId, notification)
            
            Log.d(TAG, "Persistent notification updated for ${activeServices.size} active services")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating notification: ${e.message}")
        }
    }
    
    /**
     * Get the current notification, creating it if necessary
     */
    private fun getCurrentNotification(): Notification {
        if (currentNotification == null) {
            currentNotification = notificationProvider?.getNotification() 
                ?: createDefaultNotification()
        }
        return currentNotification!!
    }
    
    /**
     * Create a default notification when no provider is set
     */
    private fun createDefaultNotification(): Notification {
        val hangupIntent = Intent(application, CallActionReceiver::class.java).apply {
            putExtra("action", "HANGUP")
        }
        val hangupPendingIntent = PendingIntent.getBroadcast(
            application,
            0,
            hangupIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )
        
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder(application, HANGUP_CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            NotificationCompat.Builder(application)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        }
        
        val notification = builder
            .setContentTitle("📞 Active Call")
            .setContentText("Call in progress")
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setOngoing(true) // CRITICAL: Non-dismissible
            .setAutoCancel(false) // CRITICAL: Prevents auto-dismissal
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDeleteIntent(null) // CRITICAL: Disable swipe-to-dismiss
            .setLocalOnly(true)
            .setOnlyAlertOnce(true)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Hangup",
                hangupPendingIntent
            )
            .build()
        
        // Apply critical persistence flags
        notification.flags = notification.flags or 
            Notification.FLAG_NO_CLEAR or 
            Notification.FLAG_ONGOING_EVENT
        
        notification.flags = notification.flags and Notification.FLAG_AUTO_CANCEL.inv()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            notification.flags = notification.flags or Notification.FLAG_FOREGROUND_SERVICE
        }
        
        Log.d(TAG, "Default notification created with persistence flags: ${notification.flags}")
        return notification
    }
    
    /**
     * Shutdown the manager and clean up resources
     */
    private fun shutdown() {
        activeServices.clear()
        currentNotification = null
        Log.d(TAG, "ForegroundServiceManager shutdown completed")
    }
    
    /**
     * Get debug information about the manager state
     */
    fun getDebugInfo(): Map<String, Any> {
        return mapOf(
            "notificationId" to notificationId,
            "activeServicesCount" to activeServices.size,
            "activeServiceIds" to activeServices.keys.toList(),
            "hasNotificationProvider" to (notificationProvider != null),
            "hasCurrentNotification" to (currentNotification != null),
            "apiLevel" to Build.VERSION.SDK_INT
        )
    }
}

/**
 * Interface for providing custom notification content
 * Based on Google Maps SDK NotificationContentProvider pattern
 */
interface NotificationContentProvider {
    fun getNotification(): Notification
}

/**
 * Base class for notification content providers
 * Provides updateNotification() method for triggering re-renders
 */
abstract class NotificationContentProviderBase(
    protected val application: Application
) : NotificationContentProvider {
    
    protected fun updateNotification() {
        try {
            ForegroundServiceManager.getForegroundServiceManager(application).updateNotification()
        } catch (e: Exception) {
            Log.e("NotificationProvider", "Error updating notification: ${e.message}")
        }
    }
}

/**
 * Default notification content provider for hangup notifications
 */
class DefaultNotificationContentProvider(
    application: Application,
    private val defaultMessage: String? = null,
    private val resumeIntent: PendingIntent? = null
) : NotificationContentProviderBase(application) {
    
    private var currentCallData: CallData? = null
    private var callStartTime: Long = 0
    
    fun setCallData(callData: CallData, startTime: Long) {
        this.currentCallData = callData
        this.callStartTime = startTime
        updateNotification()
    }
    
    override fun getNotification(): Notification {
        val callData = currentCallData
        
        val hangupIntent = Intent(application, CallActionReceiver::class.java).apply {
            putExtra("action", "HANGUP")
            callData?.let { putExtra("callId", it.id) }
        }
        val hangupPendingIntent = PendingIntent.getBroadcast(
            application,
            0,
            hangupIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )
        
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder(application, "hangup_notification_channel")
        } else {
            @Suppress("DEPRECATION")
            NotificationCompat.Builder(application)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        }
        
        val title = if (callData != null) {
            "📞 ${callData.callerName}"
        } else {
            defaultMessage ?: "📞 Active Call"
        }
        
        val duration = if (callStartTime > 0) {
            val totalSeconds = (System.currentTimeMillis() - callStartTime) / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            
            if (hours > 0) {
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        } else {
            "00:00"
        }
        
        val contentText = "⏱️ $duration"
        
        val expandedContent = buildString {
            if (callData != null) {
                append("📞 ${callData.callerName}\n")
            }
            append("⏱️ Duration: $duration\n")
            append("💬 Tap to return to call")
        }
        
        val notification = builder
            .setContentTitle(title)
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(expandedContent)
                    .setBigContentTitle(title)
                    .setSummaryText(duration)
            )
            .setOngoing(true) // CRITICAL: Non-dismissible
            .setAutoCancel(false) // CRITICAL: Prevents auto-dismissal
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(resumeIntent)
            .setDeleteIntent(null) // CRITICAL: Disable swipe-to-dismiss
            .setLocalOnly(true)
            .setOnlyAlertOnce(true)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Hangup",
                hangupPendingIntent
            )
            .build()
        
        // Apply critical persistence flags based on Google's recommendations
        notification.flags = notification.flags or 
            Notification.FLAG_NO_CLEAR or 
            Notification.FLAG_ONGOING_EVENT
        
        notification.flags = notification.flags and Notification.FLAG_AUTO_CANCEL.inv()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            notification.flags = notification.flags or Notification.FLAG_FOREGROUND_SERVICE
        }
        
        return notification
    }
} 