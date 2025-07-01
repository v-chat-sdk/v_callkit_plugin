package com.v_chat_sdk.v_callkit_plugin

import android.os.Bundle
import android.util.Log
import java.util.concurrent.ConcurrentHashMap



/**
 * Manages active call states for notification-based VoIP calls
 */
object CallConnectionManager {
    
    private const val TAG = "CallConnectionManager"
    
    // Call states (simplified - removed HOLDING state as it's no longer used)
    enum class CallState {
        RINGING,
        ACTIVE,
        ENDED
    }
    
    // Call session data (simplified - removed mute and hold fields)
    data class CallSession(
        val callData: CallData,
        var state: CallState = CallState.RINGING,
        val startTime: Long = System.currentTimeMillis()
    )
    
    private val activeCalls = ConcurrentHashMap<String, CallSession>()
    private var incomingCallData: CallData? = null
    
    /**
     * Sets the incoming call data when a new call is being displayed
     */
    fun setIncomingCallData(callData: CallData) {
        incomingCallData = callData
        // Also create a call session for this incoming call
        activeCalls[callData.id] = CallSession(callData)
        Log.d(TAG, "Set incoming call data: ${callData.id}")
    }
    
    /**
     * Gets the incoming call data for creating a new connection
     */
    fun getIncomingCallData(): CallData? {
        return incomingCallData
    }
    
    /**
     * Clears the incoming call data after connection is created
     */
    fun clearIncomingCallData() {
        incomingCallData = null
    }
    
    /**
     * Checks if there are any active calls
     */
    fun hasActiveCall(): Boolean {
        return activeCalls.values.any { it.state != CallState.ENDED }
    }
    
    /**
     * Gets active call data (simplified - removed mute and hold fields)
     */
    fun getActiveCallData(): Map<String, Any>? {
        val activeSession = activeCalls.values.firstOrNull { it.state != CallState.ENDED }
        return activeSession?.let { session ->
            mapOf(
                "id" to session.callData.id,
                "callerName" to session.callData.callerName,
                "callerNumber" to session.callData.callerNumber,
                "callerAvatar" to (session.callData.callerAvatar ?: ""),
                "isVideoCall" to session.callData.isVideoCall,
                "state" to getStateString(session.state),
                "extra" to session.callData.extra
            )
        }
    }
    
    /**
     * Gets call data by call ID
     */
    fun getCallData(callId: String): CallData? {
        return activeCalls[callId]?.callData
    }
    
    /**
     * Ends a call by call ID or the first active call if no ID provided
     */
    fun endCall(callId: String?): Boolean {
        val session = getCallSession(callId)
        return if (session != null) {
            session.state = CallState.ENDED
            // Send Flutter callback
            VCallkitPlugin.methodChannel?.invokeMethod("onCallEnded", mapOf(
                "callId" to session.callData.id,
                "reason" to "ended",
                "timestamp" to System.currentTimeMillis()
            ))
            Log.d(TAG, "Call ended: ${session.callData.id}")
            true
        } else {
            false
        }
    }
    
    /**
     * Create ongoing call session after answer
     */
    fun createOngoingCall(callData: CallData) {
        activeCalls[callData.id] = CallSession(callData, CallState.ACTIVE)
        Log.d(TAG, "Created ongoing call session: ${callData.id}")
    }
    
    /**
     * Answers a call by call ID or the first active call if no ID provided
     */
    fun answerCall(callId: String?): Boolean {
        val session = getCallSession(callId)
        return if (session != null && session.state == CallState.RINGING) {
            session.state = CallState.ACTIVE
            clearIncomingCallData()
            // Send Flutter callback
            VCallkitPlugin.methodChannel?.invokeMethod("onCallAnswered", mapOf(
                "callId" to session.callData.id,
                "timestamp" to System.currentTimeMillis()
            ))
            Log.d(TAG, "Call answered: ${session.callData.id}")
            true
        } else {
            false
        }
    }
    
    /**
     * Rejects a call by call ID or the first active call if no ID provided
     */
    fun rejectCall(callId: String?): Boolean {
        val session = getCallSession(callId)
        return if (session != null && session.state == CallState.RINGING) {
            session.state = CallState.ENDED
            clearIncomingCallData()
            // Send Flutter callback
            VCallkitPlugin.methodChannel?.invokeMethod("onCallRejected", mapOf(
                "callId" to session.callData.id,
                "timestamp" to System.currentTimeMillis()
            ))
            Log.d(TAG, "Call rejected: ${session.callData.id}")
            true
        } else {
            false
        }
    }
    
    /**
     * Gets a call session by ID or the first active one
     */
    private fun getCallSession(callId: String?): CallSession? {
        return if (callId != null) {
            activeCalls[callId]
        } else {
            activeCalls.values.firstOrNull { it.state != CallState.ENDED }
        }
    }
    
    /**
     * Converts call state to string (simplified - removed holding state)
     */
    private fun getStateString(state: CallState): String {
        return when (state) {
            CallState.RINGING -> "ringing"
            CallState.ACTIVE -> "active"
            CallState.ENDED -> "ended"
        }
    }
    
    /**
     * Cleanup ended calls (optional cleanup method)
     */
    fun cleanupEndedCalls() {
        val endedCalls = activeCalls.filterValues { it.state == CallState.ENDED }
        endedCalls.keys.forEach { callId ->
            activeCalls.remove(callId)
            Log.d(TAG, "Cleaned up ended call: $callId")
        }
    }
} 