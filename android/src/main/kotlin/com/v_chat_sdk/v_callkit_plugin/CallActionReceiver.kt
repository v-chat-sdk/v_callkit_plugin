package com.v_chat_sdk.v_callkit_plugin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class CallActionReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "CallActionReceiver"
        
        // Track processed actions to prevent duplicates
        private val processedActions = mutableSetOf<String>()
        private var lastActionTime = 0L
        
        private fun isActionAlreadyProcessed(action: String, callId: String): Boolean {
            val actionKey = "${action}_${callId}"
            val currentTime = System.currentTimeMillis()
            
            // Clear old actions (older than 10 seconds)
            if (currentTime - lastActionTime > 10000) {
                processedActions.clear()
            }
            
            // Check if this action was already processed recently (within 2 seconds)
            if (processedActions.contains(actionKey)) {
                val timeDiff = currentTime - lastActionTime
                if (timeDiff < 2000) {
                    return true
                }
            }
            
            // Mark as processed
            processedActions.add(actionKey)
            lastActionTime = currentTime
            return false
        }
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.getStringExtra("action")
        val callId = intent.getStringExtra("callId")
        
        Log.d(TAG, "Received call action: $action for call: $callId")
        
        // Prevent duplicate processing
        if (action != null && callId != null && isActionAlreadyProcessed(action, callId)) {
            Log.d(TAG, "Action $action for call $callId already processed, ignoring duplicate")
            return
        }
        
        when (action) {
            "ANSWER" -> {
                handleAnswerCall(context, callId)
            }
            "DECLINE" -> {
                handleDeclineCall(context, callId)
            }
            "HANGUP" -> {
                handleHangupCall(context, callId)
            }
            else -> {
                Log.w(TAG, "Unknown call action: $action")
            }
        }
    }
    
    private fun handleAnswerCall(context: Context, callId: String?) {
        if (callId != null) {
            Log.d(TAG, "Answering call: $callId")
            
            // Get call data before processing
            val callData = CallConnectionManager.getCallData(callId)
            
            // Stop sounds and dismiss incoming call notification
            dismissIncomingCallNotification(context)
            
            // Launch app and set call action data
            if (callData != null) {
                launchAppWithCallAction(context, "ANSWER", callData)
            }
            
            // Notify Flutter about call answer
            VCallkitPlugin.methodChannel?.invokeMethod("onCallAnswered", mapOf(
                "callId" to callId,
                "timestamp" to System.currentTimeMillis()
            ))
            
            // Update call connection manager
            CallConnectionManager.answerCall(callId)
            
            // NOTE: No automatic ongoing notification - controlled from Flutter side
            
            Log.d(TAG, "Call answered successfully - notification control delegated to Flutter: $callId")
        } else {
            Log.e(TAG, "Cannot answer call: callId is null")
        }
    }
    
    private fun handleDeclineCall(context: Context, callId: String?) {
        if (callId != null) {
            Log.d(TAG, "Declining call: $callId")
            
            // Stop sounds and dismiss notification completely
            dismissIncomingCallNotification(context)
            
            // Update call connection manager
            CallConnectionManager.rejectCall(callId)
            
            Log.d(TAG, "Call declined silently: $callId")
        } else {
            Log.e(TAG, "Cannot decline call: callId is null")
        }
    }
    
    private fun handleHangupCall(context: Context, callId: String?) {
        if (callId != null) {
            Log.d(TAG, "Hanging up call: $callId")
            
            // Stop sounds and dismiss notification completely
            dismissAllCallNotifications(context)
            
            // Notify Flutter about call hangup
            VCallkitPlugin.methodChannel?.invokeMethod("onCallEnded", mapOf(
                "callId" to callId,
                "reason" to "hangup",
                "timestamp" to System.currentTimeMillis()
            ))
            
            // Update call connection manager
            CallConnectionManager.endCall(callId)
            
            Log.d(TAG, "Call hung up successfully: $callId")
        } else {
            Log.e(TAG, "Cannot hang up call: callId is null")
        }
    }
    
    private fun dismissIncomingCallNotification(context: Context) {
        try {
            // Dismiss the incoming call notification (system automatically stops ringtone)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
                as android.app.NotificationManager
            notificationManager.cancel(VCallkitPlugin.CALL_NOTIFICATION_ID)
            
            Log.d(TAG, "Incoming call notification dismissed - system automatically stopped ringtone")
        } catch (e: Exception) {
            Log.e(TAG, "Error dismissing incoming call notification: ${e.message}")
        }
    }
    
    private fun dismissAllCallNotifications(context: Context) {
        try {
            // Dismiss all call-related notifications (system automatically stops ringtone)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
                as android.app.NotificationManager
            notificationManager.cancel(VCallkitPlugin.CALL_NOTIFICATION_ID)
            notificationManager.cancel(VCallkitPlugin.ONGOING_CALL_NOTIFICATION_ID)
            
            // Stop foreground services if running
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                CallForegroundService.stopService(context)
            }
            
            // Stop call duration timer through plugin instance
            VCallkitPlugin.pluginInstance?.stopCallTimer()
            
            Log.d(TAG, "All call notifications dismissed - system automatically stopped ringtone")
        } catch (e: Exception) {
            Log.e(TAG, "Error dismissing call notifications: ${e.message}")
        }
    }
    
    // Note: stopCallSounds method removed - system now manages ringtone lifecycle automatically
    // Notification dismissal automatically stops ringtone
    
    private fun showOngoingCallNotification(context: Context, callId: String) {
        try {
            Log.d(TAG, "Attempting to show ongoing notification for call: $callId")
            
            // Get call data to show ongoing notification
            val callData = CallConnectionManager.getCallData(callId)
            if (callData != null) {
                Log.d(TAG, "Call data found, showing ongoing notification for: ${callData.callerName}")
                
                // Use force show method to ensure it appears
                VCallkitPlugin.pluginInstance?.forceShowOngoingNotification(callData)
                
                Log.d(TAG, "Ongoing call notification requested for: $callId")
            } else {
                Log.e(TAG, "Cannot show ongoing notification: call data not found for $callId")
                
                // Try to get from incoming call data as fallback
                val incomingData = CallConnectionManager.getIncomingCallData()
                if (incomingData != null && incomingData.id == callId) {
                    Log.d(TAG, "Found call data in incoming, using that for ongoing notification")
                    VCallkitPlugin.pluginInstance?.forceShowOngoingNotification(incomingData)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing ongoing call notification: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun launchAppWithCallAction(context: Context, action: String, callData: CallData) {
        try {
            // Store the call action data for the app to retrieve
            VCallkitPlugin.setCallActionLaunchData(action, callData)
            
            // Create intent to launch the main activity
            val packageManager = context.packageManager
            val launchIntent = packageManager.getLaunchIntentForPackage(context.packageName)
            
            if (launchIntent != null) {
                launchIntent.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra("call_action", action)
                    putExtra("call_id", callData.id)
                    putExtra("launched_from_notification", true)
                }
                
                context.startActivity(launchIntent)
                Log.d(TAG, "App launched with call action: $action for ${callData.callerName}")
            } else {
                Log.e(TAG, "Cannot launch app: no launch intent found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error launching app with call action: ${e.message}")
        }
    }
} 