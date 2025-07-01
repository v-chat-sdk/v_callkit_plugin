package com.v_chat_sdk.v_callkit_plugin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log

/**
 * Special activity that handles call notification actions
 * Ensures proper app launching and call action processing
 */
class CallActionActivity : Activity() {
    
    companion object {
        private const val TAG = "CallActionActivity"
        
        fun createIntent(context: Context, action: String, callData: CallData): Intent {
            return Intent(context, CallActionActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("call_action", action)
                putExtra("call_id", callData.id)
                putExtra("call_data_bundle", callData.toBundle())
                putExtra("launched_from_notification_action", true)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val action = intent.getStringExtra("call_action")
        val callId = intent.getStringExtra("call_id")
        val callDataBundle = intent.getBundleExtra("call_data_bundle")
        
        Log.d(TAG, "CallActionActivity started with action: $action, callId: $callId")
        
        if (action != null && callId != null && callDataBundle != null) {
            try {
                val callData = CallData.fromBundle(callDataBundle)
                
                // Store the call action data for the Flutter app to retrieve
                VCallkitPlugin.setCallActionLaunchData(action, callData)
                
                // Send broadcast for internal processing (sounds, notifications, etc.)
                val broadcastIntent = Intent(this, CallActionReceiver::class.java).apply {
                    putExtra("action", action)
                    putExtra("callId", callId)
                }
                sendBroadcast(broadcastIntent)
                
                // Launch the main Flutter activity
                launchMainActivity(action, callData)
                
                Log.d(TAG, "Call action processed successfully: $action for ${callData.callerName}")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error processing call action: ${e.message}")
            }
        } else {
            Log.e(TAG, "Invalid call action data: action=$action, callId=$callId")
        }
        
        // Immediately finish this activity
        finish()
    }
    
    private fun launchMainActivity(action: String, callData: CallData) {
        try {
            // Get the main activity launch intent
            val packageManager = packageManager
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            
            if (launchIntent != null) {
                launchIntent.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra("call_action", action)
                    putExtra("call_id", callData.id)
                    putExtra("launched_from_call_action", true)
                }
                
                startActivity(launchIntent)
                Log.d(TAG, "Main activity launched for call action: $action")
            } else {
                Log.e(TAG, "Cannot launch main activity: no launch intent found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error launching main activity: ${e.message}")
        }
    }
} 