package com.v_chat_sdk.v_callkit_plugin

import android.app.Application
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat

/**
 * Custom notification content provider for hangup notifications
 * Based on Google Maps Navigation SDK NotificationContentProvider pattern
 * 
 * This provider creates persistent, non-dismissible notifications with:
 * - Real-time duration updates
 * - Custom avatar support with fallback to initials
 * - Expanded layout that shows all information without user interaction
 * - Proper persistence flags for Android O and newer
 */
class HangupNotificationContentProvider(
    application: Application,
    private var callData: CallData,
    private var callStartTime: Long,
    private var config: android.os.Bundle?,
    private val resumeIntent: PendingIntent?
) : NotificationContentProviderBase(application) {
    
    companion object {
        private const val TAG = "HangupNotificationProvider"
        private const val HANGUP_CHANNEL_ID = "hangup_notification_channel"
    }
    
    private var avatarBitmap: Bitmap? = null
    
    /**
     * Update call data and trigger notification refresh
     */
    fun updateCallData(newCallData: CallData, newStartTime: Long, newConfig: android.os.Bundle?) {
        this.callData = newCallData
        this.callStartTime = newStartTime
        this.config = newConfig
        updateNotification()
    }
    
    /**
     * Update duration and trigger notification refresh
     */
    fun updateDuration(startTime: Long) {
        this.callStartTime = startTime
        updateNotification()
    }
    
    /**
     * Update avatar and trigger notification refresh
     */
    fun updateAvatar(bitmap: Bitmap) {
        this.avatarBitmap = bitmap
        updateNotification()
    }
    
    /**
     * Create the persistent hangup notification
     * This follows Android's requirements for truly persistent notifications
     */
    override fun getNotification(): Notification {
        Log.d(TAG, "Creating persistent hangup notification for API ${Build.VERSION.SDK_INT}")
        
        // Parse configuration with defaults
        val customTitle = config?.getString("title")
        val customContentText = config?.getString("contentText")
        val customHangupButtonText = config?.getString("hangupButtonText") ?: "Hangup"
        val showDuration = config?.getBoolean("showDuration", true) ?: true
        val enableTapToReturn = config?.getBoolean("enableTapToReturn", true) ?: true
        val customPriority = config?.getInt("priority", 0) ?: 0
        
        // Create hangup action
        val hangupIntent = Intent(application, CallActionReceiver::class.java).apply {
            putExtra("action", "HANGUP")
            putExtra("callId", callData.id)
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
        
        // Calculate current duration
        val duration = getCurrentDuration()
        
        // Build notification with proper API level handling
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android O+ - Use notification channels (required for foreground services)
            NotificationCompat.Builder(application, HANGUP_CHANNEL_ID)
        } else {
            // Pre-Android O - Use legacy builder with custom priority
            @Suppress("DEPRECATION")
            NotificationCompat.Builder(application)
                .setPriority(when (customPriority) {
                    1 -> NotificationCompat.PRIORITY_HIGH
                    -1 -> NotificationCompat.PRIORITY_LOW
                    else -> NotificationCompat.PRIORITY_DEFAULT
                })
        }
        
        // Create notification content
        val title = customTitle ?: "📞 ${callData.callerName}"
        
        val contentText = if (customContentText != null) {
            customContentText.replace("{callerName}", callData.callerName)
                .replace("{duration}", if (showDuration) duration else "")
        } else {
            if (showDuration) {
                "⏱️ $duration"
            } else {
                "Call in progress"
            }
        }
        
        // Create expanded content for BigTextStyle
        // This ensures all information is visible without user interaction
        val expandedContent = buildString {
            append("📞 ${callData.callerName}\n")
            if (showDuration) {
                append("⏱️ Duration: $duration\n")
            }
            if (enableTapToReturn) {
                append("💬 Tap to return to call")
            }
        }
        
        // Build the notification with all persistence flags
        val notification = builder
            .setContentTitle(title)
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .apply {
                // Set avatar as large icon if available
                avatarBitmap?.let { avatar ->
                    setLargeIcon(avatar)
                    Log.d(TAG, "Avatar set as large icon")
                }
            }
            // Use BigTextStyle to make notification expanded by default
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(expandedContent)
                    .setBigContentTitle(title)
                    .setSummaryText(if (showDuration) duration else "Active call")
            )
            // CRITICAL: Persistence flags based on Google's recommendations
            .setOngoing(true) // Makes notification non-dismissible
            .setAutoCancel(false) // Prevents auto-dismissal
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(if (enableTapToReturn) resumeIntent else null)
            .setDeleteIntent(null) // Disable swipe-to-dismiss
            .setLocalOnly(true) // Keep notification local to device
            .setOnlyAlertOnce(true) // Don't repeatedly alert
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)
            .setColorized(true)
            .setColor(0xFF4285F4.toInt()) // Google Blue
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                customHangupButtonText,
                hangupPendingIntent
            )
            .build()
        
        // Apply critical persistence flags according to Android documentation
        // These flags ensure the notification cannot be dismissed by swiping
        notification.flags = notification.flags or 
            Notification.FLAG_NO_CLEAR or        // Prevents swipe-to-dismiss
            Notification.FLAG_ONGOING_EVENT      // Marks as ongoing event
        
        // Explicitly disable auto-cancel
        notification.flags = notification.flags and Notification.FLAG_AUTO_CANCEL.inv()
        
        // Additional flags for manufacturer customizations
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            notification.flags = notification.flags or Notification.FLAG_FOREGROUND_SERVICE
        }
        
        Log.d(TAG, "Persistent notification created with flags: ${notification.flags}")
        Log.d(TAG, "NO_CLEAR: ${notification.flags and Notification.FLAG_NO_CLEAR != 0}")
        Log.d(TAG, "ONGOING: ${notification.flags and Notification.FLAG_ONGOING_EVENT != 0}")
        Log.d(TAG, "FOREGROUND_SERVICE: ${notification.flags and Notification.FLAG_FOREGROUND_SERVICE != 0}")
        
        return notification
    }
    
    /**
     * Get formatted duration text for display in notification
     */
    private fun getCurrentDuration(): String {
        val totalSeconds = if (callStartTime > 0) {
            (System.currentTimeMillis() - callStartTime) / 1000
        } else {
            0
        }
        
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        
        return if (hours > 0) {
            // For calls longer than 1 hour: HH:MM:SS
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            // For calls under 1 hour: MM:SS
            String.format("%02d:%02d", minutes, seconds)
        }
    }
} 