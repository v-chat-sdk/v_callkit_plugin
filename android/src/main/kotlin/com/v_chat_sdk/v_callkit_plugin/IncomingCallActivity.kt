package com.v_chat_sdk.v_callkit_plugin

import android.app.Activity
import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView

class IncomingCallActivity : Activity() {
    
    companion object {
        private const val TAG = "IncomingCallActivity"
        
        fun start(context: Context, callData: CallData) {
            val intent = Intent(context, IncomingCallActivity::class.java).apply {
                putExtra(CallConstants.EXTRA_CALL_DATA, callData.toBundle())
                flags = CallConstants.CALL_SCREEN_LAUNCH_FLAGS
            }
            context.startActivity(intent)
        }
    }
    
    private lateinit var wakeLock: PowerManager.WakeLock
    private var callData: CallData? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Extract call data
        val bundle = intent.getBundleExtra(CallConstants.EXTRA_CALL_DATA)
        callData = bundle?.let { CallData.fromBundle(it) }
        
        if (callData == null) {
            Log.e(TAG, "No call data provided")
            finish()
            return
        }
        
        // Set up window to show on lock screen
        setupLockScreenVisibility()
        
        // Acquire wake lock to turn on screen
        acquireWakeLock()
        
        // Set up the UI
        setupCallUI()
        
