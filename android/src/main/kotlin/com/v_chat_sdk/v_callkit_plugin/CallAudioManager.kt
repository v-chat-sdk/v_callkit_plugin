package com.v_chat_sdk.v_callkit_plugin

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log

/**
 * Manages all audio-related functionality for the CallKit plugin
 * Handles ringtones, vibration, audio focus, and media playback
 */
class CallAudioManager(private val context: Context) {
    
    companion object {
        private const val TAG = "CallAudioManager"
        private const val RINGTONE_VOLUME = 0.8f
        private const val MEDIA_PLAYER_VOLUME = 0.9f
    }
    
    private val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val vibrator: Vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    
    private var ringtone: Ringtone? = null
    private var mediaPlayer: MediaPlayer? = null
    private var customRingtoneUri: Uri? = null
    private var originalRingerMode: Int = AudioManager.RINGER_MODE_NORMAL
    private var originalMusicVolume: Int = 0
    private var isAudioFocusRequested: Boolean = false
    
    /**
     * Set custom ringtone URI
     */
    fun setCustomRingtone(uri: Uri?) {
        customRingtoneUri = uri
        Log.d(TAG, "Custom ringtone set: $uri")
    }
    
    /**
     * Start playing ringtone for incoming call
     */
    fun startRingtone(): Boolean {
        return try {
            stopRingtone() // Stop any existing ringtone
            
            // Request audio focus
            requestAudioFocus()
            
            // Store original audio settings
            saveOriginalAudioSettings()
            
            // Set audio to ring mode if needed
            setOptimalAudioSettings()
            
            val ringtoneUri = customRingtoneUri ?: getDefaultRingtoneUri()
            
            if (shouldUseMediaPlayer()) {
                startMediaPlayerRingtone(ringtoneUri)
            } else {
                startSystemRingtone(ringtoneUri)
            }
            
            Log.d(TAG, "Ringtone started successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start ringtone: ${e.message}")
            false
        }
    }
    
