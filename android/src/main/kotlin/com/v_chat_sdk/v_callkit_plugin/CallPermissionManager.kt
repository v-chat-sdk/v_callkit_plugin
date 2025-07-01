package com.v_chat_sdk.v_callkit_plugin

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * Manages all permission-related functionality for the CallKit plugin
 * Handles permission checking, requesting, and battery optimization
 */
class CallPermissionManager(private val context: Context) {
    
    companion object {
        private const val TAG = "CallPermissionManager"
    }
    
    /**
     * Check if all required permissions are granted
     */
    fun hasRequiredPermissions(): Boolean {
        return hasNotificationPermission() && hasBasicCallPermissions()
    }
    
    /**
     * Check if notification permission is granted (Android 13+)
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, 
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Not required for older versions
        }
    }
    
    /**
     * Check if basic call permissions are available
     */
    fun hasBasicCallPermissions(): Boolean {
        val requiredPermissions = getRequiredPermissions()
        
        return requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Check if foreground service permissions are available
     */
    fun hasForegroundServicePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14+ requires both FOREGROUND_SERVICE_PHONE_CALL and MANAGE_OWN_CALLS
            val hasPhoneCallPermission = ContextCompat.checkSelfPermission(
                context, 
                Manifest.permission.FOREGROUND_SERVICE_PHONE_CALL
            ) == PackageManager.PERMISSION_GRANTED
            
            val hasManageOwnCallsPermission = ContextCompat.checkSelfPermission(
                context, 
                Manifest.permission.MANAGE_OWN_CALLS
            ) == PackageManager.PERMISSION_GRANTED
            
            Log.d(TAG, "Android 14+ permission check - PhoneCall: $hasPhoneCallPermission, ManageOwnCalls: $hasManageOwnCallsPermission")
            hasPhoneCallPermission && hasManageOwnCallsPermission
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Android 9+ requires FOREGROUND_SERVICE
            val hasPermission = ContextCompat.checkSelfPermission(
                context, 
                Manifest.permission.FOREGROUND_SERVICE
            ) == PackageManager.PERMISSION_GRANTED
            
            Log.d(TAG, "Android 9+ permission check - ForegroundService: $hasPermission")
            hasPermission
        } else {
            // Older versions don't need special foreground service permissions
            true
        }
    }
    
    /**
     * Check if MANAGE_OWN_CALLS permission is available
     */
    fun hasManageOwnCallsPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.checkSelfPermission(
                context, 
                Manifest.permission.MANAGE_OWN_CALLS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Not required for older versions
        }
    }
    
    /**
     * Check if the app is excluded from battery optimization
     */
    fun isBatteryOptimizationIgnored(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true // Not applicable for older versions
        }
    }
    
    /**
     * Request to ignore battery optimization
     */
    fun requestBatteryOptimizationIgnore(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isBatteryOptimizationIgnored()) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                Log.d(TAG, "Battery optimization ignore request sent")
                true
            } else {
                Log.d(TAG, "Battery optimization already ignored or not applicable")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to request battery optimization ignore: ${e.message}")
            false
        }
    }
    
    /**
     * Check if SYSTEM_ALERT_WINDOW permission is granted
     */
    fun hasSystemAlertWindowPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true // Not required for older versions
        }
    }
    
    /**
     * Get list of required permissions based on Android version
     */
    fun getRequiredPermissions(): List<String> {
        val permissions = mutableListOf<String>()
        
        // Basic permissions for all versions
        permissions.addAll(listOf(
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.VIBRATE,
            Manifest.permission.MODIFY_AUDIO_SETTINGS
        ))
        
        // Android 5.0+ permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            permissions.add(Manifest.permission.USE_FULL_SCREEN_INTENT)
        }
        
        // Android 6.0+ permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissions.add(Manifest.permission.SYSTEM_ALERT_WINDOW)
        }
        
        // Android 8.0+ permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            permissions.addAll(listOf(
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.MANAGE_OWN_CALLS
            ))
        }
        
        // Android 9.0+ permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            permissions.add(Manifest.permission.DISABLE_KEYGUARD)
        }
        
        // Android 10+ permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.FOREGROUND_SERVICE_PHONE_CALL)
        }
        
        // Android 13+ permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        return permissions
    }
    
    /**
     * Get missing permissions that are not yet granted
     */
    fun getMissingPermissions(): List<String> {
        val requiredPermissions = getRequiredPermissions()
        
        return requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Get permission status information for debugging
     */
    fun getPermissionStatusInfo(): Map<String, Any> {
        val missingPermissions = getMissingPermissions()
        
        return mapOf(
            "hasAllRequired" to hasRequiredPermissions(),
            "hasNotification" to hasNotificationPermission(),
            "hasBasicCall" to hasBasicCallPermissions(),
            "hasForegroundService" to hasForegroundServicePermissions(),
            "hasManageOwnCalls" to hasManageOwnCallsPermission(),
            "hasSystemAlertWindow" to hasSystemAlertWindowPermission(),
            "isBatteryOptimized" to !isBatteryOptimizationIgnored(),
            "missingPermissions" to missingPermissions,
            "androidVersion" to Build.VERSION.SDK_INT,
            "manufacturer" to Build.MANUFACTURER.lowercase()
        )
    }
    
    /**
     * Log current permission status for debugging
     */
    fun logPermissionStatus() {
        val status = getPermissionStatusInfo()
        Log.d(TAG, "Permission status: $status")
        
        val missingPermissions = status["missingPermissions"] as List<*>
        if (missingPermissions.isNotEmpty()) {
            Log.w(TAG, "Missing permissions: $missingPermissions")
        }
    }
    
    /**
     * Check if current device is from a Chinese manufacturer that needs special handling
     */
    fun isChineseRomDevice(): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return CallConstants.CHINESE_ROM_MANUFACTURERS.contains(manufacturer)
    }
    
    /**
     * Get device-specific permission recommendations
     */
    fun getDeviceSpecificRecommendations(): List<String> {
        val recommendations = mutableListOf<String>()
        
        val manufacturer = Build.MANUFACTURER.lowercase()
        
        when (manufacturer) {
            CallConstants.MANUFACTURER_XIAOMI -> {
                recommendations.addAll(listOf(
                    "Enable 'Display pop-up windows while running in the background' in App permissions",
                    "Enable 'Display pop-up windows' permission",
                    "Add app to 'Autostart' list in Security app",
                    "Disable battery optimization for this app"
                ))
            }
            CallConstants.MANUFACTURER_HUAWEI -> {
                recommendations.addAll(listOf(
                    "Enable 'Display on top of other apps' permission",
                    "Add app to 'Protected apps' list",
                    "Disable 'App launch' restrictions",
                    "Enable 'Allow notifications' permission"
                ))
            }
            CallConstants.MANUFACTURER_OPPO, CallConstants.MANUFACTURER_ONEPLUS -> {
                recommendations.addAll(listOf(
                    "Enable 'Display over other apps' permission",
                    "Add app to 'Startup Manager' whitelist",
                    "Disable battery optimization",
                    "Enable 'Allow floating windows' permission"
                ))
            }
            CallConstants.MANUFACTURER_VIVO -> {
                recommendations.addAll(listOf(
                    "Enable 'Display over other apps' permission",
                    "Add app to 'Auto-start' whitelist",
                    "Enable 'Background app refresh'",
                    "Disable smart power saving for this app"
                ))
            }
            CallConstants.MANUFACTURER_SAMSUNG -> {
                recommendations.addAll(listOf(
                    "Add app to 'Never sleeping apps' list",
                    "Disable 'Put unused apps to sleep'",
                    "Enable 'Allow background activity'"
                ))
            }
        }
        
        // Common recommendations for all devices
        if (!isBatteryOptimizationIgnored()) {
            recommendations.add("Disable battery optimization for reliable call notifications")
        }
        
        return recommendations
    }
} 