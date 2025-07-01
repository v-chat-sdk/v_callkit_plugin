package com.v_chat_sdk.v_callkit_plugin

import android.os.Bundle

/**
 * Kotlin representation of CallData for Android side
 * Represents call information passed between Flutter and Android native code
 */
data class CallData(
    val id: String,
    val callerName: String,
    val callerNumber: String,
    val callerAvatar: String? = null,
    val isVideoCall: Boolean = false,
    val extra: Map<String, Any> = emptyMap()
) {
    
    /**
     * Convert CallData to a Map for method channel communication
     */
    fun toMap(): Map<String, Any> {
        return mutableMapOf<String, Any>(
            "id" to id,
            "callerName" to callerName,
            "callerNumber" to callerNumber,
            "isVideoCall" to isVideoCall,
            "extra" to extra
        ).apply {
            callerAvatar?.let { put("callerAvatar", it) }
        }
    }
    
    /**
     * Convert CallData to a Bundle for Activity extras
     */
    fun toBundle(): Bundle {
        return Bundle().apply {
            putString("id", id)
            putString("callerName", callerName)
            putString("callerNumber", callerNumber)
            putString("callerAvatar", callerAvatar)
            putBoolean("isVideoCall", isVideoCall)
            // Convert extra map to bundle
            val extraBundle = Bundle()
            extra.forEach { (key, value) ->
                when (value) {
                    is String -> extraBundle.putString(key, value)
                    is Boolean -> extraBundle.putBoolean(key, value)
                    is Int -> extraBundle.putInt(key, value)
                    is Long -> extraBundle.putLong(key, value)
                    is Double -> extraBundle.putDouble(key, value)
                    is Float -> extraBundle.putFloat(key, value)
                    null -> extraBundle.putString(key, "") // Handle null values
                    else -> extraBundle.putString(key, value.toString())
                }
            }
            putBundle("extra", extraBundle)
        }
    }
    
    companion object {
        /**
         * Create CallData from a Map (typically from Flutter method channel)
         * With safe type casting to handle data from native platforms
         */
        @JvmStatic
        fun fromMap(map: Map<String, Any>): CallData {
            return CallData(
                id = map["id"] as? String ?: "",
                callerName = map["callerName"] as? String ?: "Unknown",
                callerNumber = map["callerNumber"] as? String ?: "",
                callerAvatar = map["callerAvatar"] as? String,
                isVideoCall = map["isVideoCall"] as? Boolean ?: false,
                extra = (map["extra"] as? Map<String, Any>) ?: emptyMap()
            )
        }
        
        /**
         * Create CallData from a Bundle (typically from Activity extras)
         */
        @JvmStatic
        fun fromBundle(bundle: Bundle): CallData {
            val extraBundle = bundle.getBundle("extra")
            val extra = mutableMapOf<String, Any>()
            
            extraBundle?.keySet()?.forEach { key ->
                extraBundle.get(key)?.let { value ->
                    extra[key] = value
                }
            }
            
            return CallData(
                id = bundle.getString("id") ?: "",
                callerName = bundle.getString("callerName") ?: "Unknown",
                callerNumber = bundle.getString("callerNumber") ?: "",
                callerAvatar = bundle.getString("callerAvatar"),
                isVideoCall = bundle.getBoolean("isVideoCall", false),
                extra = extra
            )
        }
    }
} 