    /**
     * Stop playing ringtone
     */
    fun stopRingtone() {
        try {
            // Stop system ringtone
            ringtone?.stop()
            ringtone = null
            
            // Stop media player
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            }
            mediaPlayer = null
            
            // Restore original audio settings
            restoreOriginalAudioSettings()
            
            // Release audio focus
            releaseAudioFocus()
            
            Log.d(TAG, "Ringtone stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping ringtone: ${e.message}")
        }
    }
    
    /**
     * Start vibration for incoming call
     */
    fun startVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrationEffect = VibrationEffect.createWaveform(
                    CallConstants.INCOMING_CALL_VIBRATION_PATTERN,
                    0 // Repeat from index 0
                )
                vibrator.vibrate(vibrationEffect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(CallConstants.INCOMING_CALL_VIBRATION_PATTERN, 0)
            }
            Log.d(TAG, "Vibration started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start vibration: ${e.message}")
        }
    }
    
    /**
     * Stop vibration
     */
    fun stopVibration() {
        try {
            vibrator.cancel()
            Log.d(TAG, "Vibration stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping vibration: ${e.message}")
        }
    }
    
    /**
     * Start both ringtone and vibration
     */
    fun startCallSounds() {
        startRingtone()
        startVibration()
    }
    
    /**
     * Stop both ringtone and vibration
     */
    fun stopCallSounds() {
        stopRingtone()
        stopVibration()
    }
    
    /**
     * Check if device should use MediaPlayer instead of system ringtone
     * Useful for Chinese ROMs that have restrictions on system ringtone
     */
    private fun shouldUseMediaPlayer(): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return CallConstants.CHINESE_ROM_MANUFACTURERS.contains(manufacturer) ||
               audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT ||
               audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE
    }
    
    /**
     * Start ringtone using MediaPlayer (for Chinese ROMs or restricted environments)
     */
    private fun startMediaPlayerRingtone(uri: Uri) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, uri)
                
                // Set audio attributes for call ringtone
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                } else {
                    @Suppress("DEPRECATION")
                    setAudioStreamType(AudioManager.STREAM_RING)
                }
                
                setVolume(MEDIA_PLAYER_VOLUME, MEDIA_PLAYER_VOLUME)
                isLooping = true
                
                setOnPreparedListener { player ->
                    player.start()
                    Log.d(TAG, "MediaPlayer ringtone started")
                }
                
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    fallbackToSystemRingtone(uri)
                    true
                }
                
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start MediaPlayer ringtone: ${e.message}")
            fallbackToSystemRingtone(uri)
        }
    }
    
    /**
     * Start ringtone using system RingtoneManager
     */
    private fun startSystemRingtone(uri: Uri) {
        try {
            ringtone = RingtoneManager.getRingtone(context, uri)?.apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    isLooping = true
                    volume = RINGTONE_VOLUME
                }
                play()
            }
            Log.d(TAG, "System ringtone started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start system ringtone: ${e.message}")
            // Try default ringtone as fallback
            fallbackToDefaultRingtone()
        }
    }
    
    /**
     * Fallback to system ringtone when MediaPlayer fails
     */
    private fun fallbackToSystemRingtone(uri: Uri) {
        try {
            startSystemRingtone(uri)
        } catch (e: Exception) {
            Log.e(TAG, "Fallback to system ringtone failed: ${e.message}")
            fallbackToDefaultRingtone()
        }
    }
    
    /**
     * Final fallback to default ringtone
     */
    private fun fallbackToDefaultRingtone() {
        try {
            val defaultUri = getDefaultRingtoneUri()
            startSystemRingtone(defaultUri)
        } catch (e: Exception) {
            Log.e(TAG, "All ringtone methods failed: ${e.message}")
        }
    }
    
    /**
     * Get default ringtone URI
     */
    private fun getDefaultRingtoneUri(): Uri {
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: Uri.parse("android.resource://${context.packageName}/android.R.raw.notification_sound")
    }
    
    /**
     * Request audio focus for call audio
     */
    private fun requestAudioFocus() {
        try {
            if (!isAudioFocusRequested) {
                val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                    
                    val focusRequest = android.media.AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                        .setAudioAttributes(audioAttributes)
                        .setAcceptsDelayedFocusGain(false)
                        .build()
                    
                    audioManager.requestAudioFocus(focusRequest)
                } else {
                    @Suppress("DEPRECATION")
                    audioManager.requestAudioFocus(
                        null,
                        AudioManager.STREAM_RING,
                        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                    )
                }
                
                isAudioFocusRequested = (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                Log.d(TAG, "Audio focus requested: $isAudioFocusRequested")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to request audio focus: ${e.message}")
        }
    }
    
    /**
     * Release audio focus
     */
    private fun releaseAudioFocus() {
        try {
            if (isAudioFocusRequested) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // For API 26+, we would need to store the AudioFocusRequest object
                    // For simplicity, we'll use the legacy method
                    @Suppress("DEPRECATION")
                    audioManager.abandonAudioFocus(null)
                } else {
                    @Suppress("DEPRECATION")
                    audioManager.abandonAudioFocus(null)
                }
                
                isAudioFocusRequested = false
                Log.d(TAG, "Audio focus released")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release audio focus: ${e.message}")
        }
    }
    
    /**
     * Save original audio settings before modifying them
     */
    private fun saveOriginalAudioSettings() {
        originalRingerMode = audioManager.ringerMode
        originalMusicVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    }
    
    /**
     * Set optimal audio settings for call ringtone
     */
    private fun setOptimalAudioSettings() {
        try {
            // Ensure ringer mode allows sound
            if (audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT) {
                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                Log.d(TAG, "Changed ringer mode from silent to normal")
            }
            
            // Increase ring volume if it's too low
            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING)
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)
            
            if (currentVolume < maxVolume * 0.5) {
                val targetVolume = (maxVolume * 0.7).toInt()
                audioManager.setStreamVolume(AudioManager.STREAM_RING, targetVolume, 0)
                Log.d(TAG, "Increased ring volume to $targetVolume (max: $maxVolume)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set optimal audio settings: ${e.message}")
        }
    }
    
    /**
     * Restore original audio settings
     */
    private fun restoreOriginalAudioSettings() {
        try {
            // Only restore if we actually changed something
            if (audioManager.ringerMode != originalRingerMode) {
                audioManager.ringerMode = originalRingerMode
                Log.d(TAG, "Restored original ringer mode: $originalRingerMode")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore audio settings: ${e.message}")
        }
    }
    
    /**
     * Get list of available system ringtones
     */
    fun getSystemRingtones(): List<Map<String, String>> {
        val ringtones = mutableListOf<Map<String, String>>()
        
        try {
            val manager = RingtoneManager(context)
            manager.setType(RingtoneManager.TYPE_RINGTONE)
            
            val cursor = manager.cursor
            while (cursor.moveToNext()) {
                val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                val uri = manager.getRingtoneUri(cursor.position)
                
                ringtones.add(mapOf(
                    "title" to title,
                    "uri" to uri.toString()
                ))
            }
            
            cursor.close()
            Log.d(TAG, "Found ${ringtones.size} system ringtones")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get system ringtones: ${e.message}")
        }
        
        return ringtones
    }
    
    /**
     * Check if ringtone is currently playing
     */
    fun isRingtonePlaying(): Boolean {
        return (ringtone?.isPlaying == true) || (mediaPlayer?.isPlaying == true)
    }
    
    /**
     * Check if vibration is supported on this device
     */
    fun isVibrationSupported(): Boolean {
        return vibrator.hasVibrator()
    }
    
    /**
     * Get current audio mode information for debugging
     */
    fun getAudioModeInfo(): Map<String, Any> {
        return mapOf(
            "ringerMode" to audioManager.ringerMode,
            "audioMode" to audioManager.mode,
            "isMusicActive" to audioManager.isMusicActive,
            "isSpeakerphoneOn" to audioManager.isSpeakerphoneOn,
            "isBluetoothScoOn" to audioManager.isBluetoothScoOn,
            "ringVolume" to audioManager.getStreamVolume(AudioManager.STREAM_RING),
            "maxRingVolume" to audioManager.getStreamMaxVolume(AudioManager.STREAM_RING),
            "isRingtonePlaying" to isRingtonePlaying(),
            "isVibrationSupported" to isVibrationSupported(),
            "customRingtoneSet" to (customRingtoneUri != null)
        )
    }
} 