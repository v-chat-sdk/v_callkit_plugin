package com.v_chat_sdk.v_callkit_plugin

import android.content.Context
import android.os.Build
import android.util.Log

/**
 * Simplified call management for handling call state
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
    
    private var isCallActive: Boolean = false
    private var currentCallData: CallData? = null
    
    /**
     * Handle incoming call
     */
    fun handleIncomingCall(callData: CallData) {
        Log.d(TAG, "Handling incoming call for: ${callData.callerName}")
        
        // Store current call data
        currentCallData = callData
        isCallActive = false
        
        // Store call data in connection manager
        CallConnectionManager.setIncomingCallData(callData)
    }
    
    /**
     * Handle call answer - Notification control is handled by Flutter
     */
    fun handleCallAnswered(callData: CallData) {
        Log.d(TAG, "Call answered for: ${callData.callerName}")
        
        // Mark call as active
        isCallActive = true
        currentCallData = callData
        
        // Update connection manager
        CallConnectionManager.answerCall(callData.id)
        
        // NOTE: Notification control is handled by Flutter side via startOutgoingCallNotification
        Log.d(TAG, "Call answered - notification control delegated to Flutter: ${callData.callerName}")
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
        
        // Stop any active foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CallForegroundService.stopService(context)
        }
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
        
        // Stop any active foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CallForegroundService.stopService(context)
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
} 