        Log.d(TAG, "IncomingCallActivity created for call: ${callData?.id}")
    }
    
    private fun setupLockScreenVisibility() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        
        // Make it full screen
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )
    }
    
    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            CallConstants.WAKE_LOCK_TAG
        ).apply {
            acquire(CallConstants.WAKE_LOCK_TIMEOUT_MS)
        }
    }
    
    private fun setupCallUI() {
        setContentView(createCallLayout())
    }
    
    private fun createCallLayout(): View {
        // Create a simple vertical layout for the call screen
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setBackgroundColor(android.graphics.Color.parseColor(CallConstants.COLOR_BACKGROUND))
            gravity = android.view.Gravity.CENTER
            setPadding(40, 100, 40, 100)
        }
        
        // Avatar placeholder
        val avatarView = View(this).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(
                CallConstants.AVATAR_SIZE_DP * 2, 
                CallConstants.AVATAR_SIZE_DP * 2
            ).apply {
                gravity = android.view.Gravity.CENTER_HORIZONTAL
                setMargins(0, 0, 0, 40)
            }
            background = android.graphics.drawable.ShapeDrawable(
                android.graphics.drawable.shapes.OvalShape()
            ).apply {
                paint.color = android.graphics.Color.parseColor(CallConstants.COLOR_AVATAR_PLACEHOLDER)
            }
        }
        layout.addView(avatarView)
        
        // Incoming call text
        val incomingText = TextView(this).apply {
            text = if (callData?.isVideoCall == true) "Incoming Video Call" else "Incoming Voice Call"
            textSize = 16f
            setTextColor(android.graphics.Color.parseColor(CallConstants.COLOR_TEXT_SECONDARY))
            gravity = android.view.Gravity.CENTER
        }
        layout.addView(incomingText)
        
        // Caller name
        val nameText = TextView(this).apply {
            text = callData?.callerName ?: "Unknown"
            textSize = 32f
            setTextColor(android.graphics.Color.parseColor(CallConstants.COLOR_TEXT_PRIMARY))
            gravity = android.view.Gravity.CENTER
            setPadding(0, 20, 0, 10)
        }
        layout.addView(nameText)
        
        // Caller number
        if (!callData?.callerNumber.isNullOrEmpty()) {
            val numberText = TextView(this).apply {
                text = callData?.callerNumber ?: ""
                textSize = 18f
                setTextColor(android.graphics.Color.parseColor(CallConstants.COLOR_TEXT_SECONDARY))
                gravity = android.view.Gravity.CENTER
                setPadding(0, 0, 0, 80)
            }
            layout.addView(numberText)
        }
        
        // Buttons layout
        val buttonsLayout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        // Decline button (circular red)
        val declineButton = createCircularButton(
            android.graphics.Color.parseColor(CallConstants.COLOR_DECLINE_BUTTON),
            android.R.drawable.ic_menu_close_clear_cancel,
            "Decline call"
        ) { handleDecline() }
        
        // Answer button (circular green)
        val answerButton = createCircularButton(
            android.graphics.Color.parseColor(CallConstants.COLOR_ANSWER_BUTTON),
            android.R.drawable.ic_menu_call,
            "Answer call"
        ) { handleAnswer() }
        
        // Add spacing between buttons
        val spacer = View(this).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(CallConstants.BUTTON_SPACING_DP, 0)
        }
        
        buttonsLayout.addView(declineButton)
        buttonsLayout.addView(spacer)
        buttonsLayout.addView(answerButton)
        
        layout.addView(buttonsLayout)
        
        return layout
    }
    
    private fun createCircularButton(color: Int, iconRes: Int, contentDescription: String, onClick: () -> Unit): View {
        val button = android.widget.FrameLayout(this).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(
                CallConstants.BUTTON_SIZE_DP * 2,
                CallConstants.BUTTON_SIZE_DP * 2
            )
        }
        
        // Background circle
        val background = View(this).apply {
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
            )
            background = android.graphics.drawable.ShapeDrawable(
                android.graphics.drawable.shapes.OvalShape()
            ).apply {
                paint.color = color
            }
            setOnClickListener { onClick() }
            this.contentDescription = contentDescription
        }
        
        // Icon
        val icon = ImageView(this).apply {
            layoutParams = android.widget.FrameLayout.LayoutParams(
                CallConstants.BUTTON_SIZE_DP,
                CallConstants.BUTTON_SIZE_DP
            ).apply {
                gravity = android.view.Gravity.CENTER
            }
            setImageResource(iconRes)
            setColorFilter(android.graphics.Color.parseColor(CallConstants.COLOR_TEXT_PRIMARY))
            scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
        
        button.addView(background)
        button.addView(icon)
        
        return button
    }
    
    private fun handleAnswer() {
        callData?.let { data ->
            Log.d(TAG, "Handling answer for call: ${data.id}")
            
            // Dismiss the incoming call notification (system automatically stops ringtone)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(CallConstants.CALL_NOTIFICATION_ID)
            Log.d(TAG, "Notification dismissed - system automatically stopped ringtone")
            
            // Use CallManager to handle answered call
            val callManager = CallManager.getInstance(this)
            callManager.handleCallAnswered(data)
            
            // Launch the main Flutter app with call action data
            launchMainAppWithCallAction(CallConstants.ACTION_ANSWER, data)
            
            // Send answer action
            sendCallAction(CallConstants.ACTION_ANSWER, data.id)
            
            Log.d(TAG, "Call answered via IncomingCallActivity: ${data.id}")
        }
        finish()
    }
    
    private fun handleDecline() {
        callData?.let { data ->
            Log.d(TAG, "Handling decline for call: ${data.id}")
            
            // Dismiss the incoming call notification completely (system automatically stops ringtone)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(CallConstants.CALL_NOTIFICATION_ID)
            Log.d(TAG, "Notification dismissed - system automatically stopped ringtone")
            
            // Use CallManager to handle declined call
            val callManager = CallManager.getInstance(this)
            callManager.handleCallDeclined(data)
            
            // Send decline action (internal processing only)
            sendCallAction(CallConstants.ACTION_DECLINE, data.id)
            
            Log.d(TAG, "Call declined silently, notification dismissed: ${data.id}")
        }
        finish()
    }
    
    private fun sendCallAction(action: String, callId: String) {
        val intent = Intent(this, CallActionReceiver::class.java).apply {
            putExtra(CallConstants.EXTRA_ACTION, action)
            putExtra(CallConstants.EXTRA_CALL_ID, callId)
        }
        sendBroadcast(intent)
    }
    
    private fun launchMainAppWithCallAction(action: String, callData: CallData) {
        try {
            // Store the call action data for the app to retrieve
            VCallkitPlugin.setCallActionLaunchData(action, callData)
            
            // Create intent to launch the main activity
            val packageManager = packageManager
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            
            if (launchIntent != null) {
                launchIntent.apply {
                    addFlags(CallConstants.ACTIVITY_LAUNCH_FLAGS)
                    putExtra("call_action", action)
                    putExtra("call_id", callData.id)
                    putExtra("launched_from_call_screen", true)
                }
                
                startActivity(launchIntent)
                Log.d(TAG, "Main app launched with call action: $action for ${callData.callerName}")
            } else {
                Log.e(TAG, "Cannot launch main app: no launch intent found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error launching main app with call action: ${e.message}")
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        Log.d(TAG, "IncomingCallActivity destroyed")
        
        // Release wake lock
        if (::wakeLock.isInitialized && wakeLock.isHeld) {
            wakeLock.release()
            Log.d(TAG, "Wake lock released")
        }
        
        // No need to stop sounds manually - system handles ringtone lifecycle automatically
        Log.d(TAG, "System manages ringtone lifecycle - no manual cleanup needed")
    }
    
    override fun onBackPressed() {
        // Prevent back button from dismissing the call
    }
} 